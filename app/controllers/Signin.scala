package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

object Signin extends Controller {

val form = Form(
    tuple(
      "email" -> text,
      "username" -> text,
      "password" -> text,
      "confirm" -> text
    )
  )

  def setup = Action {
    Ok(views.html.signin());
  }



  def submit = Action { implicit request =>
    val (email, username, password, confirm) = form.bindFromRequest.get

    Ok("Hi %s %s".format(email, username))
  }


}
