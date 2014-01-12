package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

case class FriendData (username: String)

object Friend extends Controller with Secured {

  def form (user: User)(implicit request : Request[Any]) = {
    Form(
      mapping(
        "username" -> nonEmptyText
      ) (FriendData.apply)(FriendData.unapply)
        verifying (AppStrings.errUserNotFound, result =>
          result match
          {case (FriendData(username)) =>
            UserModel.findByUsername(username) match {
              case None => false
              case Some (_) => true
            }
          }
        )
        verifying (AppStrings.errFriendAlreadyAdded, result =>
          result match
          {case (FriendData(username)) =>
            !(user.friends.contains(username))
          })
    )
  }

  def addFriend = withUser { user => implicit request =>
    Ok(views.html.add_friend(form(user)))
  }

  def addFriendPost = withUser { user => implicit request =>
    form(user)(request).bindFromRequest.fold (
      errors => BadRequest(views.html.add_friend(errors)),
      edit => edit match {
        case FriendData(username) =>
          UserModel.addFriend(user.email, username)
          Ok(views.html.add_friend(form(user)(request)))
      }
    )
  }


  def profile(targetUsername: String) =
    withUser { user => implicit request =>

      if (user.friends.contains(targetUsername)){
        Ok(views.html.friend_profile(targetUsername))
      }
      else
        Redirect(routes.Friend.addFriend)
  }

  def ranking = withUser { user => implicit request =>
    Ok(views.html.ranking(Scoring.ranking(user)))
  }

}
