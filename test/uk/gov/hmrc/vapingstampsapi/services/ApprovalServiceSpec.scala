package uk.gov.hmrc.vapingstampsapi.services

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.*
import uk.gov.hmrc.vapingstampsapi.models.errors.*

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

class ApprovalServiceSpec extends AnyWordSpec with Matchers {

  private val mockEisConnector = mock(classOf[EISConnector])
  private val service = new ApprovalService(mockEisConnector)

  given HeaderCarrier = HeaderCarrier()

  "ApprovalService.retrieveSummary" should {

    "return Right(ApprovalSummary) when downstream returns 200" in {

      val approvalId = "AAAA0000200BB"

      val approvalSummary = ApprovalSummary(
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

      val jsonBody = Json.toJson(approvalSummary).toString()

      when(mockEisConnector.retrieveSummary(any[String])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(200, jsonBody)))

      val result = Await.result(service.retrieveSummary(approvalId), 2.seconds)

      result mustBe Right(approvalSummary)
    }
  }
}
