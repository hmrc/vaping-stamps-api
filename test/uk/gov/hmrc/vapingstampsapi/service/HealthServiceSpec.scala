package uk.gov.hmrc.vapingstampsapi.service

import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.MongoComponent
import org.mongodb.scala.MongoDatabase
import org.mockito.ArgumentMatchers.any
import org.mongodb.scala.bson.Document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {

  "HealthService.check" should {

    "return true when Mongo ping succeeds" in {
      val mongoComponent = mock[MongoComponent]
      val database       = mock[MongoDatabase]

      when(mongoComponent.database).thenReturn(database)

      val service = spy(new HealthService(mongoComponent))
      doReturn(Future.successful(()))
        .when(service)
        .pingMongo()

      whenReady(service.check())(_ shouldBe true)
    }

    "return false when Mongo ping fails" in {
      val mongoComponent = mock[MongoComponent]

      val service = spy(new HealthService(mongoComponent))
      doReturn(Future.failed(new RuntimeException("boom")))
        .when(service)
        .pingMongo()

      whenReady(service.check())(_ shouldBe false)
    }
  }
}
