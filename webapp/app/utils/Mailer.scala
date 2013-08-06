package utils

import javax.mail.internet.MimeMessage
import java.util.Properties
import play.api.Logger
import javax.mail.Session
import play.api.Play
import javax.mail.Transport
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.internet.InternetAddress
import javax.mail.Message
import javax.mail.Address

object Mailer {
  /*
   * send email to user.
   */
  def sendTokenByEmail(to: String, token: String) {
    Logger.info(s"Sending mail to $to")
    val username = Play.current.configuration.getString("mail.smtp.username").getOrElse("");
    val password = Play.current.configuration.getString("mail.smtp.password").getOrElse("");
    val props = new Properties();
    props.put("mail.smtp.auth", Play.current.configuration.getBoolean("mail.smtp.auth").getOrElse(false).toString)
    props.put("mail.smtp.starttls.enable", Play.current.configuration.getBoolean("mail.smtp.starttls.enable").getOrElse(false).toString)
    props.put("mail.smtp.host", Play.current.configuration.getString("mail.smtp.host").getOrElse(false).toString)
    props.put("mail.smtp.port", Play.current.configuration.getInt("mail.smtp.port").getOrElse(25).toString)

    val session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        override def getPasswordAuthentication(): PasswordAuthentication = {
          new PasswordAuthentication(username, password);
        }
      });

    try {

      val message = new MimeMessage(session);
      message.setFrom(new InternetAddress(username));
      message.setRecipients(Message.RecipientType.TO,
        InternetAddress.parse(to).asInstanceOf[Array[Address]]);
      message.setSubject(s"Your planning poker code is : $token");
      message.setText(s"Just enter the code $token and start your planning poker session.");

      Transport.send(message);

    } catch {
      case ex: MessagingException =>
        ex.printStackTrace()
        throw new RuntimeException(ex);
    }
  }

}