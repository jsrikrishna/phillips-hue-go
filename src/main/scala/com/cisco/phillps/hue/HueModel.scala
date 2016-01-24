package com.phillps.hue

import spray.json.DefaultJsonProtocol
import com.phillps.hue.HueConstants._

case class ServiceStatus(name: String, status: String){
  require(Status.isValidStatus(status), InvalidStatus)
}


object Status extends Enumeration {
  val SUCCESS = Value("success")
  val FAILURE = Value("failure")

  def isValidStatus(s: String): Boolean = values.exists {
    _.toString().equalsIgnoreCase(s)
  }
}

object HueJsonProtocol extends DefaultJsonProtocol {

  implicit val serviceStatus = jsonFormat2(ServiceStatus)

}