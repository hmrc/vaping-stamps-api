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

package uk.gov.hmrc.vapingstampsapi.models.errors

import play.api.libs.json.*

//sealed trait ApprovalStatus {
//  def message: String
//}
//
//case object Approved extends ApprovalStatus {
//  override val message: String = "APPROVED"
//}
//
//case object Not_Approved extends ApprovalStatus {
//  override val message: String = "NOT_APPROVED"
//}

//object ApprovalStatus {
//  implicit val format: OFormat[ApprovalStatus] = {
//    implicit def jsf = Json.format[Approved.message]
//    implicit def ivf = Json.format[Not_Approved]
//
//    Json.format[ApprovalStatus]
//  }
//}

//object ApprovalStatus {
//  def apply(participant: String): ApprovalStatus = participant match {
//    case Approved.message => Approved
//    case Not_Approved.message => Not_Approved
//    case _ => throw InternalError() //decide what error to throw
//  }
//}

enum ApprovalStatus {
  case Approved
  case Not_Approved
}

object ApprovalStatus:
  given Format[ApprovalStatus] = Format(
    Reads {
      case JsString("APPROVED")     => JsSuccess(ApprovalStatus.Approved)
      case JsString("NOT_APPROVED") => JsSuccess(ApprovalStatus.Not_Approved)
      case _                        => JsError("There will be a better error here")
    },
    Writes {
      case ApprovalStatus.Approved     => JsString("APPROVED")
      case ApprovalStatus.Not_Approved => JsString("NOT_APPROVED")
    }
  )

//    Reads {
//      case JsString(value) =>
//        ApprovalStatus.values
//          .find(_.toString == value)
//          .map(JsSuccess(_))
//          .getOrElse(JsError(""))
//      case _ => JsError("Can put a better error here")
//    },
//    Writes(as => JsString(as.toString))
//  )
