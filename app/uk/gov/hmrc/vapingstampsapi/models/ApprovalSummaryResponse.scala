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

import play.api.libs.json.{Json, OFormat}

final case class ApprovalSummaryResponse(
  approvalStatus: ApprovalStatus,
  businessName: String,
  addressLine1: String,
  addressLine2: Option[String] = None,
  addressLine3: Option[String] = None,
  addressLine4: Option[String] = None,
  addressLine5: Option[String] = None,
  postCode: String,
  contactName: Option[String] = None,
  telephoneNumber: Option[String] = None,
  stampsThreshold: Long
) {
  def toEnrichedApprovalSummary(email: String): EnrichedApprovalSummary =
    EnrichedApprovalSummary(
      approvalStatus,
      businessName,
      addressLine1,
      addressLine2,
      addressLine3,
      addressLine4,
      addressLine5,
      postCode,
      email,
      contactName,
      telephoneNumber,
      stampsThreshold
    )
}

object ApprovalSummaryResponse {
  given OFormat[ApprovalSummaryResponse] = Json.format[ApprovalSummaryResponse]
}
