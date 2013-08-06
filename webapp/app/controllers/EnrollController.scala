package controllers

import scala.Array.canBuildFrom
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.future
import scala.util.Random

import play.api.Logger
import play.api.Play
import play.api.mvc.Action
import play.api.mvc.Controller
import play.data.validation.Constraints
import utils.AESEncryption
import utils.Mailer

object EnrollController extends Controller {
  
  /*
   * The user logs in using his email.
   * We send him a 4 digit code to check that his email is correct
   * The 4 digit code is encrypted using the cookie.secret value in the application.conf file
   * 
   */
  def enrollEmail(email: String) = Action {
    Constraints.email().isValid(email) match {
      case false => Unauthorized("Invalid email address").withNewSession
      case true => {
        val rand = new Random()
        val emailkey = "%04d".format(rand.nextInt(9999))
        Logger.debug(s"email key= $emailkey")
        future {
          Mailer.sendTokenByEmail(email, emailkey)
        }
        Ok("").withSession("emailkey" -> AESEncryption.encrypt(emailkey.toCharArray().map(_.toByte), Play.current.configuration.getString("cookie.secret").getOrElse("AsECretKeyPlaNNi")))
      }
    }
  }
  
  /*
   * When the user send us back teh 4 digit code
   * we simply decrypt the code stored in the cookie
   */
  def validateKey(code: String) = Action { request =>
    request.session.get("emailkey") match {
      case Some(emailkey) if AESEncryption.decrypt(emailkey, Play.current.configuration.getString("cookie.secret").getOrElse("AsECretKeyPlaNNi")) == code => {
        Ok("").withNewSession
      }
      case _ => Unauthorized("Invalid email key").withNewSession
    }
  }
  
}

