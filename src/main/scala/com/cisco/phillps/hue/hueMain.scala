package com.phillps.hue

import com.philips.lighting.hue.sdk.PHBridgeSearchManager
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.hue.sdk.PHMessageType
import com.philips.lighting.hue.sdk.PHSDKListener
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager
import HueConstants._
import akka.actor.Actor
import akka.io.IO
import spray.can.Http
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.philips.lighting.model.PHLightState
/**
 * @author sjalipar
 */
object hueMain {
  def main(args: Array[String]) = {

    println("starting Hue Actor")

    val phHueSDK = PHHueSDK.getInstance()
    phHueSDK.setAppName("testing-hue-bridge")

    // create the processor Actor
    implicit val actorSys = ActorSystem("HueActorSystem")
    val hueActor = actorSys.actorOf(HueActor.props(phHueSDK), "Hue-Actor")
    hueActor ! HueConstants.initialize

    //Rest service
    val colorRestService = actorSys.actorOf(ColorRestService.props(hueActor), "rest-actor")

    // Bind to the VCAP host and port
        IO(Http) ! Http.Bind(colorRestService, Option(System.getenv("VCAP_APP_HOST")).getOrElse("localhost"),
          Option(System.getenv("VCAP_APP_PORT")).getOrElse("8080").toInt)
  }
}

class phListener(val phHueSDK: PHHueSDK, actor: ActorRef) extends PHSDKListener {

  override def onAccessPointsFound(accessPoint: java.util.List[com.philips.lighting.hue.sdk.PHAccessPoint]): Unit = {
    println("AccessPoints Found: " + accessPoint.size())
    println("connecting to the accessPoint with IP: " + accessPoint.get(0).getIpAddress)
    phHueSDK.connect(accessPoint.get(0))
  }

  override def onAuthenticationRequired(accessPoint: com.philips.lighting.hue.sdk.PHAccessPoint): Unit = {
    println("Authentication is Required")
    phHueSDK.startPushlinkAuthentication(accessPoint)
    println("Authentication Done")
  }

  override def onBridgeConnected(bridge: com.philips.lighting.model.PHBridge, userName: String): Unit = {
    println("connected to bridge: " + bridge)
    phHueSDK.setSelectedBridge(bridge)
    val hbManager = PHHeartbeatManager.getInstance()
    hbManager.enableLightsHeartbeat(bridge, PHHueSDK.HB_INTERVAL)
    actor ! bridgeConnected
  }

  override def onCacheUpdated(cacheNotificationsList: java.util.List[Integer], bridge: com.philips.lighting.model.PHBridge): Unit = {
    if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) println("Lights Cache Updated ");
  }

  override def onConnectionLost(accesPoint: com.philips.lighting.hue.sdk.PHAccessPoint): Unit = {
    println("Connection to AccessPoint is lost: " + accesPoint.getIpAddress)
  }

  override def onConnectionResumed(bridge: com.philips.lighting.model.PHBridge): Unit = {
    println("Connection to Bridge is Resumed: " + bridge)
  }

  override def onError(x1: Int, x2: String): Unit = {
    println("error occured with code: " + x1 + " " + x2)
  }

  override def onParsingErrors(parsingErrorsList: java.util.List[com.philips.lighting.model.PHHueParsingError]): Unit = {
    println(parsingErrorsList)
  }

}
object HueActor{
  def props(phHueSDK: PHHueSDK): Props = Props(new HueActor(phHueSDK))
}

class HueActor(phHueSDK: PHHueSDK) extends Actor {

  override def receive = {

    case HueConstants.initialize => {
      println("Got the initialization message")
      // Get the listener
      val listener = new phListener(phHueSDK, self)
      // Register the PHSDKListener to receive callbacks from the bridge.
      phHueSDK.getNotificationManager().registerSDKListener(listener)
      // Start the bridge search
      val sm = phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE).asInstanceOf[PHBridgeSearchManager]
//      sm.search(false, false)
      //Search for the IP address of the bridge
      sm.ipAddressSearch()
    }
    case HueConstants.bridgeConnected => {
      val cache = phHueSDK.getSelectedBridge().getResourceCache()
      val myLights = cache.getAllLights()
      println("Lights found: " + myLights)
      if (myLights.size() > 0) self ! lightsFound
    }
    case HueConstants.lightsFound => {
      println("Hurray Lights Found")
      self ! ChangeLightColors
    }

    case hue : Int => {
      println("Changing the colors")
      val cache = phHueSDK.getSelectedBridge().getResourceCache()
      val myLights = cache.getAllLights()
      val  bridge = phHueSDK.getSelectedBridge()
      val lightState = new PHLightState()
      lightState.setHue(hue)
      bridge.updateLightState(myLights.get(0), lightState);
    }

    case x: Any                   => println(s"Received unknown message: ${x}")

  }

}