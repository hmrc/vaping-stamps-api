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

import play.api.libs.json.{Format, JsString, Json, OFormat, Reads, Writes}
import uk.gov.hmrc.vapingstampsapi.models.errors.ApprovalStatus

final case class NotApprovedSummaryResponse(
  approvalStatus: ApprovalStatus
) {
  def toEnrichedNotApprovedSummary(email: String): EnrichedNotApprovedSummary =
    EnrichedNotApprovedSummary(
      approvalStatus,
      email
    )
}

object NotApprovedSummaryResponse {
  given OFormat[NotApprovedSummaryResponse] = Json.format[NotApprovedSummaryResponse]
}
