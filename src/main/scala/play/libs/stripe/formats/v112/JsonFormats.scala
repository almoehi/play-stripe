package play.libs.stripe.formats.v112

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import play.libs.stripe._
import org.joda.time.DateTime
import play.api.libs.json.JsObject
import play.libs.stripe.Card

object JsonFormats {

  implicit def collectionReads[T](implicit dataReads: Reads[T]): Reads[CollectionContainer[T]] = (
      (__ \ "data").readNullable[Seq[T]].map(_.getOrElse(Seq.empty[T])) and
      (__ \ "url").readNullable[String] and
      (__ \ "has_more").read[Boolean]
    )(CollectionContainer.apply[T] _)

  implicit val cardReads: Reads[Card] = (
      (__ \ "id").read[String] and
      ((__ \ "number").read[String] or (__ \ "last4").read[String].map("************" + _)) and
      (__ \ "exp_month").read[Int] and
        (__ \ "exp_year").read[Int] and
        (__ \ "brand").read[String] and
        (__ \ "funding").read[String].map(CardFunding.withName(_)) and
        (__ \ "last4").read[String] and
        (__ \ "name").read[String] and
        (__ \ "address_line1").readNullable[String] and
        (__ \ "address_line2").readNullable[String] and
        (__ \ "address_city").readNullable[String] and
        (__ \ "address_zip").readNullable[String] and
        (__ \ "address_state").readNullable[String] and
        (__ \ "address_country").readNullable[String] and
        (__ \ "country").readNullable[String] and
        (__ \ "cvc_check").readNullable[String].map(_.map(CheckResults.withName(_))) and
        (__ \ "address_line1_check").readNullable[String].map(_.map(CheckResults.withName(_))) and
        (__ \ "address_zip_check").readNullable[String].map(_.map(CheckResults.withName(_))) and
        (__ \ "dynamic_last4").readNullable[String] and
        (__ \ "metadata").readNullable[JsObject]
    )(Card.apply _)

  implicit val cardSourceWrites: Writes[CardSource] = (
      (__ \ "number").write[String] and
      (__ \ "exp_month").write[Int] and
      (__ \ "exp_year").write[Int] and
      (__ \ "cvc").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "address_line1").writeNullable[String] and
      (__ \ "address_line2").writeNullable[String] and
      (__ \ "address_city").writeNullable[String] and
      (__ \ "address_zip").writeNullable[String] and
      (__ \ "address_state").writeNullable[String] and
      (__ \ "address_country").writeNullable[String]
    )(unlift(CardSource.unapply))


  implicit val refundReads: Reads[Refund] = (
    (__ \ "id").read[String] and
      (__ \ "amount").read[Int] and
      (__ \ "created").read[Long].map(s => new DateTime(s * 1000)) and
      (__ \ "currency").read[String] and
      (__ \ "balance_transaction").read[String] and
      (__ \ "charge").read[String] and
      (__ \ "metadata").readNullable[JsObject] and
      (__ \ "reason").read[String].map(RefundReasons.withName(_)) and
      (__ \ "receipt_number").readNullable[String] and
      (__ \ "description").readNullable[String]
    )(Refund.apply _)

  /*
  implicit def listContainerReads[T](implicit rdr:Reads[T]): Reads[ListContainer[T]] = (
      (__ \ "data").read[Seq[JsValue]].map(_.map(_.as[T])).map(_.seq)
    )(ListContainer.apply _)
  */

  implicit val chargeReads: Reads[Charge] = (
    (__ \ "id").read[String] and
      (__ \ "livemode").read[Boolean] and
      (__ \ "amount").read[Int] and
      (__ \ "captured").read[Boolean] and
      (__ \ "created").read[Long].map(s => new DateTime(s * 1000)) and
      (__ \ "currency").read[String] and
      (__ \ "paid").read[Boolean] and
      (
        (
          (__ \ "refunded").read[Boolean] and
          (__ \ "refunds").readNullable[CollectionContainer[Refund]].map(_.getOrElse(CollectionContainer[Refund]())) and
          (__ \ "amount_refunded").readNullable[Int]
          )(ChargeRefundInfo.apply _)
      ) and
      (__ \ "source").read[Card] and
      (__ \ "status").read[String].map(ChargeStatus.withName(_)) and
      (
        (
          (__ \ "customer").readNullable[String] and
          (__ \ "invoice").readNullable[String] and
          (__ \ "dispute").readNullable[String] and
          (__ \ "balance_transaction").readNullable[String] and
            (__ \ "application_fee").readNullable[String] and
            (__ \ "destination").readNullable[String] and
            (__ \ "transfer").readNullable[String]
          )(ChargeReferences.apply _)
      ) and
      (__ \ "description").readNullable[String] and
      (__ \ "failure_code").readNullable[String].map(_.map(ErrorCodes.withName(_))) and
      (__ \ "failure_message").readNullable[String] and
      (__ \ "metadata").readNullable[JsObject] and
      (__ \ "recipient_email").readNullable[String] and
      (__ \ "receipt_number").readNullable[String] and
      (__ \ "fraud_details").readNullable[JsObject] and
      (__ \ "shipping").readNullable[JsObject]
    )(Charge.apply _)

}
