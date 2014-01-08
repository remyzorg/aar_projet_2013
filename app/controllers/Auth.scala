package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.bson.types.ObjectId
import models._



object Auth extends Controller {

  val form = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying ("Wrong email or password", result =>
      result match
      {case (email, password) =>
        UserModel.findByEmailPassword(email, password) match {
          case None => false
          case _ => true
        }})
  )

  def setup = Action { implicit request =>
    Ok(views.html.login(form))
  }


  def getUser (implicit request : RequestHeader) =
    request.session.get(Security.username)

  def login = Action { implicit request =>
    form.bindFromRequest.fold (
      errors => BadRequest(views.html.login(errors)),
      user => user match {
        case (email, _) =>
          Redirect(routes.Application.index)
            .withSession(Security.username -> email)
      }
    )
  }


  def logout = Action {
    Redirect(routes.Auth.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }


}

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.setup)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withUser(f: User => Request[AnyContent] => Result) =
    withAuth { username => implicit request =>
    UserModel.findByEmail(username) match {
      case Some(user) => f(user)(request)
      case None => onUnauthorized(request)
    }
  }
}
