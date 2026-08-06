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
import org.apache.pekko.Done
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
import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.controllers.actions.{AuthAction, StubAuthAction}
import uk.gov.hmrc.vapingstampsapi.models.errors.InternalServerErrorApiError
import uk.gov.hmrc.vapingstampsapi.services.DeleteRecordService

import scala.concurrent.*

class DeleteRecordControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val mockService = mock(classOf[DeleteRecordService])
  private val mockAuthConnector = mock(classOf[AuthConnector])

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[DeleteRecordService].toInstance(mockService),
        bind[AuthConnector].toInstance(mockAuthConnector),
        bind[AuthAction].to[StubAuthAction]
      )
      .build()

  override def beforeEach(): Unit = {
    reset(mockService)
    super.beforeEach()
  }

  private given mat: Materializer = app.materializer

  private lazy val controller = app.injector.instanceOf[DeleteRecordController]

  private val validStampsReferenceNumber = "GBVC0000001DS"
  private val invalidStampsReferenceNumber = "INVALID123"

  "DeleteRecordController.deleteRecord" should {

    "return 204 No Content when deletion succeeds" in {
      when(mockService.deleteRecord(eqTo(validStampsReferenceNumber))(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Right(Done))))

      val request = FakeRequest(DELETE, s"/delete-record/$validStampsReferenceNumber")
      val result = controller.deleteRecord(validStampsReferenceNumber).apply(request)

      status(result) mustBe NO_CONTENT
      contentAsString(result) mustBe ""
    }

    "return 400 Bad Request when stampsReferenceNumber is invalid" in {
      val request = FakeRequest(DELETE, s"/delete-record/$invalidStampsReferenceNumber")
      val result = controller.deleteRecord(invalidStampsReferenceNumber).apply(request)

      status(result) mustBe BAD_REQUEST
      val json = contentAsJson(result)
      (json \ "code").as[String] mustBe "BAD_REQUEST"
      (json \ "message").as[String] must include("Invalid stampsReferenceNumber")
    }

    "return 400 Bad Request when stampsReferenceNumber has wrong prefix" in {
      val wrongPrefix = "AAVC0000001DS"
      val request = FakeRequest(DELETE, s"/delete-record/$wrongPrefix")
      val result = controller.deleteRecord(wrongPrefix).apply(request)

      status(result) mustBe BAD_REQUEST
    }

    "return 400 Bad Request when stampsReferenceNumber has wrong format" in {
      val wrongFormat = "GBVC000001DS"  // Missing one digit
      val request = FakeRequest(DELETE, s"/delete-record/$wrongFormat")
      val result = controller.deleteRecord(wrongFormat).apply(request)

      status(result) mustBe BAD_REQUEST
    }

    "return 500 Internal Server Error when service returns error" in {
      val error = InternalServerErrorApiError(message = "Error occurred deleting record")

      when(mockService.deleteRecord(eqTo(validStampsReferenceNumber))(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Left(error))))

      val request = FakeRequest(DELETE, s"/delete-record/$validStampsReferenceNumber")
      val result = controller.deleteRecord(validStampsReferenceNumber).apply(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.toJson(error)
    }

    "call service with correct stampsReferenceNumber" in {
      when(mockService.deleteRecord(eqTo(validStampsReferenceNumber))(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Right(Done))))

      val request = FakeRequest(DELETE, s"/delete-record/$validStampsReferenceNumber")
      controller.deleteRecord(validStampsReferenceNumber).apply(request)

      verify(mockService).deleteRecord(eqTo(validStampsReferenceNumber))(using any[HeaderCarrier])
    }

    "accept XI prefix in stampsReferenceNumber" in {
      val xiReference = "XIVC0000001DS"
      when(mockService.deleteRecord(eqTo(xiReference))(using any[HeaderCarrier]))
        .thenReturn(EitherT(Future.successful(Right(Done))))

      val request = FakeRequest(DELETE, s"/delete-record/$xiReference")
      val result = controller.deleteRecord(xiReference).apply(request)

      status(result) mustBe NO_CONTENT
    }
  }
}