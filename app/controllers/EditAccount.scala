
package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.bson.types.ObjectId
import models._

case class EditAccountData (
  password : String,
  newemail : Option[String],
  newpassword : Option[String],
  newusername : Option[String]
)

object EditAccount extends Controller with Secured {

  def form = {implicit request : Request[Any] =>
    Form(
      mapping(
        "password" -> nonEmptyText,
        "newemail" -> optional(text),
        "newpassword" -> optional(text),
        "newusername" -> optional(text)
      ) (EditAccountData.apply)(EditAccountData.unapply)
        verifying (AppStrings.errEmptyFields, result =>
          result match {
            case (EditAccountData(_, None, None, None)) => false
            case _ => true
          })
        verifying (AppStrings.errWrongPassword, result =>
          result match
          {case (EditAccountData(password, _, _, _)) =>
            request.session.get(Security.username) match {
              case Some (s) =>
                UserModel.findByEmailPassword(s, password) match {
                  case None => false
                  case _ => true
                }
              case None => false
            }
          })
        verifying (AppStrings.errEmailAlreadyInUse, result =>
          result match
          {case (EditAccountData(_, newemail, _, _)) =>
            newemail match {
              case None => true
              case Some (s) =>
                UserModel.findByEmail(s) match {
                  case None => true
                  case Some (_) => false
                }
          }
        })
        verifying (AppStrings.errUsernameAlreadyInUse, result =>
          result match
          {case (EditAccountData(_, _, _, newusername)) =>
            newusername match {
              case None => true
              case Some (s) =>
                UserModel.findByUsername(s) match {
                  case None => true
                  case Some (_) => false
                }
            }
        })

    )
  }


  def setup = withUser { user => implicit request =>
    Ok(views.html.edit_account(form(request), user.email, user.username))
  }

  def edit = withUser { user => implicit request =>
    form(request).bindFromRequest.fold (
      errors => BadRequest(views.html.edit_account(errors,
        user.email, user.username)),
      edit => edit match {
        case (EditAccountData(_, newemail, newpassword, newusername)) =>
          UserModel.updateByEmail(user.email, newemail, newpassword,
            newusername)
          Ok(views.html.edit_account(form(request), user.email, user.username))
      }
    )
  }


}

