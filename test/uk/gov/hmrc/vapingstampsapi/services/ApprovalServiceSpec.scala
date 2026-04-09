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

package uk.gov.hmrc.vapingstampsapi.services

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummaryResponse}
import uk.gov.hmrc.vapingstampsapi.models.errors.*

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class ApprovalServiceSpec extends AnyWordSpec with Matchers {

  private val mockEisConnector = mock(classOf[EISConnector])
  private val service = new ApprovalService(mockEisConnector)

  given HeaderCarrier = HeaderCarrier()

  "ApprovalService.retrieveSummary" should {

    "return Right(ApprovalSummary) when downstream returns 200" in {

      val approvalId = "AAAA0000200BB"

      val approvalSummary = ApprovalSummaryResponse(
        approvalStatus = "APPROVED",
        businessName = "Test Ltd",
        registeredBusinessAddress = "Addr",
        correspondenceAddress = "Addr2",
        contactName = "John",
        contactTelephone = "123",
        contactEmail = "test@test.com",
        approvalNumber = approvalId,
        stampThreshold = 100L
      )

      val jsonBody = Json.toJson(approvalSummary).toString()

      when(mockEisConnector.retrieveSummary(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(200, jsonBody)))

      val result = Await.result(service.retrieveSummary(approvalId), 2.seconds)

      result mustBe Right(approvalSummary)
    }
  }

  "ApprovalService.retrieveStatus" should {

    val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")

    val approvalSummary = ApprovalSummaryResponse(
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

    "return Right(ApprovalSummary) when downstream returns 200 with valid JSON" in {
      val jsonBody = Json.toJson(approvalSummary).toString()

      when(mockEisConnector.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(200, jsonBody)))

      val result = Await.result(service.retrieveStatus(approvalRequest), 2.seconds)

      result mustBe Right(approvalSummary)
    }

    "return Left(EisApiError(500, ...)) when downstream returns 200 with JSON that fails schema validation" in {
      when(mockEisConnector.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(200, """{"unexpected":"field"}""")))

      val result = Await.result(service.retrieveStatus(approvalRequest), 2.seconds)

      result mustBe Left(EisApiError(500, "Invalid JSON from downstream"))
    }

    "return Left(EisApiError(status, ...)) when downstream returns non-200" in {
      when(mockEisConnector.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(404, "Not found")))

      val result = Await.result(service.retrieveStatus(approvalRequest), 2.seconds)

      result mustBe Left(EisApiError(404, "Not found"))
    }

    "return Left(EisApiError(503, ...)) when connector throws NonFatal exception" in {
      when(mockEisConnector.retrieveStatus(any[ApprovalRequest])(using any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Connection refused")))

      val result = Await.result(service.retrieveStatus(approvalRequest), 2.seconds)

      result mustBe Left(EisApiError(503, "Downstream service unavailable"))
    }
  }
}
