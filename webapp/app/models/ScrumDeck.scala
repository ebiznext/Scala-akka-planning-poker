package models

import akka.actor._
import scala.concurrent._
import akka.pattern.ask
import scala.concurrent.duration.DurationInt
import akka.util.Timeout
import models.CardType._
import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import scala.util.Failure
import scala.util.Success

object ScrumDeckFactory {
  val system = ActorSystem("ScrumDeckSystem")
  def lookup(name: String): ActorRef = system.actorFor("akka://ScrumDeckSystem/user/ScrumDeck/" + name)
  def create[T <: Actor](props: Props, name: String) = system.actorOf(props, name = name)
}

object ScrumDeck {
  implicit val timeout = Timeout(10 second)
  lazy val scrumdeck = ScrumDeckFactory.create(Props[ScrumDeck], "ScrumDeck")
  
  
  /*
   * When a user wants to start a planning poker, he simply create a room
   */
  def addRoom(owner: String): ActorRef = {
    val future = (scrumdeck ? AddRoom(owner))
    Await.result(future, 10 seconds).asInstanceOf[ActorRef]
  }

  def removeRoom(owner: String) {
    scrumdeck ! RemoveRoom(owner)
    println("room removed")
  }

  /*
   * Remove participant from the room 
   */
  def revokeParticipant(roomOwner: String, participantEmail: String) {
    Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef] ! RevokeParticipant(participantEmail)
  }

  /*
   * A particpant is entering the room.
   */
  def roomConnect(roomOwner: String, participantEmail: String): (Iteratee[JsValue, _], Enumerator[JsValue]) = {
    val out = Enumerator.imperative[JsValue]()
    val in = Iteratee.foreach[JsValue] { event =>
      // default ! Talk(username, (event \ "text").as[String])
    }.mapDone { _ =>
      revokeParticipant(roomOwner, participantEmail)
    }
    Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef] ! RoomConnect(participantEmail, out)
    (in, out)
  }

  /*
   * The scrummaster is inviting someone to enter the room
   */
  def inviteParticipant(roomOwner: String, participantEmail: String) {
    val future = scrumdeck ? GetRoomByOwner(roomOwner)
    Await.result(future, 10 seconds).asInstanceOf[ActorRef] ! InviteParticipant(participantEmail)
  }

  /*
   * List of rooms where the participant is registered
   */
  def participantRooms(participantId: String): List[String] = {
    val future = (scrumdeck ? ParticipantRooms(participantId))
    Await.result(future, 10 seconds).asInstanceOf[List[String]]
  }

  /*
   *Countdown starts
   */
  def voteSessionStart(roomOwner: String) {
    Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef] ! StartNewVoteSession
  }

  /*
   * Everybody should show his card, wountdown ended
   */
  def voteSessionStop(roomOwner: String) {
    Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef] ! StopCurrentVoteSession
  }

  /*
   * A particpant chose his card
   */
  def submiVote(roomOwner: String, participantEmail: String, card: CardType) {
    Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef] ! SubmitVote(participantEmail, card)
  }

  /*
   * All teh votes are collected in a specific room
   */
  def collectVotes(roomOwner: String) :List[Vote] = {
    val actor = Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef]
    println(actor)
    Await.result(actor ? CollectVotes, 10 seconds).asInstanceOf[List[Vote]]
  }

  /*
   * List of all the participants in the room
   */
  def listPartcipants(roomOwner: String) : List[String ]= {
    val actor = Await.result(scrumdeck ? GetRoomByOwner(roomOwner), 10 seconds).asInstanceOf[ActorRef]
    println(actor)
    Await.result(actor ? ListParticipants, 10 seconds).asInstanceOf[List[String]]
  }
}

class ScrumDeck extends Actor with ActorLogging {
  import context._
  var rooms: Map[String, UniqueToken] = Map()
  val tokenMgr = actorOf(Props[UniqueTokenMgr], "UniqueTokenMgr")
  implicit val timeout = Timeout(20 seconds)
  def receive = {
    case AddRoom(owner) =>
      try {
        stop(ScrumDeckFactory.lookup(rooms(owner).value))
      } catch {
        case e: Exception => e.printStackTrace
      }
      val originalSender = sender
      (tokenMgr ? GetUniqueToken).mapTo[UniqueToken] onComplete {
        case Success(token) => {
          println("room created=" + token.value)
          val roomMgr = actorOf(Props(new RoomMgr(owner)), name = token.value)
          rooms += (owner -> token)
          originalSender ! roomMgr
        }
        case Failure(error) => log.error(error.getStackTraceString)
      }
    case GetRoomByOwner(owner) => sender ! ScrumDeckFactory.lookup(rooms(owner).value)
    case RemoveRoom(owner) =>
      try {
        stop(ScrumDeckFactory.lookup(rooms(owner).value))
        rooms -= owner
      } catch {
        case e: Exception => e.printStackTrace
      }
    case ParticipantRooms(participantId) => {
      val originalSender = sender
      val listOfFutures = (for (entry <- rooms) yield (ScrumDeckFactory.lookup(entry._2.value) ? ContainsParticipant(participantId)).mapTo[Option[String]]) toList
      val futureList = Future.sequence(listOfFutures)
      futureList.map { list =>
        originalSender ! (list.collect { case e if e.isDefined => e.get })
      }
    }
  }
}
