package play.libs

/**
 * Created by hannes on 16.07.15.
 */
package object stripe {


  def JsValueAsParam(name: String, v: play.api.libs.json.JsValue): Seq[(String,String)] = v match {
      case o:play.api.libs.json.JsObject => o.fields.filterNot(_._2.eq(play.api.libs.json.JsNull)).map{el =>
        s"$name[${el._1}]" -> (el._2 match {
          case t:play.api.libs.json.JsString => t.value
          case other => other.toString()
        })
      }
      case o:play.api.libs.json.JsArray => o.value.filterNot(_.eq(play.api.libs.json.JsNull)).map{el =>
        "$name[]" -> (el match {
          case t:play.api.libs.json.JsString => t.value
          case other => other.toString()
        })
      }
      case play.api.libs.json.JsNull => Seq.empty
      case _ => Seq((name -> (v match {
        case t:play.api.libs.json.JsString => t.value
        case other => other.toString()
      })))
    }

  object CheckResults extends Enumeration {
    type Result = Value
    val Pass = Value("pass")
    val Fail = Value("fail")
    val Unavailable = Value("unavailable")
    val Unchecked = Value("unchecked")
  }

  object ErrorTypes extends Enumeration {
    type Error = Value
    val InvalidRequestError = Value("invalid_request_error")
    val ApiError = Value("api_error")
    val CardError = Value("card_error")
    val JsonError = Value("json_error")
  }

  object ErrorCodes extends Enumeration {
    type Code = Value
    val InvalidNumber = Value("invalid_number")
    val InvalidExpiryMonth = Value("invalid_expiry_month")
    val InvalidExpiryYear = Value("invalid_expiry_year")
    val InvalidCVC = Value("invalid_cvc")
    val IncorrectNumber = Value("incorrect_number")
    val ExpiredCard = Value("expired_card")
    val IncorrectCVC = Value("incorrect_cvc")
    val IncorrectZip = Value("incorrect_zip")
    val CardDeclined = Value("card_declined")
    val CardMissing = Value("missing")
    val ProcessingError = Value("processing_error")
    val RateLimit = Value("rate_limit")
  }


  case class CollectionContainer[T](data: Seq[T] = Seq.empty, url: Option[String] = None, hasMore: Boolean = false)

  case class FailureInfo(`type`: ErrorTypes.Error, message: Option[String] = None, code: Option[ErrorCodes.Code] = None, param: Option[String] = None)
}
