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

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vapingstampsapi.models.errors.*
import uk.gov.hmrc.vapingstampsapi.services.ApprovalService

import javax.inject.*
import scala.concurrent.*

@Singleton
class ApprovalController @Inject() (
  cc: ControllerComponents,
  service: ApprovalService
)(using ec: ExecutionContext)
    extends AbstractController(cc):

  lazy val logger: Logger = LoggerFactory.getLogger("approval-controller")
  given HeaderCarrier = HeaderCarrier()

  def retrieveSummary(vdsApprovalId: String): Action[AnyContent] =
    Action.async:
      service.retrieveSummary(vdsApprovalId).map {
        case Right(summary) =>
          Ok(Json.toJson(summary))
        case Left(EisApiError(status, _)) =>
          logger.error(s"Downstream service error. Status $status ")
          Status(status)(Json.obj("message" -> "Downstream service error"))
        case Left(_) => InternalServerError
      }
