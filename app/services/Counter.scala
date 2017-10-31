package services

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState

import scala.concurrent.duration._
import scala.reflect.ClassTag

sealed trait Command
case class AddItem(item: Item) extends Command
case object Buy extends Command
case object Leave extends Command
case class GetCurrentCart(s: String) extends Command



sealed trait UserState extends FSMState
case object LookingAround extends UserState {
  override def identifier: String = "Looking Around"
}
case object Shopping extends UserState {
  override def identifier: String = "Shopping"
}
case object Inactive extends UserState {
  override def identifier: String = "Inactive"
}
case object Paid extends UserState {
  override def identifier: String = "Paid"
}


sealed trait DomainEvent
case class ItemAdded(item: Item) extends DomainEvent
case object OrderExecuted extends DomainEvent
case object OrderDiscarded extends DomainEvent

case class Item(id: String, name: String, price: Float)

sealed trait ShoppingCart {
  def addItem(item: Item): ShoppingCart
  def empty(): ShoppingCart
}
case object EmptyShoppingCart extends ShoppingCart {
  def addItem(item: Item) = NonEmptyShoppingCart(item :: Nil)
  def empty(): ShoppingCart = this
}
case class NonEmptyShoppingCart(items: Seq[Item]) extends ShoppingCart {
  def addItem(item: Item) = NonEmptyShoppingCart(items :+ item)
  def empty(): ShoppingCart = EmptyShoppingCart
}



object Counter {
  def props(): Props = Props(new Counter())
}

class Counter(implicit val domainEventClassTag: ClassTag[DomainEvent]) extends PersistentActor with PersistentFSM[UserState, ShoppingCart, DomainEvent] with ActorLogging {

  override val persistenceId = "counter1"


  startWith(LookingAround, EmptyShoppingCart)

  when(LookingAround) {
    case Event(AddItem(item), _) ⇒
      goto(Shopping) applying ItemAdded(item) forMax (1 seconds)
    case Event(GetCurrentCart, data) ⇒
      stay replying data
  }

  when(Shopping) {
    case Event(AddItem(item), _) ⇒
      stay applying ItemAdded(item) forMax (1 seconds)
    case Event(Buy, _) ⇒
      goto(Paid) applying OrderExecuted andThen {
        case NonEmptyShoppingCart(items) ⇒
          saveStateSnapshot()
        case EmptyShoppingCart ⇒ saveStateSnapshot()
      }
    case Event(Leave, _) ⇒
      stop applying OrderDiscarded andThen {
        case _ ⇒
          saveStateSnapshot()
      }
    case Event(GetCurrentCart, data) ⇒
      stay replying data
    case Event(StateTimeout, _) ⇒
      goto(Inactive) forMax (2 seconds)
  }

  when(Inactive) {
    case Event(AddItem(item), _) ⇒
      goto(Shopping) applying ItemAdded(item) forMax (1 seconds)
    case Event(StateTimeout, _) ⇒
      stop applying OrderDiscarded
  }

  when(Paid) {
    case Event(Leave, _) ⇒ stop()

  }

  whenUnhandled {
    case Event(g: GetCurrentCart, data) ⇒
      log.error(s"get current cart ${g.s} $data")
      log.error(context.sender.path.name)
      stay replying data
    case Event(s: String, _) =>
      log.error("message")
      stay
    case Event(EmptyShoppingCart, _) =>
      log.error("message2")
      stay replying "reply2"
    case Event(s: Any, _) =>
      log.error(s + "message3324")
      stay replying "reply3"
  }


  override def applyEvent(event: DomainEvent, cartBeforeEvent: ShoppingCart): ShoppingCart = {
    event match {
      case ItemAdded(item) ⇒ cartBeforeEvent.addItem(item)
      case OrderExecuted   ⇒ cartBeforeEvent
      case OrderDiscarded  ⇒ cartBeforeEvent.empty()
    }
  }
}
