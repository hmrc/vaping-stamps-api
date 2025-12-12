package uk.gov.hmrc.vapingstampsapi.service

import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.MongoComponent
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class HealthServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "HealthService check" should {

    import org.mongodb.scala._
    import org.mongodb.scala.bson.Document

    "HealthService check should return true when Mongo responds to ping" in {
      val mongoComponent = mock[MongoComponent]
      val database = mock[MongoDatabase]

      val successObs = mock[org.mongodb.scala.SingleObservable[Document]]
      when(successObs.toFuture()).thenReturn(Future.successful(Document()))

      when(database.runCommand(Document("ping" -> 1))).thenReturn(successObs)

      when(database.runCommand(Document("ping" -> 1)))
        .thenReturn(Observable(Document()))


      val service = new HealthService(mongoComponent)
      service.check().map(_ shouldBe true)
    }

    "HealthService check should return false when Mongo ping fails" in {
      val mongoComponent = mock[MongoComponent]
      val database = mock[MongoDatabase]


      val failedObs = mock[org.mongodb.scala.SingleObservable[Document]]
      when(failedObs.toFuture()).thenReturn(Future.failed(new RuntimeException("boom")))

      when(mongoComponent.database).thenReturn(database)
      when(database.runCommand(Document("ping" -> 1))).thenReturn(failedObs)

      // success case
      when(database.runCommand(Document("ping" -> 1)))
        .thenReturn(Observable(Document()))


      val service = new HealthService(mongoComponent)
      service.check().map(_ shouldBe false)
    }

  }
}

