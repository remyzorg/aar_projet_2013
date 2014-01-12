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
    )
      verifying (AppStrings.errEmailAlreadyInUse, result =>
        result match {
          case (email, _, _, _) =>
            UserModel.findByEmail(email) match {
              case None => true
              case _ => false
            }})
      verifying (AppStrings.errUsernameAlreadyInUse, result =>
        result match
        {case (_, username, _, _) =>
          UserModel.findByUsername(username) match {
            case None => true
            case Some (_) => false
          }
        })
      verifying (AppStrings.errNotMatchingPasswords, result =>
        result match
        {case (_, _, password, confirm) => password == confirm;
        })

  )

  def setup = Action { implicit request =>
    Ok(views.html.signup(form))
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold (
      errors => BadRequest(views.html.signup(errors)),
      user => user match { case (email, username, password, confirm) =>
        val user = User (new ObjectId(),
          email, username, Transaction.START_CAPITAL,
          Map(), Nil, 0, Nil, Achievements.signup.id :: Nil)
        UserModel.create (user, password);

        AchievementsUnlocker.unlockSignup(user);

        Redirect(routes.Application.index)
          .withSession(Security.username -> email)
      }
    )
  }


}
