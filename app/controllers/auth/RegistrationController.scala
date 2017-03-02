package controllers.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.settings.SettingKey
import models.user._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.settings.SettingsService
import services.user.{UserSearchService, UserService}
import utils.Application

import scala.concurrent.Future

@javax.inject.Singleton
class RegistrationController @javax.inject.Inject() (
    override val app: Application,
    userService: UserService,
    userSearchService: UserSearchService,
    authInfoRepository: AuthInfoRepository,
    hasher: PasswordHasher
) extends BaseController {
  def registrationForm(email: Option[String] = None) = withoutSession("form") { implicit request =>
    if (SettingsService.allowRegistration) {
      val form = UserForms.registrationForm.fill(RegistrationData(
        username = email.map(e => if (e.contains('@')) { e.substring(0, e.indexOf('@')) } else { "" }).getOrElse(""),
        email = email.getOrElse("")
      ))
      Future.successful(Ok(views.html.auth.register(request.identity, form)))
    } else {
      Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> "You cannot sign up at this time. Contact your administrator."))
    }
  }

  def register = withoutSession("register") { implicit request =>
    if (!SettingsService.allowRegistration) {
      throw new IllegalStateException("You cannot sign up at this time. Contact your administrator.")
    }
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.register(request.identity, form))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email.toLowerCase)
        userSearchService.retrieve(loginInfo).flatMap {
          case _ if data.password != data.passwordConfirm => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> "Passwords do not match.")
          )
          case Some(user) => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> "That email address is already in use.")
          )
          case None =>
            val authInfo = hasher.hash(data.password)
            val role = Role.withName(SettingsService(SettingKey.DefaultNewUserRole))
            val user = User(
              id = UUID.randomUUID,
              username = data.username,
              preferences = UserPreferences.empty,
              profile = loginInfo,
              role = role
            )
            val userSavedFuture = userService.save(user)
            val result = request.session.get("returnUrl") match {
              case Some(url) => Redirect(url).withSession(request.session - "returnUrl")
              case None => Redirect(controllers.routes.HomeController.home())
            }
            for {
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- app.silhouette.env.authenticatorService.create(loginInfo)
              value <- app.silhouette.env.authenticatorService.init(authenticator)
              result <- app.silhouette.env.authenticatorService.embed(value, result)
              userSaved <- userSavedFuture
            } yield {
              app.silhouette.env.eventBus.publish(SignUpEvent(userSaved, request))
              app.silhouette.env.eventBus.publish(LoginEvent(userSaved, request))
              result.flashing("success" -> "You're all set!")
            }
        }
      }
    )
  }
}
