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

sealed trait ApprovalSummaryResponse {
  def approvalStatus: ApprovalStatus
}

object ApprovalSummaryResponse {
  given readFromJson: Format[ApprovalSummaryResponse] = Format(
    Reads {
      case json @ JsObject(underlying) =>
        underlying.get("approvalStatus") match {
          case Some(JsString("APPROVED"))     => JsSuccess(json.as[ApprovedSummaryResponse])
          case Some(JsString("NOT_APPROVED")) => JsSuccess(json.as[NotApprovedSummaryResponse])
          case Some(other)                    => JsError("Unexpected approvalStatus")
          case None                           => JsError("Missing approvalStatus in ResponseBody")
        }
      case _ => JsError("Unexpected Json format")
    },
    Writes {
      case approvedSummaryResponse: ApprovedSummaryResponse       => Json.toJson(approvedSummaryResponse)
      case notApprovedSummaryResponse: NotApprovedSummaryResponse => Json.toJson(notApprovedSummaryResponse)
    }
  )
}

final case class ApprovedSummaryResponse(
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
) extends ApprovalSummaryResponse

object ApprovedSummaryResponse {
  given OFormat[ApprovedSummaryResponse] = Json.format[ApprovedSummaryResponse]
}

final case class NotApprovedSummaryResponse(approvalStatus: ApprovalStatus) extends ApprovalSummaryResponse

object NotApprovedSummaryResponse {
  given OFormat[NotApprovedSummaryResponse] = Json.format[NotApprovedSummaryResponse]
}
