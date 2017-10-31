import java.util.concurrent.Executors

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpecLike}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.Logger
import play.api.libs.json._
import play.api.test.Helpers.testServerPort

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Test the Resource Status Rest api. All the endpoints which provide information in JSON are tested.
  *
  *
  */
class ShoppingCartSpec
  extends PlaySpec with OneServerPerSuite with MustMatchers with WordSpecLike
    with BeforeAndAfterEach with BeforeAndAfterAll {

  private implicit val timeout: Duration = 10 seconds


  private var totalAllocations = 0

  override def beforeEach(): Unit = Thread.sleep(10000)

  override def afterEach(): Unit = Thread.sleep(10000)

  override def afterAll(): Unit = {

  }

  "The shopping cart" when {

    "purchasing a good" should {

      "save to state data" in {
        //val fsmRef = context.system.actorOf(WebStoreCustomerFSM.props(persistenceId, dummyReportActorRef))
      }
    }
  }
}