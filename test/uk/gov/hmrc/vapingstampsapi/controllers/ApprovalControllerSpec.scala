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

import com.google.inject.Inject
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.*
import play.api.mvc.*
import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.controllers.actions.AuthAction
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummary}
import uk.gov.hmrc.vapingstampsapi.services.ApprovalService

import scala.concurrent.*

class StubAuthAction @Inject() (
  override val authConnector: AuthConnector,
  cc: ControllerComponents
)(implicit val executionContext: ExecutionContext)
    extends AuthAction:
  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    block(request)

class ApprovalControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val mockService = mock(classOf[ApprovalService])
  private val mockAuthConnector = mock(classOf[AuthConnector])

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[ApprovalService].toInstance(mockService),
        bind[AuthConnector].toInstance(mockAuthConnector),
        bind[AuthAction].to[StubAuthAction]
      )
      .build()

  override def beforeEach(): Unit = {
    reset(mockService)
    super.beforeEach()
  }

  private given mat: Materializer = app.materializer

  private lazy val controller = app.injector.instanceOf[ApprovalController]

  val vdsApprovalId = "GBVA0000001DS"

  val approvalSummary = ApprovalSummary(
    approvalStatus = "APPROVED",
    businessName = "Acme Vaping Ltd",
    registeredBusinessAddress = "1 Business Park, London, SW1A 1AA",
    correspondenceAddress = "PO Box 123, London, SW1A 2BB",
    contactName = "John Smith",
    contactTelephone = "02071234567",
    contactEmail = "john.smith@acmevaping.co.uk",
    approvalNumber = "GBVA0000001DS",
    stampThreshold = 10000L
  )

  val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")

  "ApprovalController.retrieveStatus" should {

    "return OK with valid JSON body when parameters to the call are valid" in {
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val request = FakeRequest(POST, "/status")
        .withBody(Json.toJson(approvalRequest))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(approvalSummary)
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

    "return Service unavailable when a downstream EIS error occurs" in {
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(EisApiError(503, "Unavailable"))))

      val request = FakeRequest(POST, "/status")
        .withBody(Json.toJson(approvalRequest))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe SERVICE_UNAVAILABLE
    }

    // TODO: this may get wrapped up in a 204 - awaiting EIS/ETDS schema.
    "return ServiceUnavailable when an Approval is not found" in {
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(ApprovalNotFound)))

      val request = FakeRequest(POST, "/status")
        .withBody(Json.toJson(approvalRequest))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe SERVICE_UNAVAILABLE
    }
  }

  // TODO: leaving in for coverage; will probably be removed once we have ETDS confirmation
  "ApprovalController.retrieveSummary" should {

    "return 200 with JSON body when service returns Right(ApprovalSummary)" in {
      when(mockService.retrieveSummary(eqTo(vdsApprovalId))(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val result = controller
        .retrieveSummary(vdsApprovalId)
        .apply(FakeRequest(GET, s"/status/$vdsApprovalId/summary"))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(approvalSummary)
    }

    "return the EIS status code when service returns Left(EisApiError)" in {
      when(mockService.retrieveSummary(eqTo(vdsApprovalId))(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(EisApiError(404, "Not found"))))

      val result = controller
        .retrieveSummary(vdsApprovalId)
        .apply(FakeRequest(GET, s"/status/$vdsApprovalId/summary"))

      status(result) mustBe NOT_FOUND
    }

    "return Service Unavailable when service returns Left with non-EIS error" in {
      when(mockService.retrieveSummary(eqTo(vdsApprovalId))(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(ApprovalNotFound)))

      val result = controller
        .retrieveSummary(vdsApprovalId)
        .apply(FakeRequest(GET, s"/status/$vdsApprovalId/summary"))

      status(result) mustBe SERVICE_UNAVAILABLE
    }
  }
}
