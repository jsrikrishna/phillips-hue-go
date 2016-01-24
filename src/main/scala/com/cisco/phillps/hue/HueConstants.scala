package com.phillps.hue
import com.philips.lighting.hue.sdk.PHSDKListener
import com.philips.lighting.hue.sdk.PHBridgeSearchManager
import com.philips.lighting.hue.sdk.PHHueSDK

/**
 * @author sjalipar
 */

object HueConstants {
  val initialize = "initialize"
  val lightsFound = "lightsFound"
  val bridgeConnected = "bridgeConnected"
  val ChangeLightColors = "changeLightColors"

  //Invalid Request - Messages
  val InvalidService = "name is not valid"
  val InvalidStatus = "Status should be either success or failure"
}