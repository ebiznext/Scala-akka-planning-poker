package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.data.validation.Constraints
import models._
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.JsBoolean
import play.api.libs.json.JsBoolean
import scala.util.parsing.json.JSONArray

object AkkaCardController extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }
  
  def roomOpen(roomOwner: String) = Action { implicit request =>
    Constraints.email().isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true => {
        val roomRef = ScrumDeck.addRoom(roomOwner)
        //Ok(Json.generate(roomRef.path.toString))
        Ok(roomRef.path.toString())
      }
    }
  }

  def roomConnect(roomOwner: String, participantEmail: String) = WebSocket.using[JsValue] { implicit request =>
    Constraints.email().isValid(roomOwner) && Constraints.email().isValid(participantEmail) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true => {
        ScrumDeck.roomConnect(roomOwner, participantEmail)
      }
    }
  }

  def roomClose(roomOwner: String) = Action { implicit request =>
    Constraints.email.isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true =>
        ScrumDeck.removeRoom(roomOwner)
        Ok(new JsBoolean(true).toString)
    }
  }

  def particpantsInvite(roomOwner: String, participantEmails: List[String]) = Action { implicit request =>
    participantEmails.forall(Constraints.email.isValid(_)) && Constraints.email.isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner + " or " + participantEmails)
      case true =>
        participantEmails.foreach(participant => ScrumDeck.inviteParticipant(roomOwner, participant))
        Ok(new JsBoolean(true).toString)
    }
  }

  def participantRevoke(roomOwner: String, participantEmail: String) = Action { implicit request =>
    Constraints.email().isValid(roomOwner) && Constraints.email().isValid(participantEmail) match {
      case false => throw new IllegalArgumentException(roomOwner + " or " + participantEmail)
      case true =>
        ScrumDeck.revokeParticipant(roomOwner, participantEmail)
        Ok(new JsBoolean(true).toString)
    }
  }

  def particpantsList(roomOwner: String) = Action { implicit request =>
    Constraints.email.isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true =>
        Ok(Json.stringify(Json.toJson(ScrumDeck.listPartcipants(roomOwner))))
    }
  }

  def particpantRooms(particpantId: String) = Action { implicit request =>
    Constraints.email.isValid(particpantId) match {
      case false => throw new IllegalArgumentException(particpantId)
      case true =>
        Ok(Json.stringify(Json.toJson(ScrumDeck.participantRooms(particpantId))))
    }
  }

  def voteSessionStart(roomOwner: String) = Action { implicit request =>
    Constraints.email.isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true =>
        ScrumDeck.voteSessionStart(roomOwner)
        Ok(new JsBoolean(true).toString)
    }
  }

  def voteSessionStop(roomOwner: String) = Action { implicit request =>
    Constraints.email.isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true =>
        ScrumDeck.voteSessionStop(roomOwner)
        Ok(new JsBoolean(true).toString)
    }
  }

  def submitVote(roomOwner: String, participantEmail: String, card: String) = Action { implicit request =>
    Constraints.email.isValid(participantEmail) && Constraints.email.isValid(roomOwner) && Constraints.pattern("0|1/2|1|2|3|5|8|13|20|40|100|\\?|coffee").isValid(card) match {
      case false => throw new IllegalArgumentException(roomOwner + " or " + card)
      case true =>
        ScrumDeck.submiVote(roomOwner, participantEmail, card)
        Ok(new JsBoolean(true).toString)
    }
  }

  def collectVotes(roomOwner: String) = Action { implicit request =>
    Constraints.email.isValid(roomOwner) match {
      case false => throw new IllegalArgumentException(roomOwner)
      case true =>
        val votes = ScrumDeck.collectVotes(roomOwner)
        Ok(Json.stringify(Json.toJson(votes)))
    }
  }
}


