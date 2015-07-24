import java.util.concurrent.TimeUnit
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.libs.stripe._
import play.libs.stripe.formats.v112.JsonFormats._
import scala.concurrent.Await
import scala.concurrent.duration._

/**
* Add your spec here.
* You can mock out a whole application including requests, plugins etc.
* For more information, consult the wiki.
*/
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  sequential

  var maybeCreatedCharge: Option[Charge] = None

  val validVisa = "4242424242424242"
  val validMasterCard = "5555555555554444"

  val declinedVisaCvc = "4000000000000127"
  val declinedVisaExpired = "4000000000000069"

  val timeout = Duration(30, TimeUnit.SECONDS)

  "play-stripe library" should {

    "be able to create and retrieve a charge" in {

      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val stripe = new StripeV112
      val cardSource = CardSource(validVisa, 1, 2016, "123", name = Some("Test Card"), addressLine1 = Some("1 Address Road"))
      val futureResult = Charge.createWithCardSource(cardSource, 155, "cad", true, Some("play-stripe UnitTest"))

      val maybeResult = Await.result(futureResult, timeout)

      maybeResult should beRight

      val chargeId = maybeResult.right.get.id

      val futureRetrieve = Charge.retrieve(chargeId)
      val maybeRetrive = Await.result(futureRetrieve, timeout)

      maybeRetrive should beRight

      maybeCreatedCharge = Some(maybeResult.right.get)

      maybeRetrive.right.get.id must_==(maybeResult.right.get.id)
      maybeRetrive.right.get.amount must_==(maybeResult.right.get.amount)

    }

    "be able to refund a charge" in {
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val stripe = new StripeV112

      maybeCreatedCharge must beSome

      val createdCharge = maybeCreatedCharge.get

      val futureRefund = Refund.create(createdCharge.id, reason = Some(RefundReasons.RequestedByCustomer))
      val maybeRefund = Await.result(futureRefund, timeout)

      maybeRefund should beRight

      val refund = maybeRefund.right.get

      refund.amount must_==(createdCharge.amount)
      refund.currency must_==(createdCharge.currency)
      refund.charge must_==(createdCharge.id)
    }
  }
}
