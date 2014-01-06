
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
        verifying ("Wrong password", result =>
          result match
          {case (EditAccountData(password, newemail,
            newpassword, newusername)) =>
            request.session.get(Security.username) match {
              case Some (s) =>
                UserModel.findByEmailPassword(s, password) match {
                  case None => false
                  case _ => true
                }
              case None => false
            }
          })
    )
  }

  def setup_page (
    form : Form[EditAccountData], email : String, username : String
  ) = {implicit request : Request[Any]=>
    Ok(views.html.edit_account(form,email,username))
  }

  def getFillingContent = { implicit request : Request[Any]=>
    request.session.get(Security.username) match {
      case Some (s) =>
        UserModel.findByEmail(s) match {
          case None => None
          case Some(u) => Some (u.email, u.username)
        }
      case None => None
    }
  }

  def opCapital (value : Double, op : Boolean) = Action { implicit request =>
    request.session.get(Security.username) match {
      case Some (mail) =>
        if (op) UserModel.opCapital(mail, value, (_+_))
        else UserModel.opCapital(mail, value, (_-_));

        UserModel.findByEmail(mail) match {
          case None => onUnauthorized(request)
          case Some(u) =>
            val opstr = if (op) "adding" else "substracting"
            Ok(opstr + " " + value + " to capital" + "\n" +
              "current: " + u.capital)
        }
      case None => onUnauthorized(request)
    }
  }


  def opQuote (from : String, value : Int, op : Boolean) =
    Action { implicit request =>
    request.session.get(Security.username) match {
      case Some (mail) =>
        if (op) UserModel.opQuoteByCompany(mail, from, value, (_+_))
        else UserModel.opQuoteByCompany(mail, from, value, (_-_));
        UserModel.findByEmail(mail) match {
          case None => onUnauthorized(request)
          case Some(u) =>
            val opstr = if (op) "adding" else "substracting"
            Ok(opstr + " " + value + " to " + from + "\n" +
              "current: " + u.quotes.mkString(" "))
        }
      case None => onUnauthorized(request)
    }
  }
  

  def setup = Action { implicit request : Request[Any] =>
    getFillingContent(request) match {
      case Some ((email, username)) =>
        setup_page(form(request), email, username)(request)
      case None => onUnauthorized(request)
    }
  }

  def edit = Action { implicit request =>
    form(request).bindFromRequest.fold (
      errors => getFillingContent(request) match {
        case Some ((email, username)) =>
          BadRequest(views.html.edit_account(errors, email, username))
        case None => onUnauthorized(request)
      } ,
      user => user match {
        case (EditAccountData(_, newemail, newpassword, newusername)) =>
          request.session.get(Security.username) match {
            case Some (email) =>
              UserModel.updateByEmail(email, newemail, newpassword, newusername)
              getFillingContent(request) match {
                case Some ((email, username)) =>
                  setup_page(form(request), email, username)(request)
                case None => onUnauthorized(request)
              }
            case None => onUnauthorized(request)
          }
      }
    )
  }


}

