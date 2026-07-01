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

import play.api.libs.json.*

enum ApprovalStatus:
  case approved, not_approved

object ApprovalStatus:
  given Format[ApprovalStatus] = Format(
    Reads {
      case JsString("approved")     => JsSuccess(ApprovalStatus.approved)
      case JsString("not approved") => JsSuccess(ApprovalStatus.not_approved)
      case JsString(other)          => JsError(s"Unknown Status: $other")
      case _                        => JsError("Unexpected Enum")
    },
    Writes {
      case ApprovalStatus.`approved`     => JsString("APPROVED")
      case ApprovalStatus.`not_approved` => JsString("NOT_APPROVED")
    }
  )
