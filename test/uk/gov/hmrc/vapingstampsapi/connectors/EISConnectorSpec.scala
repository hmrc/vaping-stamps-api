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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, urlEqualTo}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.models.ApprovalStatus.Approved
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovedSummaryResponse}
import uk.gov.hmrc.vapingstampsapi.utils.WiremockHelper

class EISConnectorSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WiremockHelper {

  given hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.eis.port" -> server.port(),
      "play.ws.timeout.request"        -> "1000ms",
      "play.ws.timeout.connection"     -> "500ms"
    )
    .build()

  lazy val connector: EISConnector =
    app.injector.instanceOf[EISConnector]

  "EISConnector.retrieveStatus" should {

    val approvalRequest = ApprovalRequest("test@test.com", "GBVA0000001DS")
    val responseBody =
      """
        |{
        |  "approvalStatus": "APPROVED",
        |  "businessName": "Acme Vaping Ltd",
        |  "addressLine1": "1 Business Park",
        |  "addressLine2": "London",
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
        |  "vdsEmail": "test@test.com",
        |  "stampsReferenceNumber": "GBVA0000001DS"
        |}
        |""".stripMargin

    "return HttpResponse 200 on success" in {
      stubEndpointForPost(200, requestBody, responseBody)
      val result = connector.retrieveStatus(approvalRequest).futureValue

      result mustBe Right(
        ApprovedSummaryResponse(
          Approved,
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
      val result = connector.retrieveStatus(approvalRequest).futureValue

      result.left.map(_.status) mustBe Left(400)
      result.left.map(_.body) mustBe Left("The request payload is invalid or malformed.")
    }

    "handle downstream timeout by handling HttpException" in {
      server.stubFor(
        post(urlEqualTo("/etds/vaping/stamps/status"))
          .withRequestBody(equalToJson(requestBody))
          .willReturn(
            aResponse()
              .withStatus(499)
              .withHeader("Content-Type", "application/json")
              .withBody("The request payload has timed out")
              .withFixedDelay(2000)
          )
      )

      val result = connector.retrieveStatus(approvalRequest).futureValue

      result.left.map(_.status) mustBe Left(503)
    }
  }
}
