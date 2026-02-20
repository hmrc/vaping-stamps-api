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

    "call the correct URL and return HttpResponse" in {
      val approvalId = "AAAA0000200BB"

      val stubRequestBuilder = mock(classOf[RequestBuilder])
      val fakeResponse = HttpResponse(200, """{"approvalStatus":"APPROVED"}""")

      when(mockHttpClient.get(any())(any())).thenReturn(stubRequestBuilder)

      when(stubRequestBuilder.setHeader(any()))
        .thenReturn(stubRequestBuilder)

      when(stubRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
        .thenReturn(Future(fakeResponse))

      val resultF = connector.retrieveSummary(approvalId)
      val result = Await.result(resultF, 2.seconds)

      result.status mustBe 200
      result.body must include("APPROVED")

      verify(stubRequestBuilder).execute[HttpResponse](using HttpReads.Implicits.readRaw)

    }

    "fail when HttpClient throws exception" in {
      val approvalId = "AAAA0000404BB"

      val stubRequestBuilder = mock(classOf[RequestBuilder])

      when(mockHttpClient.get(any())(any())).thenReturn(stubRequestBuilder)

      when(stubRequestBuilder.setHeader(any()))
        .thenReturn(stubRequestBuilder)

      when(stubRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
        .thenReturn(Future.failed(new RuntimeException("Internal error")))

      val resultF = connector.retrieveSummary(approvalId)

      val ex = intercept[RuntimeException](Await.result(resultF, 2.seconds))
      ex.getMessage mustBe "Internal error"

    }
  }
}
