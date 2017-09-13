package daos

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.{Inject, Singleton}
import errors.CDBException
import org.bson.types.ObjectId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.db.NamedDatabase
import slick.driver.JdbcProfile

trait GeekDAO {
  def select(geekId: ObjectId): Option[JsValue]

  def save(email: String, isReceptive: Boolean): Future[Boolean]

  def saveSession(geekId: ObjectId, token: String): Unit

  def checkSession(token: String): Future[Option[ObjectId]]

  def prior(email: String, code: String): Future[Boolean]

  def verify(email: String, code: String): Future[Option[ObjectId]]

  def starStrip(geekId: ObjectId, stripId: ObjectId, isStarred: Boolean): Unit

  def viewStrip(geekId: ObjectId, stripId: ObjectId, isViewed: Boolean): Unit

  def starComic(geekId: ObjectId, comicId: ObjectId, isStarred: Boolean): Unit

  def viewComic(geekId: ObjectId, comicId: ObjectId, isViewed: Boolean): Unit
}


@Singleton
class GeekDAOImpl @Inject()(@NamedDatabase("cdb") protected val dbConfigProvider: DatabaseConfigProvider) (implicit exec: ExecutionContext)
extends HasDatabaseConfigProvider[JdbcProfile] with GeekDAO {
  import driver.api._
  def select(geekId: ObjectId): Option[JsValue] = Some(Json.obj())

  def save(email: String, isReceptive: Boolean): Future[Boolean] = {
    db.run(sqlu"""
      INSERT INTO cg.geek (email, is_receptive) VALUES ($email, $isReceptive::BOOLEAN)
      ON CONFLICT (email) DO
      UPDATE SET (email, is_receptive) = ($email, $isReceptive::BOOLEAN)""").map {
      case 1 => true
      case 0 => throw CDBException("GeekDAO.save")
    }
  }

  def prior(email: String, code: String): Future[Boolean] = {
    db.run(sqlu"""
      INSERT INTO cg.verification (email, code) VALUES ($email, $code)
    ON CONFLICT (email) DO
      UPDATE SET (email, code) = ($email, $code)""").map {
      case 1 => true
      case 0 => throw CDBException("GeekDAO.prior")
    }
  }

  def saveSession(geekId: ObjectId, token: String): Unit = {
    db.run(sqlu"""INSERT INTO cg.session_geek (geek_id, token) VALUES (${geekId.toString}, $token)""")
  }

  def checkSession(token: String): Future[Option[ObjectId]] = {
    db.run(sql"""
      SELECT sg.geek_id
      FROM cg.session_geek sg
      WHERE sg.token = $token""".as[ObjectId].headOption)
  }

  def verify(email: String, code: String): Future[Option[ObjectId]] = {
    db.run(sql"""
      WITH
        validate AS (
          SELECT g.id
          FROM cg.verification v
            JOIN cg.geek g USING (email)
          WHERE v.email = $email
                AND v.code = $code),
        cleanup AS (
          DELETE FROM cg.verification
          WHERE email = $email)
      SELECT * FROM validate""".as[ObjectId].headOption)
  }

  def starStrip(geekId: ObjectId, stripId: ObjectId, isStarred: Boolean): Unit = {}

  def viewStrip(geekId: ObjectId, stripId: ObjectId, isViewed: Boolean): Unit = {}

  def starComic(geekId: ObjectId, comicId: ObjectId, isStarred: Boolean): Unit = {}

  def viewComic(geekId: ObjectId, comicId: ObjectId, isViewed: Boolean): Unit = {}
}
