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

package uk.gov.hmrc.vapingstampsapi.services

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.vapingstampsapi.connectors.EISConnector
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummaryResponse}
import uk.gov.hmrc.vapingstampsapi.models.errors.*

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import javax.inject.*

@Singleton
class ApprovalService @Inject() (
  eisConnector: EISConnector
)(using ec: ExecutionContext)
    extends Logging:

  def retrieveSummary(
    vdsApprovalId: String
  )(using hc: HeaderCarrier): Future[Either[ApprovalError, ApprovalSummaryResponse]] =

    eisConnector
      .retrieveSummary(vdsApprovalId)
      .map { response =>
        response.status match
          case 200 =>
            Json
              .parse(response.body)
              .validate[ApprovalSummaryResponse]
              .asEither
              .left
              .map(_ =>
                logger.warn("Invalid JSON from downstream")
                EisApiError(
                  500,
                  "Invalid JSON from downstream"
                )
              )

          case status =>
            logger.info("STATUS" + status + "::" + response.body)
            Left(EisApiError(status, response.body))
      }
      .recover:
        case e: UpstreamErrorResponse =>
          Left(EisApiError(e.statusCode, e.message))

        case NonFatal(ex) =>
          logger.warn(
            s" Downstream EIS unreachable. Cause: ${ex.getMessage}"
          )
          Left(EisApiError(503, "Downstream service unavailable"))

  def retrieveStatus(
    request: ApprovalRequest
  )(using hc: HeaderCarrier): Future[Either[HttpResponse, ApprovalSummaryResponse]] =

    eisConnector
      .retrieveStatus(request)
      .recover:
        case e: UpstreamErrorResponse =>
          Left(HttpResponse(e.statusCode, e.message))

        case NonFatal(ex) =>
          logger.warn(
            s"Downstream EIS unreachable. Cause: ${ex.getMessage}"
          )
          Left(HttpResponse(503, "Downstream service unavailable"))
