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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.*
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import org.mockito.ArgumentMatchers.any

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class ApprovalServiceSpec extends AnyWordSpec with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockEisConnector = mock(classOf[EISConnector])
  private val service = new ApprovalService(mockEisConnector)

  "ApprovalService.retrieveSummary" should {

    "return connector response" in {
      val approvalId = "AAAA0000200BB"

      val expectedResponse = ApprovalSummary(
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

      when(mockEisConnector.retrieveSummary(any[String])(using any()))
        .thenReturn(Future.successful(Right(expectedResponse)))

      val result = service.retrieveSummary(approvalId)

      Await.result(result, 2.seconds) mustBe Right(expectedResponse)
    }
  }
}
