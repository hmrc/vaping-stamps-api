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
          |  "addressLine1": "1 Business Park",
          |  "addressLine2": "London",
          |  "addressLine3": null,
          |  "addressLine4": null,
          |  "addressLine5": null,
          |  "postCode": "SW1A 1AA",
          |  "contactName": "John Smith",
          |  "telephoneNumber": "02071234567",
          |  "stampsThreshold": 10000
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
          "1 Business Park",
          Some("London"),
          None,
          None,
          None,
          "SW1A 1AA",
          Some("John Smith"),
          Some("02071234567"),
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

    "return HttpResponse 200 with all optional fields present" in {

      val responseBody =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "addressLine2": "London",
          |  "addressLine3": "Greater London",
          |  "addressLine4": "United Kingdom",
          |  "addressLine5": "Europe",
          |  "postCode": "SW1A 1AA",
          |  "contactName": "John Smith",
          |  "telephoneNumber": "02071234567",
          |  "stampsThreshold": 10000
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
          "1 Business Park",
          Some("London"),
          Some("Greater London"),
          Some("United Kingdom"),
          Some("Europe"),
          "SW1A 1AA",
          Some("John Smith"),
          Some("02071234567"),
          10000
        )
      )

    }

    "return HttpResponse 200 with only mandatory fields present" in {

      val responseBody =
        """
          |{
          |  "approvalStatus": "APPROVED",
          |  "businessName": "Acme Vaping Ltd",
          |  "addressLine1": "1 Business Park",
          |  "postCode": "SW1A 1AA",
          |  "stampsThreshold": 10000
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
          "1 Business Park",
          None,
          None,
          None,
          None,
          "SW1A 1AA",
          None,
          None,
          10000
        )
      )

    }
  }

  "EISConnector.retrieveStatus" should {

    val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")
    val responseBody =
      """
        |{
        |  "approvalStatus": "APPROVED",
        |  "businessName": "Acme Vaping Ltd",
        |  "addressLine1": "1 Business Park",
        |  "addressLine2": "London",
        |  "addressLine3": null,
        |  "addressLine4": null,
        |  "addressLine5": null,
        |  "postCode": "SW1A 1AA",
        |  "contactName": "John Smith",
        |  "telephoneNumber": "02071234567",
        |  "stampsThreshold": 10000
        |}
        |
          """.stripMargin
    val requestBody =
      """
        |{
        |  "contactEmail": "test@test.com",
        |  "vdsApprovalId": "GBVA0000001DS"
        |}
        |""".stripMargin

    "return HttpResponse 200 on success" in {
      stubEndpointForPost(200, requestBody, responseBody)
      val result = Await.result(connector.retrieveStatus(approvalRequest), 2.seconds)

      result mustBe Right(
        ApprovalSummaryResponse(
          "APPROVED",
          "Acme Vaping Ltd",
          "1 Business Park",
          Some("London"),
          None,
          None,
          None,
          "SW1A 1AA",
          Some("John Smith"),
          Some("02071234567"),
          10000
        )
      )
    }

    "propagate non-200 HttpResponse from downstream" in {
      stubEndpointForPost(400, requestBody, "The request payload is invalid or malformed.")
      val result = Await.result(connector.retrieveStatus(approvalRequest), 2.seconds)

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left("The request payload is invalid or malformed.")
    }
  }
}
