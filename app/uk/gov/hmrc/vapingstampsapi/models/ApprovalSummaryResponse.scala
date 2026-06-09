package uk.gov.hmrc.vapingstampsapi.models

import play.api.libs.json.*
import play.api.libs.json.JsPath.json
import play.mvc.BodyParser.Json
import uk.gov.hmrc.vapingstampsapi.models.ApprovalStatus.{Approved, Not_Approved}

sealed trait  ApprovalSummaryResponse {
  def approvalStatus: ApprovalStatus
}

//case res: JsObject if (res \ "approvalStatus").equals(ApprovalStatus.Approved)=> ApprovedSummaryResponse

object ApprovalSummaryResponse{
  given readFromJsString: Reads[ApprovalSummaryResponse] = Reads{
    case res if res.approvalStatu => JsSuccess(ApprovalSummaryResponse)
    case res:JsValue if (res \ "approvalStatus").equals(ApprovalStatus.Not_Approved) => JsSuccess(Not_Approved)
    case _ => JsError("Unexpected Json format")
  }
}

//final case class ApprovedSummaryResponse (
//                                           approvalStatus: ApprovalStatus,
//                                           businessName: String,
//                                           addressLine1: String,
//                                           addressLine2: Option[String] = None,
//                                           addressLine3: Option[String] = None,
//                                           addressLine4: Option[String] = None,
//                                           addressLine5: Option[String] = None,
//                                           postCode: String,
//                                           contactName: Option[String] = None,
//                                           telephoneNumber: Option[String] = None,
//                                           stampsThreshold: Long
//                                         ) extends ApprovalSummaryResponse {
//  def toEnrichedApprovedSummary(email: String): EnrichedApprovedSummary =
//    EnrichedApprovedSummary(
//      approvalStatus,
//      businessName,
//      addressLine1,
//      addressLine2,
//      addressLine3,
//      addressLine4,
//      addressLine5,
//      postCode,
//      email,
//      contactName,
//      telephoneNumber,
//      stampsThreshold
//    )
//}
//
//object ApprovedSummaryResponse {
//  given OFormat[ApprovedSummaryResponse] = Json.format[ApprovedSummaryResponse]
//}

//case class EnrichedApprovedSummary(
//                                    approvalStatus: ApprovalStatus,
//                                    businessName: String,
//                                    addressLine1: String,
//                                    addressLine2: Option[String],
//                                    addressLine3: Option[String],
//                                    addressLine4: Option[String],
//                                    addressLine5: Option[String],
//                                    postCode: String,
//                                    contactEmail: String,
//                                    contactName: Option[String],
//                                    telephoneNumber: Option[String],
//                                    stampsThreshold: Long
//                                  ) extends ApprovalSummaryResponse
//
//object EnrichedApprovedSummary {
//  given writes: Writes[EnrichedApprovedSummary] = Json.writes[EnrichedApprovedSummary]
//}
//
//final case class NotApprovedSummaryResponse(approvalStatus: ApprovalStatus) extends ApprovalSummaryResponse
//
//object NotApprovedSummaryResponse {
//  given OFormat[NotApprovedSummaryResponse] = Json.format[NotApprovedSummaryResponse]
//}

//  given responseSelector: response[JsObject] with {
//    override def compare(js: JsObject):ApprovalSummaryResponse
//
//    match
//    {
//      case js.validate[ApprovalSummaryResponse
//      ] => ApprovalSummaryResponse
//      case js.validate[NotApprovedSummaryResponse]
//      => NotApprovedSummaryResponse
//      case _ => JsError("Unrecognised Json Format")
//    }
//  }

  //  Reads[ApprovalSummaryResponse] = new Reads[ApprovalSummaryResponse]
  //
  //  def reads(js: JsObject): JsResult[ApprovalSummary] = js.validate[JsObject]
  //}

  //implicit val emailAddressReads: Reads[EmailAddress] = new Reads[EmailAddress] {
  //  def reads(js: JsValue): JsResult[EmailAddress] = js.validate[String].flatMap {
  //    case s if EmailAddress.isValid(s) => JsSuccess(EmailAddress(s))
  //    case _ => JsError("not a valid email address")
  //  }
  //}

//  override def canEqual(that: Any): Boolean = super.canEqual(that)

//  override def equals(obj: JsObject): Boolean = super.equals(obj)

