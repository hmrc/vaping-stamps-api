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

import cats.data.ValidatedNec
import cats.implicits.*
import org.apache.pekko.Done
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Request
import uk.gov.hmrc.vapingstampsapi.models.ApprovalRequest
import uk.gov.hmrc.vapingstampsapi.models.errors.*

class RequestValidator extends Validator[ApprovalRequest]:
  override def fromRequest(request: Request[_]): ValidatedNec[BadRequestError, ApprovalRequest] =
    (
      validateAcceptHeader(request),
      validateAuthorizationHeader(request),
      validateStampsReferenceNumber(request),
      validateVdsEmail(request)
    ).mapN(
      (
        accept,
        authorization,
        srn,
        email
      ) => ApprovalRequest(email, srn)
    )

  private def validateAcceptHeader(request: Request[_]): ValidatedNec[BadRequestError, Done] = {
    val acceptHeader: String = "application/vnd.hmrc.1.0+json"
    request.headers.get("Accept") match {
      case Some(value) =>
        value match {
          case str if str == acceptHeader => Done.validNec
          case _                          => IncorrectAcceptHeader.invalidNec
        }
      case None => MissingAcceptHeader.invalidNec
    }
  }

  private def validateAuthorizationHeader(request: Request[_]): ValidatedNec[BadRequestError, Done] =
    request.headers.get("Authorization") match {
      case Some(value) =>
        value match {
          case str if str.startsWith("Bearer") || str.startsWith("GNAP dummy") => Done.validNec
          case _ => IncorrectAuthorizationHeader.invalidNec
        }
      case None => MissingAuthorizationHeader.invalidNec
    }

  private def validateStampsReferenceNumber(request: Request[_]): ValidatedNec[BadRequestError, String] =
    val stampsReferenceNumberRegex: String = "^GBVA[0-9]{7}DS$"

    request.body match {
      case JsObject(underlying) =>
        underlying.get("stampsReferenceNumber") match {
          case Some(JsString(value)) if value.matches(stampsReferenceNumberRegex) => value.validNec
          case Some(_) => InvalidStampsReferenceNumber.invalidNec
          case None    => MissingStampsReferenceNumber.invalidNec
        }
      case _ => InvalidStampsReferenceNumber.invalidNec
    }

  private def validateVdsEmail(request: Request[_]): ValidatedNec[BadRequestError, String] =
    val vdsEmailRegex: String = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"

    request.body match {
      case JsObject(underlying) =>
        underlying.get("vdsEmail") match {
          case Some(JsString(value)) if value.matches(vdsEmailRegex) => value.validNec
          case Some(_)                                               => InvalidVdsEmail.invalidNec
          case None                                                  => MissingVdsEmail.invalidNec
        }
      case _ => InvalidVdsEmail.invalidNec
    }
