package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.bson.types.ObjectId
import models._



object Signin extends Controller {

  val form = Form(
    tuple(
      "email" -> text,
      "username" -> text,
      "password" -> text,
      "confirm" -> text
    ) verifying ("Passwords must match", result =>
      result match
      {case (_, _, password, confirm) => password == confirm;}

    ) verifying ("This e-mail is already in use", result =>
      result match {
        case (email, _, _, _) =>
          UserModel.findByEmail(email) match {
            case None => true
            case _ => println ("HEHO"); false
          }})
  )

  def setup = Action {
    UserModel.printAll;
    Ok(views.html.signin(form))
  }

  
  def delete = Action {
    UserModel.deleteAll
    Ok(views.html.home())
  }


  def submit = Action { implicit request =>
    form.bindFromRequest.fold (
      errors => BadRequest(views.html.signin(errors)),
      user => user match { case (email, username, password, confirm) =>
        UserModel.create (User (new ObjectId(),
          email, username), password);
        Ok(views.html.home())
      }
    )
  }


}
