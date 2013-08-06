package models

import play.api.libs.iteratee.PushEnumerator
import play.api.libs.json._
import play.api.libs.functional.syntax._

/*
 * Permet de modéliser un participant à un planning poker
 * Le channel indique le canal de discussion Websocket avec le navigateur pour ce participant.
 */
case class Participant(val email: String, val timestamp: Long = System.currentTimeMillis, var channel: Option[PushEnumerator[JsValue]] = None)
