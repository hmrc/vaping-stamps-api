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
import org.mockito.ArgumentMatchers.{any, anyString}
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
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.controllers.actions.AuthAction
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummaryResponse}
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

  val approvalSummary = ApprovalSummaryResponse(
    approvalStatus = "APPROVED",
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

  val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")

  val getRequest = FakeRequest(GET, s"/status/$vdsApprovalId/summary")
  val postRequest: FakeRequest[JsValue] = FakeRequest(POST, "/status").withBody(Json.toJson(approvalRequest))

  val errorJson: JsObject =
    Json.obj("code" -> "FORBIDDEN", "message" -> "You are not authorised to access this resource")

  "ApprovalController.retrieveStatus" should {

    "return OK with valid JSON body when parameters to the call are valid" in {
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val result = controller.retrieveStatus().apply(postRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(approvalSummary)
    }

    "return NoContent when the service returns HttpResponse with status 204" in {
      val httpResponse204 = HttpResponse(204, Json.obj("message" -> "No content").toString)
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(httpResponse204)))

      val result = controller.retrieveStatus().apply(postRequest)

      status(result) mustBe NO_CONTENT
      contentType(result) mustBe None
      contentAsString(result) mustBe empty
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

    "return 403 with JSON body when service returns Left(HttpResponse) with status 403" in {
      val httpResponse =
        HttpResponse(403, errorJson.toString())

      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(httpResponse)))

      val request = FakeRequest(POST, "/status")
        .withBody(Json.toJson(approvalRequest))

      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe FORBIDDEN
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe errorJson
    }

  }

  "ApprovalController.retrieveSummary" should {

    "return OK with valid request parameters" in {
      when(mockService.retrieveSummary(anyString())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val result = controller.retrieveSummary(vdsApprovalId).apply(getRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(approvalSummary)
    }

    "return NoContent when the service returns HttpResponse with status 204" in {
      val httpResponse204 = HttpResponse(204, Json.obj("message" -> "No content").toString)
      when(mockService.retrieveSummary(anyString())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(httpResponse204)))

      val result = controller.retrieveSummary(vdsApprovalId).apply(getRequest)

      status(result) mustBe NO_CONTENT
      contentType(result) mustBe None
      contentAsString(result) mustBe empty
    }

    "return 403 with JSON body when service returns Left(HttpResponse) with status 403" in {
      val httpResponse =
        HttpResponse(403, errorJson.toString())

      when(mockService.retrieveSummary(anyString())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(httpResponse)))

      val result = controller.retrieveSummary(vdsApprovalId).apply(getRequest)

      status(result) mustBe FORBIDDEN
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe errorJson
    }

  }
}
