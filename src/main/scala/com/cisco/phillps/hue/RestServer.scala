package com.phillps.hue

import spray.routing.HttpService
import com.phillps.hue.HueJsonProtocol._
import com.phillps.hue.HueConstants._
import spray.http.StatusCodes._
import spray.json._
import spray.httpx.SprayJsonSupport._
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor.Props

object ColorRestService {
  def props(hueActorRef: ActorRef): Props = Props(new ColorRestService(hueActorRef))
}

class ColorRestService(hueActorRef: ActorRef) extends Actor with HttpService  with RestServer {
  override def actorRefFactory: ActorContext = context
  override def hueActor: ActorRef = hueActorRef
  override def receive: PartialFunction[Any, Unit] = runRoute(hueRoutes)
}

trait RestServer extends HttpService {

  private implicit val execContext = actorRefFactory.dispatcher
  def hueActor: ActorRef

  val hueRoutes = pathPrefix("hue" / "v1" / "servicestatus") {
    post {
      entity(as[ServiceStatus]) { req =>
        complete {
          hueActor ! tellColor(req.name, req.status)
          OK
        }
      }
    }
  }

  def tellColor(name: String, status: String): Int = {
    if (Status.FAILURE.toString().equals(status)) {
      //Return Red Color
      0
    } else {
      //Return Green Color
      25500
    }
  }
}