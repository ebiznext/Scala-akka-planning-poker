package models

import java.util.NoSuchElementException
import CardType._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import scala.concurrent.duration.DurationInt
import akka.util.Timeout
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.iteratee.PushEnumerator
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber

case class RoomMgr(val owner: String,
  var participants: Map[String, Participant] = Map(),
  var voteSessions: List[VoteSession] = List(),
  var lastAccessTime: Long = System.currentTimeMillis) extends Actor with ActorLogging {

  import context._
  implicit val timeout = Timeout(5 seconds)

  /*
  val roomId = Promise[UniqueToken]
  (ScrumDeckFactory.lookup("UniqueTokenMgr") ? GetUniqueToken).mapTo[UniqueToken] onComplete {
    case Right(uniqueToken) => roomId success uniqueToken
    case Left(error) => roomId failure error
  }
*/
  setReceiveTimeout(20 minutes)

  private def lastSession: VoteSession = voteSessions match {
    case head :: tail => head
    case Nil => throw new NoSuchElementException("Aucune session active")
  }

  def receive = {
    case GetRoomOwner => sender ! owner

    case InviteParticipant(participantId) =>
      participants += (participantId -> new Participant(participantId))
      notifyPaticipantEvent(participantId, "invite")

    case RevokeParticipant(participantId) =>
      notifyPaticipantEvent(participantId, "revoke")
      participants.get(participantId) match {
        case Some(participant) if (participant.channel.isDefined) => participant.channel.get >>> Enumerator.eof
        case _ =>
      }
      participants -= participantId

    case ContainsParticipant(participantId) =>
      sender ! (participants.get(participantId) match {
        case Some(participant) => Some(owner)
        case None => None
      })

    case StartNewVoteSession =>
      voteSessions ::= VoteSession()
      notifyRoomEvent("startNewVoteSession")

    case StopCurrentVoteSession =>
      lastSession.endTime = Some(System.currentTimeMillis)
      notifyRoomEvent("stopCurrentVoteSession")

    case SubmitVote(participantId, card) =>
      lastSession submit (participantId, card)
      notifyPaticipantEvent(participantId, "submitVote")

    case CollectVotes =>
      sender ! lastSession.collectVotes
      notifyVoteEvent(lastSession.collectVotes, "collectVotes")

    case ListParticipants => sender ! participants.keys.toList

    case RoomConnect(participantId, channel) =>
      participants.get(participantId) match {
        case Some(participant) => participant.channel = Some(channel)
        case None => channel >>> Enumerator.eof
      }
      notifyPaticipantEvent(participantId, "connect")
  }

  private def notifyPaticipantEvent(email: String, kind: String) {
    participants.values.foreach(participant => participant.channel match {
      case Some(channel) =>
        val msg = JsObject(
          Seq(
            "kind" -> JsString(kind),
            "participant" -> JsString(email),
            "room" -> JsString(owner)))
        channel.push(msg)
      case None =>
    })
  }

  private def notifyVoteEvent(votes: List[Vote], kind: String) {
    val jsVotes = votes.map {
      vote =>
        JsObject(
          Seq(
            "email" -> JsString(vote.email),
            "time" -> JsNumber(vote.voteTime),
            "card" -> JsString(vote.card)))
    }
    participants.values.foreach(participant => participant.channel match {
      case Some(channel) =>
        val msg = JsObject(
          Seq(
            "kind" -> JsString(kind),
            "votes" -> JsArray(jsVotes),
            "room" -> JsString(owner)))
        channel.push(msg)
      case None =>
    })
  }

  private def notifyRoomEvent(kind: String, close: Boolean = false) {
    participants.values.foreach(participant => participant.channel match {
      case Some(channel) =>
        val msg = JsObject(
          Seq(
            "kind" -> JsString(kind),
            "room" -> JsString(owner)))
        channel.push(msg)
        if (close) channel >>> Enumerator.eof
      case None =>
    })
  }

  override def postStop() = {
    notifyRoomEvent("disconnect", true)
    participants = Map()
    println("actor " + owner + "stopped")
  }
}
