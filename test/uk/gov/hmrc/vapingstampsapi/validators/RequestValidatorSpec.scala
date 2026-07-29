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

package uk.gov.hmrc.vapingstampsapi.validators

import cats.data.Chain
import cats.data.Validated.{Invalid, Valid}
import org.scalactic.Prettifier.default
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.vapingstampsapi.models.VDSDetails
import uk.gov.hmrc.vapingstampsapi.models.errors.*

class RequestValidatorSpec extends AnyWordSpec with Matchers:

  val validator = new RequestValidator

  "fromRequest" must {
    "Return Valid ApprovalSummaryRequest" when {
      "Valid request is made" in {

        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Valid(VDSDetails("example@email.com", "GBVA0000001DS"))
      }

      "Valid request is made with NI Reference" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "XIVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Valid(VDSDetails("example@email.com", "XIVA0000001DS"))
      }

      "Valid request is made with multiple possible characters" in {

        val validChar = Seq("A", "C", "E", "F", "M", "R")

        def detailsObject(letter: String) =
          Json.obj(
            "vdsEmail"              -> "example@email.com",
            "stampsReferenceNumber" -> s"XIV${letter}0000001DS"
          )

        def request(body: JsObject) = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            body
          )
        validChar.foreach { letter =>
          validator.fromRequest(request(detailsObject(letter))) mustBe Valid(
            VDSDetails("example@email.com", s"XIV${letter}0000001DS")
          )
        }
      }
    }

    "Return Invalid Non Empty Chain" when {
      "Accept header is missing" in {
        val request = FakeRequest()
          .withHeaders(
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(MissingAcceptHeader))
      }

      "Accept header is invalid" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(IncorrectAcceptHeader))
      }

      "Content-Type header is missing" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(MissingContentTypeHeader))
      }

      "Content-Type header is invalid" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/xml"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(IncorrectContentTypeHeader))
      }

      "vdsEmail is missing from JSON payload" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(MissingVdsEmail))
      }

      "vdsEmail is invalid email" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(InvalidVdsEmail))
      }

      "vdsEmail has too many chars in the email so returns invalid" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "12345678912345678912345678912345678912345678912345678912345678965@email.com",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(InvalidVdsEmail))
      }

      "vdsEmail has 132 chars in the email so returns valid" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail" -> "123456789123456789123456789123456789123456789123456789123456789@email.1234567890123456789012345678901234567890123456789012",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Valid(
          VDSDetails(
            "123456789123456789123456789123456789123456789123456789123456789@email.1234567890123456789012345678901234567890123456789012",
            "GBVA0000001DS"
          )
        )
      }

      "vdsEmail has too many Chars overall in the email so returns too long invalid" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail" -> "1234567891234567891234567891234567891234567891234567891234567890@1234567891234567891234567891234567891234567891234567891234567890@e.1234567891234567891234567891234567891234567891234567891234567890",
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(TooLongVdsEmail))
      }

      "vdsEmail is Invalid Json type" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> true,
              "stampsReferenceNumber" -> "GBVA0000001DS"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(InvalidVdsEmail))
      }

      "stampsReferenceNumber is missing from JSON payload" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail" -> "example@email.com"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(MissingStampsReferenceNumber))
      }

      "stampsReferenceNumber is invalid format" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> "GBVA0000001"
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(InvalidStampsReferenceNumber))
      }

      "stampsReferenceNumber is invalid json type" in {
        val request = FakeRequest()
          .withHeaders(
            "Accept"        -> "application/vnd.hmrc.1.0+json",
            "Authorization" -> "Bearer Token",
            "Content-Type"  -> "application/json"
          )
          .withBody(
            Json.obj(
              "vdsEmail"              -> "example@email.com",
              "stampsReferenceNumber" -> Seq(1, 2, 3, 4, 5, 6)
            )
          )

        validator.fromRequest(request) mustBe Invalid(Chain(InvalidStampsReferenceNumber))
      }
    }
  }
