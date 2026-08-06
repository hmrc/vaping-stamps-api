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

import cats.data.EitherT
import org.apache.pekko.Done
import org.playframework.cachecontrol.HttpDate
import org.slf4j.LoggerFactory
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.vapingstampsapi.config.AppConfig
import uk.gov.hmrc.vapingstampsapi.connectors.parsers.EISParser.{EISDeleteResponse, EISDeleteResponseReads, EISResponse, EISResponseReads}
import uk.gov.hmrc.vapingstampsapi.models.errors.{ApiError, ServiceUnavailableApiError}
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalSummaryResponse, VDSDetails}

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID.randomUUID
import javax.inject.*
import scala.concurrent.*

@Singleton
class EISConnector @Inject() (
  http: HttpClientV2,
  appConfig: AppConfig
)(using ec: ExecutionContext):

  private val logger = LoggerFactory.getLogger(getClass)

  def retrieveStatus(
    request: VDSDetails
  )(using hc: HeaderCarrier): EitherT[Future, ApiError, ApprovalSummaryResponse] =

    val url = s"${appConfig.eisBaseUrl}/excise/decision/vapingstamps/profile/v1"

    logger.info(s"[EISConnector][retrieveStatus] called: $url")

    EitherT(
      http
        .post(url"$url")
        .setHeader(
          "Authorization" -> s"Bearer ${appConfig.eisAuthToken}",
          "content-type"  -> "application/json",
          "Accept"        -> "application/json",
          "date"          -> HttpDate.now.format(
            DateTimeFormatter.ofPattern("EEE, dd MMM YYYY HH:mm:ss z", Locale.UK).withZone(ZoneId.of("GMT"))
          ),
          "x-correlation-id" -> randomUUID().toString,
          "x-forwarded-host" -> "MDTP"
        )
        .withBody(Json.toJson(request))
        .execute[EISResponse]
        .recover { case e: HttpException =>
          logger.error(s"[EISConnector][retrieveStatus]: Unexpected Exception returned - $e")
          Left(ServiceUnavailableApiError(message = e.message))
        }
    )

  def deleteRecord(
    stampsReferenceNumber: String
  )(using hc: HeaderCarrier): EitherT[Future, ApiError, Done] =

    val url = s"${appConfig.eisBaseUrl}/delete-record"

    logger.info(s"[EISConnector][deleteRecord] called: $url")

    EitherT(
      http
        .delete(url"$url")
        .setHeader(
          "Authorization" -> s"Bearer ${appConfig.eisDeleteAuthToken}",
          "content-type"  -> "application/json",
          "Accept"        -> "application/json",
          "date"          -> HttpDate.now.format(
            DateTimeFormatter.ofPattern("EEE, dd MMM YYYY HH:mm:ss z", Locale.UK).withZone(ZoneId.of("GMT"))
          ),
          "x-correlation-id" -> randomUUID().toString,
          "x-forwarded-host" -> "MDTP"
        )
        .execute[EISDeleteResponse]
        .recover { case e: HttpException =>
          logger.error(s"[EISConnector][deleteRecord]: Unexpected Exception returned - $e")
          Left(ServiceUnavailableApiError(message = e.message))
        }
    )
