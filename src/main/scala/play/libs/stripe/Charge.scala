package play.libs.stripe

import play.api.libs.json.{JsNull, Json, JsObject}
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext


object ChargeStatus extends Enumeration {
  type Status = Value
  val Succeeded = Value("succeeded")
  val Failed = Value("failed")
}

case class CardSource(
                       number: String,
                       expMonth: Int,
                       expYear: Int,
                       cvc: String,
                       name: Option[String] = None,
                       addressLine1: Option[String] = None,
                       addressLine2: Option[String] = None,
                       addressCity: Option[String] = None,
                       addressZip: Option[String] = None,
                       addressState: Option[String] = None,
                       addressCountry: Option[String] = None
                       )

sealed case class ChargeRefundInfo(refunded: Boolean,
                                    refunds: CollectionContainer[Refund],
                                    amountRefunded: Option[Int])

sealed case class ChargeReferences(customer: Option[String], // TODO: use customer here
                                   invoice: Option[String], // TODO: use Invoice object here
                                   dispute: Option[String], // TODO: use Dispute object here
                                   balanceTransaction: Option[String],
                                   applicationFee: Option[String], // TODO: use Fee info here
                                   destination: Option[String], // TODO: use account object here
                                   transfer: Option[String] // TODO: use object here)
                                    )

sealed case class Charge(id: String,
                   livemode: Boolean,
                   amount: Int,
                   captured: Boolean,
                   created: DateTime,
                   currency: String,
                   paid: Boolean,
                   refundInfo: ChargeRefundInfo,
                   source: Card,
                   status: ChargeStatus.Status,
                   references: ChargeReferences,
                   description: Option[String],
                   failureCode: Option[ErrorCodes.Code],
                   failureMessage: Option[String],
                   metadata: Option[JsObject],
                   recipientEmail: Option[String],
                   receiptNumber: Option[String],
                   fraudDetails: Option[JsObject],
                   shippingInfo: Option[JsObject])

object Charge {

  def createWithCardSource(card: CardSource, amount: Int, currency: String, capture: Boolean,
               description: Option[String] = None, statementDescriptor: Option[String] = None, metadata: Option[JsObject] = None,
                      receiptEmail: Option[String] = None, destination: Option[String] = None, applicationFee: Option[Int] = None,
                      shipping: Option[JsObject] = None)(implicit client: StripeApi, ec: ExecutionContext) = {

    val params = Seq(
      "amount" -> amount.toString,
      "currency" -> currency,
      "capture" -> capture.toString,
      "description" -> description.getOrElse(""),
      "statement_descriptor" -> statementDescriptor.getOrElse(""),
      "receipt_email" -> receiptEmail.getOrElse(""),
      "destination" -> destination.getOrElse(""),
      "shipping" -> shipping.map(_.toString()).getOrElse("")
    ) ++ JsValueAsParam("metadata", metadata.getOrElse(JsNull)) ++
      JsValueAsParam("source", Json.toJson(card)(client.writesCardSource)) ++
      applicationFee.map(fee => Seq("application_fee" -> fee.toString)).getOrElse(Seq.empty[(String,String)])

    client.post[Charge]("/charges", params:_*)(client.readsCharge, ec)

  }

  /*
  def createWithCustomerSource() = {

  }
  */

  def retrieve(id: String)(implicit client: StripeApi, ec: ExecutionContext) = {
    client.get[Charge](s"/charges/$id")(client.readsCharge, ec)
  }

  def update(id: String, description: Option[String] = None, metadata: Option[JsObject] = None, receiptEmail: Option[String] =  None,
             fraudDetails: Option[JsObject] = None, shippingInfo: Option[JsObject] = None)(implicit client: StripeApi, ec: ExecutionContext) = {

    val params = Seq(
      "description" -> description.getOrElse(""),
      "receipt_email" -> receiptEmail.getOrElse(""),
      "shipping" -> shippingInfo.map(_.toString()).getOrElse("")
    ) ++ JsValueAsParam("metadata", metadata.getOrElse(JsNull)) ++
      JsValueAsParam("fraud_details", fraudDetails.getOrElse(JsNull))

    client.post[Charge](s"/charges/$id", params:_*)(client.readsCharge, ec)
  }


  def capture(id: String, amount: Option[Int] = None, applicationFee: Option[Int] = None, receiptEmail: Option[String] = None,
               statementDescriptor: Option[String] = None)(implicit client: StripeApi, ec: ExecutionContext) = {

    val params:Seq[(String,String)] = {
      Seq.empty ++ amount.map(d => Seq(("amount", d.toString))).getOrElse(Seq.empty) ++
        applicationFee.map(d => Seq(("application_fee", d.toString))).getOrElse(Seq.empty) ++
        receiptEmail.map(d => Seq(("receipt_email", d))).getOrElse(Seq.empty) ++
        statementDescriptor.map(d => Seq(("statement_descriptor", d))).getOrElse(Seq.empty)
    }

    client.post[Charge](s"/charges/$id/capture", params:_*)(client.readsCharge, ec)
  }

  def list(limit: Option[Int] = None, created: Option[JsObject] = None, customer: Option[String] = None,
           endingBefore: Option[String] = None, startingAfter: Option[String] = None, includeTotal: Boolean = true)(implicit client: StripeApi, ec: ExecutionContext) = {

    // TODO: add params
    /*
    implicit val r = client.readsCharge
    implicit val listRead =
    client.get[ListContainer[Charge]]("/charges")
    */
  }

}