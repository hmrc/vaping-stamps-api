package uk.gov.hmrc.vapingstampsapi.models.errors

import play.api.Logging
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

trait BusinessError
object BusinessError extends Logging:
  given format: Format[BusinessError] = Format(
    Reads{
      case JsString("001") => JsSuccess(StampsReferenceNumberNotFound)
      case JsString("002") => JsSuccess(VdsEmailNotFound)
      case _ =>
        logger.error("[BusinessError][format]: Invalid BusinessError value")
        JsError("Invalid BusinessError value")
    },
    Writes{
      case StampsReferenceNumberNotFound => JsString(StampsReferenceNumberNotFound.toString)
      case VdsEmailNotFound => JsString(VdsEmailNotFound.toString)
    }
  )
    
case object StampsReferenceNumberNotFound extends BusinessError:
  override def toString: String = "001"

case object VdsEmailNotFound extends BusinessError:
  override def toString: String = "002"