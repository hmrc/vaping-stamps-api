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

import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status.{NO_CONTENT, OK, UNPROCESSABLE_ENTITY}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.vapingstampsapi.models.ApprovalSummaryResponse
import uk.gov.hmrc.vapingstampsapi.models.errors.{ApiError, InternalServerErrorApiError, ServiceUnavailableApiError, UnprocessableEntityApiError}

object EISParser extends Logging {
  type EISResponse = Either[ApiError, ApprovalSummaryResponse]
  type EISDeleteResponse = Either[ApiError, Done]

  implicit object EISResponseReads extends HttpReads[EISResponse] {

    override def read(method: String, url: String, response: HttpResponse): EISResponse =
      response.status match {
        case OK =>
          response.json
            .validate[ApprovalSummaryResponse]
            .fold(
              errors =>
                logger.error("[EISParser][read]: Unable to parse response as ApprovalSummary")
                Left(InternalServerErrorApiError(message = "Success response received invalid JSON response"))
              ,
              Right(_)
            )
        case UNPROCESSABLE_ENTITY =>
          response.json
            .validate[UnprocessableEntityApiError]
            .fold(
              errors =>
                logger.error("[EISParser][read]: Unable to parse response as UnprocessableEntityApiError")
                Left(InternalServerErrorApiError(message = "Business error response received invalid JSON response"))
              ,
              businessError =>
                logger.warn(s"[EISParser][read]: Response received business error. errors: ${businessError.errors}")
                Left(businessError)
            )
        case downstreamStatusCode =>
          logger.warn(s"[EISParser][read]: EIS has returned error statusCode: $downstreamStatusCode")
          Left(ServiceUnavailableApiError(message = "Error has occurred in downstream service"))
      }
  }

  implicit object EISDeleteResponseReads extends HttpReads[EISDeleteResponse] {

    override def read(method: String, url: String, response: HttpResponse): EISDeleteResponse =
      response.status match {
        case NO_CONTENT => Right(Done)
        case statusCode =>
          logger.error(s"[EISParser][deleteRecord]: EIS returned error statusCode: $statusCode, body: ${response.body}")
          Left(InternalServerErrorApiError(message = "Error occurred deleting record"))
      }
  }
}
