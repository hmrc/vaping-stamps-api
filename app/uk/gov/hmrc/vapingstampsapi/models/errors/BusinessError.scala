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
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

trait BusinessError
object BusinessError extends Logging:
  given format: Format[BusinessError] = Format(
    Reads {
      case JsString("001") => JsSuccess(StampsReferenceNumberNotFound)
      case JsString("002") => JsSuccess(VdsEmailNotFound)
      case _               =>
        logger.error("[BusinessError][format]: Invalid BusinessError value")
        JsError("Invalid BusinessError value")
    },
    Writes {
      case StampsReferenceNumberNotFound => JsString(StampsReferenceNumberNotFound.toString)
      case VdsEmailNotFound              => JsString(VdsEmailNotFound.toString)
    }
  )

case object StampsReferenceNumberNotFound extends BusinessError:
  override def toString: String = "001"

case object VdsEmailNotFound extends BusinessError:
  override def toString: String = "002"
