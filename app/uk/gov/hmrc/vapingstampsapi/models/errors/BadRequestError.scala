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

import play.api.libs.json.{JsString, Writes}

trait BadRequestError
object BadRequestError:
  given writes: Writes[BadRequestError] =
    Writes: badRequestError =>
      JsString(badRequestError.toString)

case object MissingAcceptHeader extends BadRequestError:
  override def toString: String = "001"

case object IncorrectAcceptHeader extends BadRequestError:
  override def toString: String = "002"

case object MissingContentTypeHeader extends BadRequestError:
  override def toString: String = "003"

case object IncorrectContentTypeHeader extends BadRequestError:
  override def toString: String = "004"

case object MissingStampsReferenceNumber extends BadRequestError:
  override def toString: String = "005"

case object InvalidStampsReferenceNumber extends BadRequestError:
  override def toString: String = "006"

case object MissingVdsEmail extends BadRequestError:
  override def toString: String = "007"

case object InvalidVdsEmail extends BadRequestError:
  override def toString: String = "008"

case object TooManyCharsInVdsEmail extends BadRequestError:
  override def toString: String = "009"
