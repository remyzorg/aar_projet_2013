package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

object Signin extends Controller {

  val errors = ""

  val form = Form(
    tuple(
      "email" -> text,
      "username" -> text,
      "password" -> text,
      "confirm" -> text
    ) verifying ("Passwords must match", result => result match {
      case (_, _, password, confirm) => password == confirm
    })
  )

  def setup = Action {
    Ok(views.html.signin(form));
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.signin(errors)),
      user => user match { case (email, username, password, confirm) =>
      Ok("Hi %s %s".format(email, username)) }
      // Redirect(routes.Application.index) // .withSession(Security.username -> user._1)
    )
  }


}
