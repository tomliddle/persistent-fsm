package services

import akka.actor.Actor

class CounterSupervisor extends Actor {

  private val counter =  context.system.actorOf(Counter.props())

  override def receive: Receive = {
    case x => counter ! x
  }

}
