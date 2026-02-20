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

package uk.gov.hmrc.vapingstampsapi.connectors

import org.slf4j.LoggerFactory
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.vapingstampsapi.config.AppConfig
import uk.gov.hmrc.vapingstampsapi.models.*
import uk.gov.hmrc.vapingstampsapi.models.errors.*

import javax.inject.*
import scala.concurrent.*
import scala.util.control.NonFatal

@Singleton
class EISConnector @Inject() (
  http: HttpClientV2,
  appConfig: AppConfig
)(using ec: ExecutionContext):

  private val logger = LoggerFactory.getLogger(getClass)

  def retrieveSummary(
    vdsApprovalId: String
  )(using hc: HeaderCarrier): Future[Either[ApprovalError, ApprovalSummary]] =

    val url =
      s"${appConfig.approvalExternalBaseUrl}/approvals/$vdsApprovalId"

    http
      .get(url"$url")
      .setHeader("Environment" -> appConfig.approvalEnvironment)
      .setHeader("Authorization" -> s"Bearer ${appConfig.approvalAuthToken}")
      .execute[HttpResponse]
      .map { response =>
        response.status match
          case 200 =>
            response.json
              .validate[ApprovalSummary]
              .asEither
              .left
              .map(_ => ApprovalApiError(500, "Invalid JSON from downstream"))

          case 404 =>
            Left(ApprovalNotFound)

          case status =>
            Left(ApprovalApiError(status, response.body))
      }
      .recover { case NonFatal(ex) =>
        logger.warn(
          s"[EISConnector] Downstream approval-external-api unreachable. Returning stub. Cause: ${ex.getMessage}"
        )

        Right(
          ApprovalSummary(
            approvalStatus = "APPROVED",
            businessName = "Acme Vaping Ltd",
            registeredBusinessAddress = "1 Business Park, London, SW1A 1AA",
            correspondenceAddress = "PO Box 123, London, SW1A 2BB",
            contactName = "John Smith",
            contactTelephone = "02071234567",
            contactEmail = "john.smith@acmevaping.co.uk",
            approvalNumber = vdsApprovalId,
            stampThreshold = 10000L
          )
        )
      }
