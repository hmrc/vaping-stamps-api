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

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import play.api.mvc.*
import play.api.test.Helpers.stubControllerComponents

import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummary}
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.mdc.MdcExecutionContext
import uk.gov.hmrc.vapingstampsapi.services.ApprovalService
import uk.gov.hmrc.vapingstampsapi.controllers.actions.AuthAction
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer

import scala.concurrent.*

class ApprovalControllerSpec extends AnyWordSpec with Matchers {

  private given system: ActorSystem = ActorSystem("test")
  private given mat: Materializer = Materializer(system)
  given headerCarrier: HeaderCarrier = HeaderCarrier()
  given ec: ExecutionContext = MdcExecutionContext()

  private val mockAuthConnector = mock(classOf[AuthConnector])
  private val mockCc: ControllerComponents = stubControllerComponents()
  private val mockService = mock(classOf[ApprovalService])

  private val stubAuthorisedAction: AuthAction = new AuthAction {
    override val authConnector: AuthConnector = mockAuthConnector
    override val parser: BodyParser[AnyContent] = mockCc.parsers.defaultBodyParser
    override implicit val executionContext: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
      block(request)
  }

  private val controller =
    new ApprovalController(
      mockAuthConnector,
      stubAuthorisedAction,
      mockCc,
      mockService
    )

  "ApprovalController.retrieveStatus" should {

    val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")

    val approvalSummary = ApprovalSummary(
      approvalStatus = "APPROVED",
      businessName = "Test Ltd",
      registeredBusinessAddress = "Addr",
      correspondenceAddress = "Addr2",
      contactName = "John",
      contactTelephone = "123",
      contactEmail = "test@test.com",
      approvalNumber = "GBVA0000001DS",
      stampThreshold = 100L
    )

    "return 200 with JSON body when service returns Right(ApprovalSummary)" in {
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val request = FakeRequest(POST, "/status")
        .withBody(Json.toJson(approvalRequest))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(approvalSummary)
    }

    "return BadRequest when request body is missing" in {
      val request = FakeRequest(POST, "/status")
        .withBody(JsNull)
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe BAD_REQUEST
    }

    "return BadRequest when request body has invalid JSON" in {
      val request = FakeRequest(POST, "/status")
        .withBody(Json.obj("unexpected" -> "field"))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe BAD_REQUEST
    }

    "return ServiceUnavailable when service returns EIS error" in {
      when(mockService.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(EisApiError(503, "Unavailable"))))

      val request = FakeRequest(POST, "/status")
        .withBody(Json.toJson(approvalRequest))
      val result = controller.retrieveStatus().apply(request)

      status(result) mustBe SERVICE_UNAVAILABLE
    }
  }
}
