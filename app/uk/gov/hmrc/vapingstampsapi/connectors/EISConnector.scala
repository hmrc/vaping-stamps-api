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
import org.slf4j.LoggerFactory
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.vapingstampsapi.config.AppConfig
import uk.gov.hmrc.vapingstampsapi.connectors.parsers.EISParser.{EISResponse, EISResponseReads}
import uk.gov.hmrc.vapingstampsapi.models.{ApprovalRequest, ApprovalSummaryResponse}

import javax.inject.*
import scala.concurrent.*

@Singleton
class EISConnector @Inject() (
  http: HttpClientV2,
  appConfig: AppConfig
)(using ec: ExecutionContext):

  private val logger = LoggerFactory.getLogger(getClass)

  def retrieveStatus(
    request: ApprovalRequest
  )(using hc: HeaderCarrier): EitherT[Future, HttpResponse, ApprovalSummaryResponse] =

    val url = s"${appConfig.eisBaseUrl}/etds/vaping/stamps/status"

    logger.info(s"[EISConnector][retrieveStatus] called: $url")

    EitherT(
      http
        .post(url"$url")
        .setHeader("Environment" -> appConfig.eisEnvironment)
        .setHeader("Authorization" -> s"Bearer ${appConfig.eisAuthToken}")
        .withBody(Json.toJson(request))
        .execute[EISResponse]
        .recover { case e: HttpException =>
          Left(HttpResponse(503, e.message))
        }
    )
