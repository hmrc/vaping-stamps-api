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

import cats.data.EitherT
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.*
import uk.gov.hmrc.vapingstampsapi.models.errors.ApprovalStatus.{Approved, Not_Approved}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class ApprovalServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val mockConnector = mock[EISConnector]
  private val service = new ApprovalService(mockConnector)

  private val approvalRequest =
    ApprovalRequest("test@test.com", "GBVA0000001DS")

  private val approvalSummary =
    ApprovalSummaryResponse(
      approvalStatus = Approved,
      businessName = "Acme Vaping Ltd",
      addressLine1 = "1 Business Park",
      addressLine2 = Some("London"),
      addressLine3 = None,
      addressLine4 = None,
      addressLine5 = None,
      postCode = "SW1A 1AA",
      contactName = Some("John Smith"),
      telephoneNumber = Some("02071234567"),
      stampsThreshold = 10000
    )

  private val enrichedApprovalSummary =
    EnrichedApprovalSummary(
      Approved,
      "Acme Vaping Ltd",
      "1 Business Park",
      Some("London"),
      None,
      None,
      None,
      "SW1A 1AA",
      "test@test.com",
      Some("John Smith"),
      Some("02071234567"),
      10000
    )

  private val notApprovedSummary =
    ApprovalSummaryResponse(
      approvalStatus = Not_Approved,
      businessName = "Acme Vaping Ltd",
      addressLine1 = "1 Business Park",
      addressLine2 = Some("London"),
      addressLine3 = None,
      addressLine4 = None,
      addressLine5 = None,
      postCode = "SW1A 1AA",
      contactName = Some("John Smith"),
      telephoneNumber = Some("02071234567"),
      stampsThreshold = 10000
    )

  private val enrichedNotApprovedSummary =
    EnrichedApprovalSummary(
      Not_Approved,
      "Acme Vaping Ltd",
      "1 Business Park",
      Some("London"),
      None,
      None,
      None,
      "SW1A 1AA",
      "test@test.com",
      Some("John Smith"),
      Some("02071234567"),
      10000
    )

  "ApprovalService.retrieveStatus" should {

    "return Right(ApprovalSummaryResponse) when downstream returns 200" in {
      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(EitherT(Future.successful(Right(approvalSummary))))

      val result = Await.result(service.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Right(enrichedApprovalSummary)
    }

    "propagate Left(HttpResponse) when downstream returns an error" in {
      val errorResponse =
        HttpResponse(
          400,
          """{"code":"INVALID_REQUEST","message":"The request payload is invalid or malformed."}"""
        )

      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val result = Await.result(service.retrieveStatus(approvalRequest).value, 2.seconds)

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left(
        """{"code":"INVALID_REQUEST","message":"The request payload is invalid or malformed."}"""
      )
    }
  }
}
