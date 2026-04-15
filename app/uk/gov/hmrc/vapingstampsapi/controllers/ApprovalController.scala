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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.vapingstampsapi.controllers.actions.AuthAction
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummaryResponse}
import uk.gov.hmrc.vapingstampsapi.services.ApprovalService

import javax.inject.*
import scala.concurrent.*

@Singleton
class ApprovalController @Inject() (
  val authConnector: AuthConnector,
  authorise: AuthAction,
  cc: ControllerComponents,
  service: ApprovalService
)(using ec: ExecutionContext)
    extends AbstractController(cc) with AuthorisedFunctions with Logging:

  def retrieveSummary(
    vdsApprovalId: String
  ): Action[AnyContent] =
    authorise.async:
      implicit request: RequestHeader =>
        given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

        logger.info("[retrieveSummary]: authorisation successful")
        service.retrieveSummary(vdsApprovalId).map {
          case Right(summary)                => Ok(Json.toJson(summary))
          case Left(HttpResponse(204, _, _)) => NoContent
          case Left(response)                =>
            logger.warn(s"[retrieveSummary][EIS API Error] Service Unavailable: ${response.status} - ${response.body}")
            Status(response.status)(response.json)
        }

  def retrieveStatus(): Action[JsValue] =
    authorise.async(parse.json):
      implicit request: Request[JsValue] =>
        given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

        request.body
          .validate[ApprovalRequest]
          .fold(
            _ => {
              logger.error(s"The request payload is invalid or malformed.");
              Future.successful(
                BadRequest(
                  Json.obj("code" -> "INVALID_REQUEST", "message" -> "The request payload is invalid or malformed.")
                )
              )
            },
            approvalRequest =>
              logger.info("[retrieveStatus]: authorisation successful")
              processRequest(approvalRequest)
          )

  private def processRequest(approvalRequest: ApprovalRequest)(using HeaderCarrier): Future[Result] =
    service.retrieveStatus(approvalRequest).map {
      case Right(summary)                => Ok(Json.toJson(summary))
      case Left(HttpResponse(204, _, _)) => NoContent
      case Left(response)                =>
        logger.warn(s"[retrieveStatus][EIS API Error] Service Unavailable: ${response.status} - ${response.body}")
        Status(response.status)(response.json)
    }
