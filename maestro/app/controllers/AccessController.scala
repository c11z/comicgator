package controllers

import java.security.SecureRandom
import javax.inject.{Inject, Singleton}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

import com.eclipsesource.schema._
import com.typesafe.config.ConfigFactory
import daos.{EmailDAO, GeekDAO}
import models.Geek
import play.Logger
import play.api.Environment
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.BearerToken

@Singleton
class AccessController @Inject() (environment: Environment,
                                  geekDAO: GeekDAO,
                                  emailDAO: EmailDAO)
                                 (implicit exec: ExecutionContext) extends Controller {
  private val config = ConfigFactory.load()
  private val VALIDATION_CODE_LENGTH = config.getInt("validation_code_length")
  private val VALIDATION_CODE_CHARS = config.getString("validation_code_chars")

  val secureRandom = new SecureRandom()
  val validator = SchemaValidator()

  lazy val schema: SchemaType = {
    val filename = environment.getFile("/conf/schemas/geek.schema.json")
    val t = scala.io.Source.fromFile(filename).mkString
    val j = Json.parse(t)
    Json.fromJson[SchemaType](j).get
  }

  /**
    *
    */
  def start: Action[JsValue] = Action.async(parse.json) { request =>
    validator.validate(schema, request.body, Geek.Reader).fold(
      invalid = {
        errors =>
          Logger.error(Json.prettyPrint(errors.toJson))
          Future.successful(BadRequest)
      },
      valid = { geek =>
        (for (isValid <- emailDAO.validateEmail(geek.email))
          yield for {
            code <- Future.successful(generateVerificationCode)
            saved <- geekDAO.save(geek.email, geek.isReceptive)
            set <- geekDAO.prior(geek.email, code)
            sent <- emailDAO.sendHello(geek.email, code)
            list <- emailDAO.upsertMailingList(geek)
          } yield isValid & saved & set & sent & list).flatMap(identity).map {
          case true => Accepted
          case false => BadRequest
        }
      }
    )
  }

  private def generateVerificationCode: String = {
    val charLength = VALIDATION_CODE_CHARS.length()
    @tailrec
    def loop(code: String, number: Int): String = {
      if (number == 0) {
        code
      } else {
        loop(code + VALIDATION_CODE_CHARS(secureRandom.nextInt(charLength)).toString, number - 1)
      }
    }
    loop("", VALIDATION_CODE_LENGTH)
  }

  /**
    *
    */
  def verify(email: String, code: String): Action[AnyContent] = Action.async { request =>
    geekDAO.verify(email, code).map {
      case Some(geekId) =>
        val token = BearerToken.generateMD5(geekId.toString)
        geekDAO.saveSession(geekId, token)
        // Set token and geek_id in local session as well.
        Ok.withCookies(Cookie("cg_token", token))
      case None => Redirect("/home").discardingCookies(DiscardingCookie("cg_token"))
    }
  }
}
