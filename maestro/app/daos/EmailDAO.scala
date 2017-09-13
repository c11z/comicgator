package daos

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.ConfigFactory
import errors.{MailGunEmailInvalidException, MailGunException}
import models.Geek
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.libs.ws.WSClient

trait EmailDAO {
  def validateEmail(email: String): Future[Boolean]
  def upsertMailingList(geek: Geek): Future[Boolean]
  def sendHello(email: String, code: String): Future[Boolean]
}


@Singleton
class MailGun @Inject()(ws: WSClient) (implicit exec: ExecutionContext) extends EmailDAO {
  private val config = ConfigFactory.load()
  private val COMIC_GATOR_HOSTNAME = config.getString("comic_gator_hostname")
  private val MAILGUN_BASE_URL = config.getString("mailgun.base_url")
  private val MAILGUN_DOMAIN = config.getString("mailgun.domain")
  private val MAILGUN_USER = config.getString("mailgun.user")
  private val MAILGUN_PRIVATE_API_KEY = config.getString("mailgun.private_api_key")
  private val MAILGUN_PUBLIC_API_KEY = config.getString("mailgun.public_api_key")
  private val MAILGUN_FROM = config.getString("mailgun.from")

  private val HTML_HELLO =
    """
      |<p>Greetings and thank you for your interest in...</p>
      |<h1>ComicGator</h1>
      |<p>Putting the alligator in web comics since 2016.</p>
    """.stripMargin

  private val TEXT_HELLO =
    """
      |Greetings and thank you for your interest in...
      |ComicGator
      |Putting the alligator in web comics since 2016.
    """.stripMargin

  private val HTML_START =
    """
      |<p>Greetings from...</p>
      |<h1>ComicGator</h1>
      |<p>Prepare yourself for the most awesome web comic aggregation experience of your life.<p>
    """.stripMargin

  private val TEXT_START =
    """
      |Greetings from...
      |ComicGator
      |
      |Prepare yourself for the most awesome web comic aggregation experience of your life.
    """.stripMargin

  def validateEmail(email: String): Future[Boolean] = {
    ws.url(s"$MAILGUN_BASE_URL/address/validate")
      .withAuth(MAILGUN_USER, MAILGUN_PUBLIC_API_KEY, BASIC)
      .withQueryString(("address", email))
      .get().map { response =>
        if (response.status != 200) {
          throw MailGunException(s"${response.status}:\n${response.body}")
        } else if (!(response.json \ "is_valid").as[Boolean]) {
          throw MailGunEmailInvalidException()
        } else {
            true
        }
    }
  }

  def upsertMailingList(geek: Geek): Future[Boolean] = {
    ws.url(s"$MAILGUN_BASE_URL/lists/splash@mg.comicgator.com/members")
      .withAuth(MAILGUN_USER, MAILGUN_PRIVATE_API_KEY, BASIC)
      .post(Map(
        "address" -> Seq(geek.email),
        "subscribed" -> Seq(if (geek.isReceptive) "yes" else "no"),
        "upsert" -> Seq("yes")
      )).map { response =>
      response.status match {
        case 200 => true
        case _ => throw MailGunException(s"${response.status}:\n${response.body}")
      }
    }
  }

  def sendHello(email: String, code: String): Future[Boolean] = {
//    val magicLink = s"$COMIC_GATOR_HOSTNAME/verify?email=$email&verification_code=$code"
    ws.url(s"$MAILGUN_BASE_URL/$MAILGUN_DOMAIN/messages")
      .withAuth(MAILGUN_USER, MAILGUN_PRIVATE_API_KEY, BASIC)
      .post(Map(
        "from" -> Seq(MAILGUN_FROM),
        "to" -> Seq(email),
        "subject" -> Seq("Comic Gator"),
        "text" -> Seq(TEXT_HELLO),
        "html" -> Seq(HTML_HELLO)
      )).map { response =>
        response.status match {
          case 200 => true
          case _ => throw MailGunException(s"${response.status}:\n${response.body}")
      }
    }
  }
}
