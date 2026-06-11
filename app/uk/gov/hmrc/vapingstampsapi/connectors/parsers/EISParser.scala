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

package uk.gov.hmrc.vapingstampsapi.connectors.parsers

import play.api.http.Status.OK
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.Format.GenericFormat
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.models.ApprovalSummaryResponse

object EISParser {
  type EISResponse = Either[HttpResponse, ApprovalSummaryResponse]

  implicit object EISResponseReads extends HttpReads[EISResponse] {

    override def read(method: String, url: String, response: HttpResponse): EISResponse =
      response.status match {
        case OK =>
          response.json
            .validate[ApprovalSummaryResponse]
            .fold(
              errors => Left(HttpResponse(500, "Received invalid response")),
              summaryResponse => Right(summaryResponse)
            )
        case _ => Left(response)
      }
  }
}
