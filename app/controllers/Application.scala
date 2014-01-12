package controllers

import scala.concurrent._

import play.api._
import play.api.libs.json._
import play.api.mvc._

import models.{Message, Finance, UserModel}
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller with SecuredAsync {

  def index = Action { implicit request =>
    session.get(Security.username) match {
      case Some (_) => Redirect(routes.Application.indexAuth)
      case None => Ok(views.html.home())
    }
  }

  def rules = Action { implicit request =>
    Ok(views.html.rules())
  }

  def indexAuth = withUser { user_data => implicit request =>
    Future.traverse(user_data.quotes) {
      case (quote, quantity) => for {
        quoteInfoHistory <- FinanceAPI.rawQuoteWithHistory(quote).recover {
          case e :  JsResultException => None }
      } yield (quote, quantity, quoteInfoHistory)
    }.map {values =>
      val messages = Message.getAllUnreadMessages(user_data).toList
      for(message <- messages) Message.setRead(message)
      val achievements = user_data.achievements.map 
      { ach => models.Achievements.toAchievement(ach) }
      Ok(views.html.user_home(user_data, messages, values, achievements))
    }
  }
}
