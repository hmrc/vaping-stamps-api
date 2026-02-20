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

import javax.inject.*
import scala.concurrent.*

@Singleton
class EISConnector @Inject() (
  http: HttpClientV2,
  appConfig: AppConfig
)(using ec: ExecutionContext):

  private val logger = LoggerFactory.getLogger(getClass)

  def retrieveSummary(
    vdsApprovalId: String
  )(using hc: HeaderCarrier): Future[HttpResponse] =

    val url =
      s"${appConfig.eisBaseUrl}/etds/vaping/stamps/$vdsApprovalId/status"

    http
      .get(url"$url")
      .setHeader("Environment" -> appConfig.eisEnvironment)
      .setHeader("Authorization" -> s"Bearer ${appConfig.eisAuthToken}")
      .execute[HttpResponse](using HttpReads.Implicits.readRaw)
