/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vapingstampsapi.controllers

import cats.data.EitherT
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.*
import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.controllers.actions.{AuthAction, StubAuthAction, StubValidateRequestAction, ValidateRequestAction}
import uk.gov.hmrc.vapingstampsapi.models.ApprovalStatus.{approved, not_approved}
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalStatus, ApprovedSummaryResponse, NotApprovedSummaryResponse, VDSDetails}
import uk.gov.hmrc.vapingstampsapi.services.ApprovalService

import scala.concurrent.*

class ApprovalControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val mockService = mock(classOf[ApprovalService])
  private val mockAuthConnector = mock(classOf[AuthConnector])

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[ApprovalService].toInstance(mockService),
        bind[AuthConnector].toInstance(mockAuthConnector),
        bind[AuthAction].to[StubAuthAction],
        bind[ValidateRequestAction].to[StubValidateRequestAction]
      )
      .build()

  override def beforeEach(): Unit = {
    reset(mockService)
    super.beforeEach()
  }

  private given mat: Materializer = app.materializer

  private lazy val controller = app.injector.instanceOf[ApprovalController]

  val approvedSummary = ApprovedSummaryResponse(
    approvalStatus = approved,
    businessName = "Acme Vaping Ltd",
    addressLine1 = "123 Main Street",
    addressLine2 = Some("Any Town"),
    addressLine3 = None,
    addressLine4 = None,
    addressLine5 = None,
    postCode = "SW98 1XY",
    contactName = Some("John Doe"),
    telephoneNumber = Some("0123456789"),
    stampsThreshold = 10000L
  )

  val notApprovedSummary = NotApprovedSummaryResponse(
    approvalStatus = not_approved
  )

  val incomingVDSDetails: JsObject =
    Json.obj("vdsEmail" -> JsString("example@email.com"), "stampsReferenceNumber" -> JsString("XIVC0000001BB"))
  val incomingNotApprovedVDSDetails: JsObject =
    Json.obj("vdsEmail" -> JsString("testBad@test.com"), "stampsReferenceNumber" -> JsString("XIVC0000001BB"))

  def postRequest(postRequest: JsObject): FakeRequest[JsValue] = FakeRequest(POST, "/status").withBody(postRequest)

  "ApprovalController.retrieveStatus" should {

    "return OK with valid JSON body when parameters to the call are valid" in {
      when(mockService.retrieveStatus(any[VDSDetails])(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Right(approvedSummary))))

      val result = controller.retrieveStatus().apply(postRequest(incomingVDSDetails))

      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(approvedSummary)
      status(result) mustBe OK
    }

    "return OK with valid JSON body for not_approved Status when parameters to the call are valid" in {
      when(mockService.retrieveStatus(any[VDSDetails])(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Right(notApprovedSummary))))

      val result = controller.retrieveStatus().apply(postRequest(incomingNotApprovedVDSDetails))

      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(notApprovedSummary)
      status(result) mustBe OK
    }

    "return BadRequest when the request body is missing" in {
      val request = FakeRequest(POST, "/status")
        .withBody(JsNull)
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe BAD_REQUEST
    }

    "return BadRequest when the request body has invalid JSON" in {
      val request = FakeRequest(POST, "/status")
        .withBody(Json.obj("unexpected" -> "field"))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe BAD_REQUEST
    }

    "return 400 with JSON body when service returns Left(BadRequestApiError)" in {
      val errorResponse =
        BadRequestApiError(errors = Seq(MissingAcceptHeader, IncorrectContentTypeHeader))

      when(mockService.retrieveStatus(any[VDSDetails])(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val request = FakeRequest(POST, "/status")
        .withBody(incomingVDSDetails)

      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(errorResponse)
    }

    "return 422 with JSON body when service returns Left(UnprocessableEntityApiError)" in {
      val errorResponse =
        UnprocessableEntityApiError(errors = Seq(VdsEmailNotFound))

      when(mockService.retrieveStatus(any[VDSDetails])(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val request = FakeRequest(POST, "/status")
        .withBody(incomingVDSDetails)

      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe UNPROCESSABLE_ENTITY
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(errorResponse)
    }

    "return 500 with JSON body when service returns Left(InternalServerErrorApiError)" in {
      val errorResponse =
        InternalServerErrorApiError(message = "Error has occurred in the service")

      when(mockService.retrieveStatus(any[VDSDetails])(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val request = FakeRequest(POST, "/status")
        .withBody(incomingVDSDetails)

      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(errorResponse)
    }

    "return 503 with JSON body when service returns Left(ServiceUnavailableApiError)" in {
      val errorResponse =
        ServiceUnavailableApiError(message = "Error has occurred downstream")

      when(mockService.retrieveStatus(any[VDSDetails])(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val request = FakeRequest(POST, "/status")
        .withBody(incomingVDSDetails)

      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe SERVICE_UNAVAILABLE
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(errorResponse)
    }
  }
}
