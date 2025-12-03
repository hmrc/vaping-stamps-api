package uk.gov.hmrc.vapingstampsapi.controllers

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.vapingstampsapi.service.HealthService

import scala.concurrent.Future

class HealthControllerSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar {

  "HealthController.health" should {

    "return 200 when Mongo is healthy" in {
      val mockMongo = mock[HealthService]
      when(mockMongo.check()).thenReturn(Future.successful(true))

      val controller = new HealthController(
        cc = Helpers.stubControllerComponents(),
        healthService = mockMongo
      )(using scala.concurrent.ExecutionContext.global)

      val result = controller.health(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) shouldBe """{"status":"UP"}"""
    }

    "return 503 when Mongo is unhealthy" in {
      val mockMongo = mock[HealthService]
      when(mockMongo.check()).thenReturn(Future.successful(false))

      val controller = new HealthController(
        cc = Helpers.stubControllerComponents(),
        healthService = mockMongo
      )(using scala.concurrent.ExecutionContext.global)

      val result = controller.health(FakeRequest())

      status(result) shouldBe SERVICE_UNAVAILABLE
      contentAsString(result) shouldBe """{"status":"DOWN","error":"Mongo unreachable"}"""
    }
  }
}

