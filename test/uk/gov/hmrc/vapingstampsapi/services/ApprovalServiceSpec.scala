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

import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.*

import scala.concurrent.{ExecutionContext, Future}

class ApprovalServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val mockConnector = mock[EISConnector]
  private val service = new ApprovalService(mockConnector)

  private val approvalId = "AAAA0000200BB"
  private val approvalRequest =
    ApprovalRequest("test@test.com", "GBVA0000001DS")

  private val approvalSummary =
    ApprovalSummaryResponse(
      "APPROVED",
      "Acme Vaping Ltd",
      "1 Business Park, London, SW1A 1AA",
      "PO Box 123, London, SW1A 2BB",
      "John Smith",
      "02071234567",
      "john.smith@acmevaping.co.uk",
      "AAAA0000200BB",
      10000
    )

  "ApprovalService.retrieveSummary" should {

    "return Right(ApprovalSummaryResponse) when downstream returns 200 with valid JSON" in {
      when(mockConnector.retrieveSummary(approvalId))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val result = service.retrieveSummary(approvalId).futureValue

      result mustBe Right(approvalSummary)
    }

    "propagate Left(HttpResponse) when downstream returns an error" in {
      val errorResponse =
        HttpResponse(
          400,
          """{"code":"INVALID_REQUEST","message":"The request payload is invalid or malformed."}"""
        )

      when(mockConnector.retrieveSummary(approvalId))
        .thenReturn(Future.successful(Left(errorResponse)))

      val result = service.retrieveSummary(approvalId).futureValue

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left(
        """{"code":"INVALID_REQUEST","message":"The request payload is invalid or malformed."}"""
      )
    }

    "return Left(HttpResponse) when UpstreamErrorResponse is thrown" in {
      when(mockConnector.retrieveSummary(approvalId))
        .thenReturn(
          Future.failed(
            UpstreamErrorResponse("Bad Request", 400)
          )
        )

      val result = service.retrieveSummary(approvalId).futureValue

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left("Bad Request")
    }

    "return 503 when a non-fatal exception occurs" in {
      when(mockConnector.retrieveSummary(approvalId))
        .thenReturn(Future.failed(new RuntimeException("Timeout")))

      val result = service.retrieveSummary(approvalId).futureValue

      result.left.map(_.status) mustBe Left(503)
      result.left.map(_.body) mustBe Left("Downstream service unavailable")
    }
  }

  "ApprovalService.retrieveStatus" should {

    "return Right(ApprovalSummaryResponse) when downstream returns 200" in {
      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(Future.successful(Right(approvalSummary)))

      val result = service.retrieveStatus(approvalRequest).futureValue

      result mustBe Right(approvalSummary)
    }

    "propagate Left(HttpResponse) when downstream returns an error" in {
      val errorResponse =
        HttpResponse(
          400,
          """{"code":"INVALID_REQUEST","message":"The request payload is invalid or malformed."}"""
        )

      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(Future.successful(Left(errorResponse)))

      val result = service.retrieveStatus(approvalRequest).futureValue

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left(
        """{"code":"INVALID_REQUEST","message":"The request payload is invalid or malformed."}"""
      )
    }

    "return Left(HttpResponse) when UpstreamErrorResponse is thrown" in {
      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(
          Future.failed(
            UpstreamErrorResponse("Bad Request", 400)
          )
        )

      val result = service.retrieveStatus(approvalRequest).futureValue

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left("Bad Request")
    }

    "return 503 when a non-fatal exception occurs" in {
      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(Future.failed(new RuntimeException("Timeout")))

      val result = service.retrieveStatus(approvalRequest).futureValue

      result.left.map(_.status) mustBe Left(503)
      result.left.map(_.body) mustBe Left("Downstream service unavailable")
    }
  }
}
