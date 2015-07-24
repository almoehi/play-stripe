package play.libs.stripe

import play.api.libs.json.JsObject

object CardFunding extends Enumeration {
  type Fund = Value
  val Credit = Value("credit")
  val Debit = Value("debit")
  val Prepaid = Value("prepaid")
  val Unknown = Value("unknown")
}


sealed case class Card(
                 id: String,
                 number: String,
                 expMonth: Int,
                 expYear: Int,
                 brand: String,
                 funding: CardFunding.Fund,
                 last4: String,
                 name: String,
                 addressLine1: Option[String],
                 addressLine2: Option[String],
                 addressCity: Option[String],
                 addressZip: Option[String],
                 addressState: Option[String],
                 addressCountry: Option[String],
                 country: Option[String],
                 cvcCheck: Option[CheckResults.Result],
                 addressLine1Check: Option[CheckResults.Result],
                 addressZipCheck: Option[CheckResults.Result],
                 dynamicLastFour: Option[String],
                 metadata: Option[JsObject]
)
