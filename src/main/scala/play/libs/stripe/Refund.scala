package play.libs.stripe

import org.joda.time.DateTime
import play.api.libs.json.{JsNull, JsObject}
import scala.concurrent.ExecutionContext


sealed case class Refund(id: String,
                   amount: Int,
                   created: DateTime,
                   currency: String,
                   balanceTransaction: String,
                   charge: String,
                   metadata: Option[JsObject],
                   reason: RefundReasons.Reason,
                   receiptNumber: Option[String],
                   description: Option[String])


object RefundReasons extends Enumeration {
  type Reason = Value
  val RequestedByCustomer = Value("requested_by_customer")
  val Duplicate = Value("duplicate")
  val Fraudulent = Value("fraudulent")
}

object Refund {

  def create(chargeId: String, amount: Option[Int] = None, reason: Option[RefundReasons.Reason] = None,
             refundApplicationFee: Boolean = false,
             reverseTransfer: Boolean = false, metadata: Option[JsObject] = None)(implicit client: StripeApi, ec: ExecutionContext) = {

    val params = Seq(
        ("refund_application_fee" -> refundApplicationFee.toString),
        ("reverse_transfer" -> reverseTransfer.toString)
      ) ++
      JsValueAsParam("metadata", metadata.getOrElse(JsNull)) ++
      amount.map(d => Seq(("amount", d.toString))).getOrElse(Seq.empty) ++
      reason.map(d => Seq(("reason", d.toString))).getOrElse(Seq.empty)

    client.post[Refund](s"/charges/$chargeId/refunds", params:_*)(client.readsRefund, ec)
  }

  def retrieve(id: String, chargeId: String)(implicit client: StripeApi, ec: ExecutionContext) = {
    client.get[Refund](s"/charges/$chargeId/refunds/$id")(client.readsRefund, ec)
  }

  def update(id: String, chargeId: String, metadata: JsObject)(implicit client: StripeApi, ec: ExecutionContext) = {
    val params = JsValueAsParam("metadata", metadata)
    client.post[Refund](s"/charges/$chargeId/refunds/$id", params:_*)(client.readsRefund, ec)
  }

  def list(chargeId: String, limit: Option[Int] = None, endingBefore: Option[String] = None, startingAfter: Option[String] = None,
            includeTotal: Boolean = true)(implicit client: StripeApi, ec: ExecutionContext) = {

    client.get[CollectionContainer[Refund]](s"/charges/$chargeId/refunds")(client.readsCollection(client.readsRefund), ec)

  }
}