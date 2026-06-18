package uk.gov.hmrc.vapingstampsapi.models.errors

import play.api.libs.json.{Format, JsString, JsSuccess, Reads, Writes}

trait BusinessError
object BusinessError:
  given format: Format[BusinessError] = Format(
    Reads{
      case JsString("001") => JsSuccess(StampsReferenceNumberNotFound)
      case JsString("002") => JsSuccess(VdsEmailNotFound)
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