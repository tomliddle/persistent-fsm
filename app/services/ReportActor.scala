package services

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}


class ReportActor @Inject() extends Actor with ActorLogging {


  override def receive: Receive = {

    case x: Any =>
      log.info(x.toString)
  }

}
