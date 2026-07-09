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

package uk.gov.hmrc.vapingstampsapi.models.errors

import play.api.Logging
import play.api.libs.json.*

sealed trait ApiError:
  def code: String
  def message: String

case class BadRequestApiError(
  code: String = "BAD_REQUEST",
  message: String = "The request is invalid",
  errors: Seq[BadRequestError]
) extends ApiError

object BadRequestApiError:
  given writes: Writes[BadRequestApiError] = Json.writes[BadRequestApiError]

case class UnprocessableEntityApiError(
  code: String = "UNPROCESSABLE_ENTITY",
  message: String = "The request has returned a business logic error.",
  errors: Seq[BusinessError]
) extends ApiError

object UnprocessableEntityApiError extends Logging:
  given format: Format[UnprocessableEntityApiError] = Format(
    reads,
    Json.writes[UnprocessableEntityApiError]
  )

  given reads: Reads[UnprocessableEntityApiError] =
    Reads {
      case obj: JsObject =>
        (obj \ "errorDetail" \ "sourceFaultDetail" \ "detail")
          .validate[Seq[BusinessError]]
          .map(errorList => UnprocessableEntityApiError(errors = errorList))
      case e =>
        logger.error(s"[UnprocessableEntityApiError][format]: Unprocessable Entity JSON body error: $e")
        JsError("Unprocessable Entity response JSON has produced an error at parsing")
    }

case class InternalServerErrorApiError(code: String = "INTERNAL_SERVER_ERROR", message: String) extends ApiError

object InternalServerErrorApiError:
  given writes: Writes[InternalServerErrorApiError] = Json.writes[InternalServerErrorApiError]

case class ServiceUnavailableApiError(code: String = "SERVICE_UNAVAILABLE", message: String) extends ApiError

object ServiceUnavailableApiError:
  given writes: Writes[ServiceUnavailableApiError] = Json.writes[ServiceUnavailableApiError]
