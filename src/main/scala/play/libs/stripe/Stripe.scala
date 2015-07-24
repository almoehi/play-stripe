package play.libs.stripe
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._

import play.api.libs.ws.WSAuthScheme
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

trait StripeApi {
  val ApiKey: String
  val ApiBase: String
  val BindingsVersion: String

  implicit val readsCard: Reads[Card]
  implicit val readsRefund: Reads[Refund]
  implicit val readsCharge: Reads[Charge]
  implicit val writesCardSource: Writes[CardSource]

  implicit def readsCollection[T](implicit dataReads: Reads[T]): Reads[CollectionContainer[T]]

  implicit val readsErrorInfo: Reads[FailureInfo] = (
      (__ \ "error" \ "type").read[String].map(ErrorTypes.withName(_)) and
      (__ \ "error" \ "message").readNullable[String] and
      (__ \ "error" \ "code").readNullable[String].map(_.map(ErrorCodes.withName(_))) and
      (__ \ "error" \ "param").readNullable[String]
    )(FailureInfo.apply _)

  // TODO: writes / reads for other STRIPE entities

  // TODO: support Stripe-Version header

  val client = {
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
    new play.api.libs.ws.ning.NingWSClient(builder.build())
  }

  private def prepareRequest(url: String) = client.url(url).withAuth(ApiKey, "", WSAuthScheme.BASIC).withHeaders(
    "Idempotency-Key" -> UUID.randomUUID().toString
  ).withRequestTimeout(30000)

  def post[T](url: String, params: (String,String)*)(implicit r:Reads[T], ec: ExecutionContext): Future[Either[FailureInfo,T]] = prepareRequest(s"$ApiBase$url").withQueryString(params.filter(_._2.nonEmpty):_*).execute("POST").map{response =>
    println(params.filter(_._2.nonEmpty).mkString("\n"))

    val curl = s"curl https://api.stripe.com/v1/charges -u $ApiKey: " + params.filter(_._2.nonEmpty).map(p => s"-d ${p._1}='${p._2}'").mkString(" ")
    println(curl)

    response.json.validate[T](r) match {
      case s: JsSuccess[T] => Right(s.get)
      case primaryError: JsError => response.json.validate[FailureInfo] match {
        case info:JsSuccess[FailureInfo] => Left(info.get)
        case err: JsError =>
          println(Json.prettyPrint(response.json))
          Left(FailureInfo(ErrorTypes.JsonError, Some(JsError.toFlatJson(primaryError).toString())))
      }

    }
  }

  def get[T](url: String, params: (String,String)*)(implicit r:Reads[T], ec: ExecutionContext): Future[Either[FailureInfo,T]] = prepareRequest(s"$ApiBase$url").withQueryString(params.filter(_._2.nonEmpty):_*).get().map{response =>
    println(params.filter(_._2.nonEmpty).mkString("\n"))
    response.json.validate[T](r) match {
      case s: JsSuccess[T] => Right(s.get)
      case primaryError: JsError => response.json.validate[FailureInfo] match {
        case info:JsSuccess[FailureInfo] => Left(info.get)
        case err: JsError =>
          println(Json.prettyPrint(response.json))
          Left(FailureInfo(ErrorTypes.JsonError, Some(JsError.toFlatJson(primaryError).toString())))
      }
    }
  }

  // POST header: Idempotency-Key
  // response: Request-Id

}

class StripeV112(key: Option[String] = None) extends StripeApi {
  val BindingsVersion = "1.1.2"
  val ApiBase = "https://api.stripe.com/v1"

  val readsCard = formats.v112.JsonFormats.cardReads
  val readsRefund = formats.v112.JsonFormats.refundReads
  val readsCharge = formats.v112.JsonFormats.chargeReads
  val writesCardSource = formats.v112.JsonFormats.cardSourceWrites

  implicit def readsCollection[T](implicit dataReads: Reads[T]): Reads[CollectionContainer[T]] = formats.v112.JsonFormats.collectionReads(dataReads)

  val lines = scala.io.Source.fromFile("credentials.properties").getLines()

  def getProperty(name: String): Option[String] = lines.find(_.startsWith(s"$name=")).map(_.replace(s"$name=", ""))

  val ApiKey = {
    key.orElse(getProperty("apiKey")).getOrElse("")
  }
}
