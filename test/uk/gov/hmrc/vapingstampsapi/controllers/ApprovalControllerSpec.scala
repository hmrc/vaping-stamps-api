package uk.gov.hmrc.vapingstampsapi.controllers

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.models.*
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import uk.gov.hmrc.vapingstampsapi.services.ApprovalService

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

class ApprovalControllerSpec extends AnyWordSpec with Matchers {

  private val mockService = mock(classOf[ApprovalService])
  private val controller =
    new ApprovalController(
      Helpers.stubControllerComponents(),
      mockService
    )

  given HeaderCarrier = HeaderCarrier()

  "ApprovalController.retrieveSummary" should {

    "return 200 and full approval summary payload for valid ID" in {

      val approvalId = "AAAA0000200BB"

      val expectedResponse = ApprovalSummary(
        approvalStatus = "APPROVED",
        businessName = "Acme Vaping Ltd",
        registeredBusinessAddress = "1 Business Park, London, SW1A 1AA",
        correspondenceAddress = "PO Box 123, London, SW1A 2BB",
        contactName = "John Smith",
        contactTelephone = "02071234567",
        contactEmail = "john.smith@acmevaping.co.uk",
        approvalNumber = approvalId,
        stampThreshold = 10000L
      )

      when(
        mockService.retrieveSummary(any[String])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(expectedResponse)))

      val request = FakeRequest(GET, s"/status/$approvalId/summary")
      val result = controller.retrieveSummary(approvalId).apply(request)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")

      contentAsJson(result) mustBe Json.toJson(expectedResponse)
    }

    "return error status when service returns Left(EisApiError)" in {

      val approvalId = "AAAA0000200BB"

      when(
        mockService.retrieveSummary(any[String])(using any[HeaderCarrier])
      ).thenReturn(
        Future.successful(
          Left(EisApiError(404, "Not found"))
        )
      )

      val request = FakeRequest(GET, s"/status/$approvalId/summary")
      val result = controller.retrieveSummary(approvalId).apply(request)

      status(result) mustBe NOT_FOUND
    }
  }
}
