package services

import javax.inject.{Inject, Named}

import akka.pattern.ask
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class Tester @Inject()(@Named("countersupervisor") counter: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)

  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(5 seconds, context.self, "test")
  }


  override def receive: Receive = {

    case x: String =>
    //  counter ! AddItem(Item("1", "1", 2.0f))

     // counter ! Buy
      log.error("1111")
      (counter ? GetCurrentCart("1")).map { s =>
        log.error(s"cart response $s")
        val x = s
      }

      counter ! "ASDfasdfsadf"

      counter ! AddItems(Seq(Item("1", "1", 1.0f)))
      counter ! AddItems(Seq(Item("2", "2", 2.0f)))


      log.error("2222")
      counter ! GetCurrentCart("2")

      counter ! AddItems(Seq(Item("3", "3", 3.0f)))
      counter ! AddItems(Seq(Item("4", "4", 4.0f)))

      log.error("33333")
      (counter ? GetCurrentCart("3")).map { s =>
        log.error(s"cart response 2 $s")
        val x = s
      }

  }

}
