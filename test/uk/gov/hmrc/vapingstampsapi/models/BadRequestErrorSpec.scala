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
import uk.gov.hmrc.vapingstampsapi.models.errors.*

class BadRequestErrorSpec extends AnyWordSpec with Matchers:
  "BadRequestErrors" should {
    "toString" must
      Seq(
        MissingAcceptHeader          -> "001",
        IncorrectAcceptHeader        -> "002",
        MissingContentTypeHeader     -> "003",
        IncorrectContentTypeHeader   -> "004",
        MissingStampsReferenceNumber -> "005",
        InvalidStampsReferenceNumber -> "006",
        MissingVdsEmail              -> "007",
        InvalidVdsEmail              -> "008",
        TooLongVdsEmail              -> "009"
      ).foreach { case (error, str) =>
        s"Convert $error to $str" in {
          error.toString mustBe str
        }
      }
  }
