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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.config.AppConfig

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class EISConnectorSpec extends AnyWordSpec with Matchers {

  private val mockHttpClient = mock(classOf[HttpClientV2])
  private val mockAppConfig = mock(classOf[AppConfig])
  private val connector = new EISConnector(mockHttpClient, mockAppConfig)

  when(mockAppConfig.eisBaseUrl).thenReturn("http://localhost:1234")
  when(mockAppConfig.eisEnvironment).thenReturn("test-env")
  when(mockAppConfig.eisAuthToken).thenReturn("auth-token")

  given hc: HeaderCarrier = HeaderCarrier()

  "EISConnector.retrieveSummary" should {

    val stubRequestBuilder = mock(classOf[RequestBuilder])
    when(mockHttpClient.get(any())(any())).thenReturn(stubRequestBuilder)
    when(stubRequestBuilder.setHeader(any()))
      .thenReturn(stubRequestBuilder)

    "call the correct URL and return HttpResponse 200" in {
      val approvalId = "AAAA0000200BB"

      val fakeResponse = HttpResponse(
        200,
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
      )

      when(stubRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
        .thenReturn(Future(fakeResponse))

      val resultF = connector.retrieveSummary(approvalId)
      val result = Await.result(resultF, 2.seconds)

      result.status mustBe 200
      result.body must include("APPROVED")

      verify(stubRequestBuilder).execute[HttpResponse](using HttpReads.Implicits.readRaw)

    }

    "call the correct URL and return HttpResponse 204" in {
      val approvalId = "AAAA0000204BB"

      val noContentResponse = HttpResponse(204, "")

      when(stubRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
        .thenReturn(Future(noContentResponse))

      val resultF = connector.retrieveSummary(approvalId)
      val result = Await.result(resultF, 2.seconds)

      result.status mustBe 204
      result.body mustBe ""

    }

    "fail when HttpClient throws exception" in {
      val approvalId = "AAAA0000404BB"

      when(stubRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
        .thenReturn(Future.failed(new RuntimeException("Internal error")))

      val resultF = connector.retrieveSummary(approvalId)

      val ex = intercept[RuntimeException](Await.result(resultF, 2.seconds))
      ex.getMessage mustBe "Internal error"

    }
  }
}
