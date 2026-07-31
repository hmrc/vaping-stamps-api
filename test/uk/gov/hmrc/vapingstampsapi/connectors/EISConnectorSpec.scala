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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.models.ApprovalStatus.{approved, not_approved}
import uk.gov.hmrc.vapingstampsapi.models.errors.{ServiceUnavailableApiError, StampsReferenceNumberNotFound, UnprocessableEntityApiError}
import uk.gov.hmrc.vapingstampsapi.models.{ApprovedSummaryResponse, NotApprovedSummaryResponse, VDSDetails}
import uk.gov.hmrc.vapingstampsapi.utils.WiremockHelper

import scala.concurrent.*
import scala.concurrent.duration.DurationInt
import scala.util.Left

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

    val approvalRequest = VDSDetails("test@test.com", "GBVC0000001DS")
    val responseBody =
      """
        |{
        |  "approvalStatus": "approved",
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

    val responseBodyNotApproved =
      """
        |{
        |  "approvalStatus": "not approved",
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

    val outgoingRequestBody =
      """
        |{
        |  "vdsdetails": {
        |     "vdsEmail": "test@test.com",
        |     "stampsReferenceNumber": "GBVC0000001DS"
        |  }
        |}
        |""".stripMargin

    "return HttpResponse 200 with a success and Full responseBody" in {
      stubEndpointForPost(200, outgoingRequestBody, responseBody)
      val result = Await.result(connector.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Right(
        ApprovedSummaryResponse(
          approved,
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

    "return HttpResponse 200 with a success and a Not Approved responseBody" in {
      stubEndpointForPost(200, outgoingRequestBody, responseBodyNotApproved)
      val result = Await.result(connector.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Right(
        NotApprovedSummaryResponse(
          not_approved
        )
      )
    }

    "propagate 422 response as UnprocessableEntityApiError" in {
      val responseBody = """{
                           |  "errorDetail": {
                           |    "errorCode": "422",
                           |    "errorMessage": "Unprocessable Entity",
                           |    "source": "Backend",
                           |    "sourceFaultDetail": {
                           |      "detail": [
                           |        "001"
                           |      ]
                           |    },
                           |    "timestamp": "2026-04-14T10:54:12Z",
                           |    "correlationId": "1ae81b45-41b4-4642-ae1c-db1126900001"
                           |  }
                           |}""".stripMargin

      stubEndpointForPost(422, outgoingRequestBody, responseBody)
      val result = Await.result(connector.retrieveStatus(approvalRequest).value, 1.seconds)

      result mustBe Left(UnprocessableEntityApiError(errors = Seq(StampsReferenceNumberNotFound)))
    }

    "propagate non-200 HttpResponse from downstream" in {
      stubEndpointForPost(400, outgoingRequestBody, "The request payload is invalid or malformed.")
      val result = Await.result(connector.retrieveStatus(approvalRequest).value, 1.seconds)

      result mustBe Left(ServiceUnavailableApiError(message = "Error has occurred in downstream service"))
    }

    "handle downstream timeout by handling HttpException" in {
      server.stubFor(
        post(urlEqualTo("/excise/decision/vapingstamps/profile/v1"))
          .withRequestBody(equalToJson(outgoingRequestBody))
          .willReturn(
            aResponse()
              .withStatus(499)
              .withHeader("Content-Type", "application/json", "Accept", "date", "x-correlation-id", "x-forwarded-host")
              .withBody("The request payload has timed out")
              .withFixedDelay(2000)
          )
      )

      val result = Await.result(connector.retrieveStatus(approvalRequest).value, 2.seconds)

      result mustBe Left(
        ServiceUnavailableApiError(message =
          s"POST of 'http://localhost:${server.port()}/excise/decision/vapingstamps/profile/v1' timed out with message 'Request timeout to localhost/127.0.0.1:${server.port()} after 1000 ms'"
        )
      )
    }
  }
}
