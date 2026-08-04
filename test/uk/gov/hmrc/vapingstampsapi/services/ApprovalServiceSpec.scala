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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.*
import uk.gov.hmrc.vapingstampsapi.models.ApprovalStatus.{approved, not_approved}
import uk.gov.hmrc.vapingstampsapi.models.errors.{ServiceUnavailableApiError, StampsReferenceNumberNotFound, UnprocessableEntityApiError}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class ApprovalServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val mockConnector = mock[EISConnector]
  private val service = new ApprovalService(mockConnector)

  private val approvalRequest =
    VDSDetails("test@test.com", "GBVC0000001DS")

  private val approvalBadRequest =
    VDSDetails("test@badtest.com", "GBVC0000001DS")

  private val approvedSummary =
    ApprovedSummaryResponse(
      approvalStatus = approved,
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

  private val notApprovedSummary =
    NotApprovedSummaryResponse(
      approvalStatus = not_approved
    )

  "ApprovalService.retrieveStatus" should {

    "return Right(ApprovedSummaryResponse) when downstream returns 200 with an approved status message" in {
      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(EitherT(Future.successful(Right(approvedSummary))))

      val result = Await.result(service.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Right(approvedSummary)
    }

    "return Right(NotApprovedSummaryResponse) when downstream returns 200 with a not approved status message" in {
      when(mockConnector.retrieveStatus(approvalBadRequest))
        .thenReturn(EitherT(Future.successful(Right(notApprovedSummary))))

      val result = Await.result(service.retrieveStatus(approvalBadRequest).value, 2.seconds)

      result mustBe Right(notApprovedSummary)
    }

    "propagate Left(UnprocessableEntityApiError) when downstream returns an Left(UnprocessableEntityApiError)" in {
      val errorResponse =
        UnprocessableEntityApiError(errors = Seq(StampsReferenceNumberNotFound))

      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val result = Await.result(service.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Left(
        UnprocessableEntityApiError(errors = Seq(StampsReferenceNumberNotFound))
      )
    }

    "propagate Left(ServiceUnavailableApiError) when downstream returns an Left(ServiceUnavailableApiError)" in {
      val errorResponse =
        ServiceUnavailableApiError(message = "Downstream error has occurred")

      when(mockConnector.retrieveStatus(approvalRequest))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val result = Await.result(service.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Left(
        ServiceUnavailableApiError(message = "Downstream error has occurred")
      )
    }
  }
}
