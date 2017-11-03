package services

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState

import scala.reflect.ClassTag

// Note these can be teh same
sealed trait Command
case class AddItems(item: Seq[Item]) extends Command
case object Buy extends Command
case object Leave extends Command
case class GetCurrentCart() extends Command



sealed trait UserState extends FSMState {
  override def identifier: String = this.getClass.getName
}
case object LookingAround extends UserState
case object Shopping extends UserState
case object Inactive extends UserState
case object Paid extends UserState


sealed trait DomainEvent
case class DomainEventUpdate(item: ShoppingCart) extends DomainEvent
case object OrderExecuted extends DomainEvent
case object OrderDiscarded extends DomainEvent

case class Item(id: String, name: String, price: Float)

case class ShoppingCart(items: Seq[Item] = Seq[Item]())




object Counter {
  def props(): Props = Props(new Counter())
}

class Counter(implicit val domainEventClassTag: ClassTag[DomainEvent]) extends PersistentActor with PersistentFSM[UserState, ShoppingCart, DomainEvent] with ActorLogging {

  override val persistenceId = "counter"


  startWith(LookingAround, ShoppingCart())

  when(LookingAround) {
    case Event(s: AddItems, _) ⇒
      stay applying DomainEventUpdate(ShoppingCart(s.item))

  }

  when(Shopping) {
    case Event(s: AddItems, _) ⇒
      stay applying DomainEventUpdate(ShoppingCart(s.item))

    case Event(Buy, _) ⇒
      goto(Paid) applying OrderExecuted andThen {
        case ShoppingCart(items) ⇒
          saveStateSnapshot()
      }

    case Event(Leave, _) ⇒
      stop applying OrderDiscarded andThen { _ ⇒
          saveStateSnapshot()
      }

    case Event(StateTimeout, _) ⇒
      log.error("state timeout")
      goto(Inactive)
  }

  when(Inactive) {
    case Event(s: AddItems, _) ⇒
      goto(Shopping) applying DomainEventUpdate(ShoppingCart(s.item))

    case Event(StateTimeout, _) ⇒
      log.error("state timeout")
      stop applying OrderDiscarded
  }

  when(Paid) {
    case Event(Leave, _) ⇒ stop()

  }

  whenUnhandled {
    case Event(_: GetCurrentCart, data) ⇒
      log.info(s"get current cart $data")
      stay replying data

    case Event(s: Any, _) =>
      log.error(s + "message3324")
      stay replying "reply3"
  }


  override def applyEvent(event: DomainEvent, cartBeforeEvent: ShoppingCart): ShoppingCart = {
    event match {
      case DomainEventUpdate(item) ⇒ item
      case OrderExecuted   ⇒ cartBeforeEvent
    }
  }
}
