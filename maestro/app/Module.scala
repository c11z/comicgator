import java.time.Clock

import actions.GeekAction
import com.google.inject.AbstractModule
import daos._
import play.api.libs.concurrent.AkkaGuiceSupport
import services._

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // ActionBinders
    // bind(classOf[GeekAction])
    // DAO Binders
    bind(classOf[ComicDAO]).to(classOf[ComicDAOImpl])
    bind(classOf[GeekDAO]).to(classOf[GeekDAOImpl])
    bind(classOf[StripDAO]).to(classOf[StripDAOImpl])
    bind(classOf[FeedDAO]).to(classOf[FeedDAOImpl])
    bind(classOf[EmailDAO]).to(classOf[MailGun])
    // Use this to launch crawling jobs for comics.
    bindActor[LurkerActor]("lurker-actor")
    bind(classOf[Lurker]).asEagerSingleton()
    // Use this to orchestrate feed generation.
    bindActor[LineCookActor]("line-cook-actor")
    bind(classOf[LineCook]).asEagerSingleton()
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    // Set AtomicCounter as the implementation for Counter.
    bind(classOf[Counter]).to(classOf[AtomicCounter])
  }

}
