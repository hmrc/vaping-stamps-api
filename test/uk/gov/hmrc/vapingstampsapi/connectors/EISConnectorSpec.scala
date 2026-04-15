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

package uk.gov.hmrc.vapingstampsapi.connectors

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummaryResponse}
import uk.gov.hmrc.vapingstampsapi.utils.WiremockHelper

import scala.concurrent.*
import scala.concurrent.duration.DurationInt

class EISConnectorSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WiremockHelper {

  given hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.eis.port" -> server.port()
    )
    .build()

  lazy val connector: EISConnector =
    app.injector.instanceOf[EISConnector]

  "EISConnector.retrieveSummary" should {

    val approvalId = "AAAA0000200BB"

    "call the correct URL and return HttpResponse 200" in {

      val responseBody =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "registeredBusinessAddress": "1 Business Park, London, SW1A 1AA",
          |  "correspondenceAddress": "PO Box 123, London, SW1A 2BB",
          |  "contactName": "John Smith",
          |  "contactTelephone": "02071234567",
          |  "contactEmail": "john.smith@acmevaping.co.uk",
          |  "approvalNumber": "AAAA0000200BB",
          |  "stampThreshold": 10000
          |}
          |
          """.stripMargin

      stubEndpointForGet(200, responseBody, approvalId)
      val resultF = connector.retrieveSummary(approvalId)
      val result = Await.result(resultF, 2.seconds)

      result mustBe Right(
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
      )

    }

    "propagate non-200 HttpResponse from downstream" in {
      stubEndpointForGet(400, "The request payload is invalid or malformed.", approvalId)
      val result = Await.result(connector.retrieveSummary(approvalId), 2.seconds)

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left("The request payload is invalid or malformed.")
    }

  }

  "EISConnector.retrieveStatus" should {

    val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")
    val responseBody =
      """
        |{
        |  "approvalStatus": "APPROVED",
        |  "businessName": "Acme Vaping Ltd",
        |  "registeredBusinessAddress": "1 Business Park, London, SW1A 1AA",
        |  "correspondenceAddress": "PO Box 123, London, SW1A 2BB",
        |  "contactName": "John Smith",
        |  "contactTelephone": "02071234567",
        |  "contactEmail": "john.smith@acmevaping.co.uk",
        |  "approvalNumber": "AAAA0000200BB",
        |  "stampThreshold": 10000
        |}
        |
         """.stripMargin

    "return HttpResponse 200 on success" in {
      stubEndpointForPost(200, responseBody)
      val result = Await.result(connector.retrieveStatus(approvalRequest), 2.seconds)

      result mustBe Right(
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
      )
    }

    "propagate non-200 HttpResponse from downstream" in {
      stubEndpointForPost(400, "The request payload is invalid or malformed.")
      val result = Await.result(connector.retrieveStatus(approvalRequest), 2.seconds)

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left("The request payload is invalid or malformed.")
    }
  }
}
