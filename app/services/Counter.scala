package services

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState

import scala.concurrent.duration._
import scala.reflect.ClassTag

sealed trait Command
case class AddItems(item: Seq[Item]) extends Command
case object Buy extends Command
case object Leave extends Command
case class GetCurrentCart(s: String) extends Command



sealed trait UserState extends FSMState
case object LookingAround extends UserState {
  override def identifier: String = this.getClass.getName
}
case object Shopping extends UserState {
  override def identifier: String =  this.getClass.getName
}
case object Inactive extends UserState {
  override def identifier: String =  this.getClass.getName
}
case object Paid extends UserState {
  override def identifier: String =  this.getClass.getName
}


sealed trait DomainEvent
case class DomainEventUpdate(item: ShoppingCart) extends DomainEvent
case object OrderExecuted extends DomainEvent
case object OrderDiscarded extends DomainEvent

case class Item(id: String, name: String, price: Float)

case class ShoppingCart(items: Seq[Item] = Seq[Item]()) {
  def empty: ShoppingCart = ShoppingCart()
  def replaceItems(item: Seq[Item]): ShoppingCart = ShoppingCart(item)
}




object Counter {
  def props(): Props = Props(new Counter())
}

class Counter(implicit val domainEventClassTag: ClassTag[DomainEvent]) extends PersistentActor with PersistentFSM[UserState, ShoppingCart, DomainEvent] with ActorLogging {

  override val persistenceId = "counter5"


  startWith(LookingAround, ShoppingCart())

  when(LookingAround) {
    case Event(s: AddItems, _) ⇒
      goto(Shopping) applying DomainEventUpdate(ShoppingCart(s.item))
    case Event(GetCurrentCart, data) ⇒
      stay replying data
  }

  when(Shopping) {
    case Event(s: AddItems, _) ⇒
      stay applying DomainEventUpdate(ShoppingCart(s.item))// forMax (1 seconds)
    case Event(Buy, _) ⇒
      goto(Paid) applying OrderExecuted andThen {
        case ShoppingCart(items) ⇒
          saveStateSnapshot()
      }
    case Event(Leave, _) ⇒
      stop applying OrderDiscarded andThen {
        case _ ⇒
          saveStateSnapshot()
      }
    case Event(GetCurrentCart, data) ⇒
      log.error(s"getting cart $data")
      stay replying data
    case Event(StateTimeout, _) ⇒
      log.error("state timeout")
      goto(Inactive)// forMax (2 seconds)
  }

  when(Inactive) {
    case Event(s: AddItems, _) ⇒
      goto(Shopping) applying DomainEventUpdate(ShoppingCart(s.item))// forMax (1 seconds)
    case Event(StateTimeout, _) ⇒
      log.error("state timeout")
      stop applying OrderDiscarded
  }

  when(Paid) {
    case Event(Leave, _) ⇒ stop()

  }

  whenUnhandled {
    case Event(g: GetCurrentCart, data) ⇒
      log.error(s"get current cart ${g.s} $data")
      log.error(s" Name ios ${context.sender.path.name}")
      stay replying data
    case Event(s: String, _) =>
      log.error("message")
      stay
    case Event(s: Any, _) =>
      log.error(s + "message3324")
      stay replying "reply3"
  }


  override def applyEvent(event: DomainEvent, cartBeforeEvent: ShoppingCart): ShoppingCart = {
    event match {
      case DomainEventUpdate(item) ⇒ item
      case OrderExecuted   ⇒ cartBeforeEvent
      case OrderDiscarded  ⇒ cartBeforeEvent.empty
    }
  }
}
