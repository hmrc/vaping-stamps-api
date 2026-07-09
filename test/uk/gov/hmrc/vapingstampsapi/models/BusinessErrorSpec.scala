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

package uk.gov.hmrc.vapingstampsapi.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResultException, JsString, Json, JsonValidationError}
import uk.gov.hmrc.vapingstampsapi.models.errors.{BusinessError, StampRequestedBefore, StampsReferenceNumberNotFound, VdsEmailNotFound}

class BusinessErrorSpec extends AnyWordSpec with Matchers:
  "BusinessError" must {
    "toString" must {
      "MissingStampsReferenceNumber must be 001" in {
        StampsReferenceNumberNotFound.toString mustBe "001"
      }

      "MissingVdsEmail must be 002" in {
        VdsEmailNotFound.toString mustBe "002"
      }

      "StampRequestedBefore must be 003" in {
        StampRequestedBefore.toString mustBe "003"
      }
    }

    "read from JsValue" when {
      "reads of JsString(001) must be JsSuccess(StampsReferenceNumberNotFound)" in {
        JsString("001").as[BusinessError] mustBe StampsReferenceNumberNotFound
      }

      "reads of JsString(002) must be JsSuccess(VdsEmailNotFound)" in {
        JsString("002").as[BusinessError] mustBe VdsEmailNotFound
      }

      "reads of JsString(003) must be JsSuccess(StampRequestedBefore)" in {
        JsString("003").as[BusinessError] mustBe StampRequestedBefore
      }
    }

    "return JsError" when {
      "unreadable JsValue is bound" in {
        intercept[JsResultException] {
          JsString("004").as[BusinessError]
        }.errors map { case (_, error) =>
          error mustBe List(JsonValidationError(List("Invalid BusinessError value")))
        }
      }
    }

    "write as JsValue" when {
      "write StampsReferenceNumberNotFound to JsString(001)" in {
        Json.toJson(StampsReferenceNumberNotFound)(using BusinessError.format) mustBe JsString("001")
      }

      "write VdsEmailNotFound to JsString(002)" in {
        Json.toJson(VdsEmailNotFound)(using BusinessError.format) mustBe JsString("002")
      }

      "write StampRequestedBefore to JsString(003)" in {
        Json.toJson(StampRequestedBefore)(using BusinessError.format) mustBe JsString("003")
      }

    }
  }
