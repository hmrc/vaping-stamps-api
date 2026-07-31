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

package uk.gov.hmrc.vapingstampsapi.controllers.actions

import cats.data.NonEmptyChainImpl
import cats.data.Validated.{Invalid, Valid}
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.vapingstampsapi.models.VDSDetails
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import uk.gov.hmrc.vapingstampsapi.validators.RequestValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidateRequestActionSpec extends AnyWordSpec with Matchers {

  val mockRequestValidator: RequestValidator = mock[RequestValidator]
  val validator: ValidateRequestActionImpl = ValidateRequestActionImpl(global, mockRequestValidator)

  "ValidateRequestAction" must {
    "filter" should {
      "return None" when {
        "valid request is supplied" in {
          val request: FakeRequest[_] = FakeRequest()
            .withHeaders(("Accept", "application/vnd.hmrc.1.0+json"), ("Authorization", "Bearer Token"))
            .withJsonBody(
              Json.obj(
                "vdsdetails" -> Json.obj(
                  "vdsEmail"              -> JsString("example@email.com"),
                  "stampsReferenceNumber" -> JsString("XIVC0000001BB")
                )
              )
            )

          when(mockRequestValidator.fromRequest(request))
            .thenReturn(Valid(VDSDetails("email@example.com", "XIVC0000001BB")))

          await(validator.filter(request)) mustBe None
        }
      }

      "return Bad Request" when {
        "Invalid request is supplied" in {
          val request: FakeRequest[_] = FakeRequest()

          when(mockRequestValidator.fromRequest(request))
            .thenReturn(
              Invalid(
                NonEmptyChainImpl(
                  MissingAcceptHeader,
                  MissingContentTypeHeader,
                  MissingStampsReferenceNumber,
                  MissingVdsEmail
                )
              )
            )

          val json = Json.obj(
            "code"    -> "BAD_REQUEST",
            "message" -> "The request is invalid",
            "errors"  -> Seq("001", "003", "005", "007")
          )

          await(validator.filter(request)).map { result =>
            status(Future.successful(result)) mustBe BAD_REQUEST
            contentAsJson(Future.successful(result)) mustBe json
          }
        }
      }
    }
  }
}
