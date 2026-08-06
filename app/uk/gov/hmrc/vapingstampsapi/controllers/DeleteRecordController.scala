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

package uk.gov.hmrc.vapingstampsapi.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.vapingstampsapi.controllers.actions.AuthAction
import uk.gov.hmrc.vapingstampsapi.models.errors.{BadRequestApiError, InternalServerErrorApiError, InvalidStampsReferenceNumber, ServiceUnavailableApiError}
import uk.gov.hmrc.vapingstampsapi.services.DeleteRecordService

import javax.inject.*
import scala.concurrent.*

@Singleton
class DeleteRecordController @Inject() (
  val authConnector: AuthConnector,
  authorise: AuthAction,
  cc: ControllerComponents,
  service: DeleteRecordService
)(using ec: ExecutionContext)
    extends AbstractController(cc) with AuthorisedFunctions with Logging:

  private val STAMPS_REFERENCE_NUMBER_REGEX = "^(GB|XI)V[CEFMR][0-9]{7}DS$"

  def deleteRecord(stampsReferenceNumber: String): Action[AnyContent] =
    authorise.async:
      implicit request: Request[AnyContent] =>
        given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

        validateStampsReferenceNumber(stampsReferenceNumber) match {
          case Some(error) =>
            logger.error(s"[DeleteRecordController][deleteRecord] Invalid stampsReferenceNumber: $stampsReferenceNumber")
            Future.successful(BadRequest(Json.toJson(error)))
          case None =>
            logger.info(s"[DeleteRecordController][deleteRecord] Deleting record for: $stampsReferenceNumber")
            service
              .deleteRecord(stampsReferenceNumber)
              .fold(
                {
                  case internalServerError: InternalServerErrorApiError =>
                    logger.error(s"[DeleteRecordController][deleteRecord] Error deleting record - code: ${internalServerError.code}, message: ${internalServerError.message}")
                    InternalServerError(Json.toJson(internalServerError))
                  case serviceUnavailableError: ServiceUnavailableApiError =>
                    logger.error(s"[DeleteRecordController][deleteRecord] Service unavailable - code: ${serviceUnavailableError.code}, message: ${serviceUnavailableError.message}")
                    InternalServerError(Json.toJson(serviceUnavailableError))
                },
                _ => {
                  logger.info(s"[DeleteRecordController][deleteRecord] Successfully deleted record: $stampsReferenceNumber")
                  NoContent
                }
              )
        }

  private def validateStampsReferenceNumber(srn: String): Option[BadRequestApiError] =
    if (!srn.matches(STAMPS_REFERENCE_NUMBER_REGEX)) {
      Some(
        BadRequestApiError(
          message = "Invalid stampsReferenceNumber format",
          errors = Seq(InvalidStampsReferenceNumber)
        )
      )
    } else None