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

import play.api.libs.json.{Json, Writes}

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

case class DownstreamApiError(code: String, message: String) extends ApiError
