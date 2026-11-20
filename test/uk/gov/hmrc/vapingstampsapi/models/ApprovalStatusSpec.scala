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
import play.api.libs.json.{JsNumber, JsResultException, JsString, Json}
import uk.gov.hmrc.vapingstampsapi.models.ApprovalStatus.*

class ApprovalStatusSpec extends AnyWordSpec with Matchers {

  "format" must {
    "read Approved status" in {
      JsString("approved").as[ApprovalStatus] mustBe approved
    }

    "read Not Approved status" in {
      JsString("not approved").as[ApprovalStatus] mustBe not_approved
    }

    "throw error on unrecognized status" in {

      intercept[JsResultException] {
        JsString("revoked").as[ApprovalStatus]
      }.getMessage mustBe "JsResultException(errors:List((,List(JsonValidationError(List(Unknown Status: revoked),ArraySeq())))))"
    }

    "throw error status" in {

      intercept[JsResultException] {
        JsNumber(1).as[ApprovalStatus]
      }.getMessage mustBe "JsResultException(errors:List((,List(JsonValidationError(List(Unexpected Enum),ArraySeq())))))"
    }

    "write approved.status enum" in {
      Json.toJson(approved) mustBe JsString("APPROVED")
    }

    "write not_approved.status enum" in {
      Json.toJson(not_approved) mustBe JsString("NOT_APPROVED")
    }
  }

}
