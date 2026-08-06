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

package uk.gov.hmrc.vapingstampsapi.services

import cats.data.EitherT
import org.apache.pekko.Done
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.errors.InternalServerErrorApiError

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class DeleteRecordServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val mockConnector = mock[EISConnector]
  private val service = new DeleteRecordService(mockConnector)

  private val stampsReferenceNumber = "GBVC0000001DS"

  "DeleteRecordService.deleteRecord" should {

    "return Right(Done) when connector returns Right(Done)" in {
      when(mockConnector.deleteRecord(stampsReferenceNumber))
        .thenReturn(EitherT(Future.successful(Right(Done))))

      val result = Await.result(service.deleteRecord(stampsReferenceNumber).value, 2.seconds)

      result mustBe Right(Done)
    }

    "return Left(InternalServerErrorApiError) when connector returns Left(InternalServerErrorApiError)" in {
      val errorResponse =
        InternalServerErrorApiError(message = "Error occurred deleting record")

      when(mockConnector.deleteRecord(stampsReferenceNumber))
        .thenReturn(EitherT(Future.successful(Left(errorResponse))))

      val result = Await.result(service.deleteRecord(stampsReferenceNumber).value, 2.seconds)

      result mustBe Left(
        InternalServerErrorApiError(message = "Error occurred deleting record")
      )
    }
  }
}