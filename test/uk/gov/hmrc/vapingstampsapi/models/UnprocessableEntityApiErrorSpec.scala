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
import play.api.libs.json.{JsResultException, Json, JsonValidationError}
import uk.gov.hmrc.vapingstampsapi.models.errors.{UnprocessableEntityApiError, VdsEmailNotFound}

class UnprocessableEntityApiErrorSpec extends AnyWordSpec with Matchers:

  "return UnprocessableEntityApiError" when {
    "JSON is correct format" in {
      val json = Json.parse(
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "422",
          |    "errorMessage": "Business Error",
          |    "source": "Backend",
          |    "sourceFaultDetail": {
          |      "detail": [
          |        "002"
          |      ]
          |    },
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          |  }
          |}
          |""".stripMargin
      )

      json.as[UnprocessableEntityApiError] mustBe UnprocessableEntityApiError(errors = Seq(VdsEmailNotFound))
    }
  }

  "return JsError" when {
    "errorDetail not present" in {
      val json = Json.parse("{}")
      intercept[JsResultException] {
        json.as[UnprocessableEntityApiError]
      }.errors
        .map(_._2 mustBe List(JsonValidationError(List("'errorDetail' is undefined on object. Available keys are ''"))))
    }

    "sourceFaultDetail not present" in {
      val json = Json.parse(
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "422",
          |    "errorMessage": "Business Error",
          |    "source": "Backend",
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          |  }
          |}
          |""".stripMargin
      )

      intercept[JsResultException] {
        json.as[UnprocessableEntityApiError]
      }.errors.map(
        _._2 mustBe List(
          JsonValidationError(
            List(
              "'sourceFaultDetail' is undefined on object. Available keys are 'errorCode', 'errorMessage', 'source', 'timestamp', 'correlationId'"
            )
          )
        )
      )
    }

    "sourceFaultDetail not correct type" in {
      val json = Json.parse(
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "422",
          |    "errorMessage": "Business Error",
          |    "source": "Backend",
          |    "sourceFaultDetail": "002",
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          |  }
          |}
          |""".stripMargin
      )

      intercept[JsResultException] {
        json.as[UnprocessableEntityApiError]
      }.errors.map(_._2 mustBe List(JsonValidationError(List(""""002" is not an object"""))))
    }

    "detail not present" in {
      val json = Json.parse(
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "422",
          |    "errorMessage": "Business Error",
          |    "source": "Backend",
          |    "sourceFaultDetail": {},
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          |  }
          |}
          |""".stripMargin
      )

      intercept[JsResultException] {
        json.as[UnprocessableEntityApiError]
      }.errors
        .map(_._2 mustBe List(JsonValidationError(List("'detail' is undefined on object. Available keys are ''"))))
    }

    "detail not expected type" in {
      val json = Json.parse(
        """
          |{
          |  "errorDetail": {
          |    "errorCode": "422",
          |    "errorMessage": "Business Error",
          |    "source": "Backend",
          |    "sourceFaultDetail": {
          |      "detail": "002"
          |    },
          |    "timestamp": "2026-04-14T10:54:12Z",
          |    "correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e"
          |  }
          |}
          |""".stripMargin
      )

      intercept[JsResultException] {
        json.as[UnprocessableEntityApiError]
      }.errors.map(_._2 mustBe List(JsonValidationError(List("error.expected.jsarray"))))
    }
  }
