package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.bson.types.ObjectId
import models._



object Signup extends Controller {

  val form = Form(
    tuple(
      "email" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "confirm" -> nonEmptyText
    ) verifying ("Passwords must match", result =>
      result match
      {case (_, _, password, confirm) => password == confirm;}
    ) verifying ("This e-mail is already in use", result =>
      result match {
        case (email, _, _, _) =>
          UserModel.findByEmail(email) match {
            case None => true
            case _ => false
          }})
  )

  def setup = Action { implicit request =>
    Ok(views.html.signup(form))
  }

  def print = Action {
    UserModel.printAll;
    Ok("" + UserModel.stringAll)
  }
  
  def delete = Action { implicit request =>
    UserModel.deleteAll
    Ok(views.html.home())
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold (
      errors => BadRequest(views.html.signup(errors)),
      user => user match { case (email, username, password, confirm) =>
        UserModel.create (User (new ObjectId(),
          email, username), password);
        Redirect(routes.Application.index)
          .withSession(Security.username -> email)
      }
    )
  }


}
