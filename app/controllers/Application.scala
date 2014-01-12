package controllers

import scala.concurrent._

import play.api._
import play.api.libs.json._
import play.api.mvc._

import models.Finance
import models.UserModel
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller with SecuredAsync {

  // I had to avoid withUser for conditional rendering

  def index = Action { implicit request =>
    session.get(Security.username) match {
      case Some (_) => Redirect(routes.Application.indexAuth)
      case None => Ok(views.html.home())
    }
  }


  def indexAuth = withUser { user_data => implicit request =>
    Future.traverse(user_data.quotes) {
      case (quote, quantity) => for {
        quoteInfoHistory <- FinanceAPI.rawQuoteWithHistory(quote).recover {
          case e :  JsResultException => None }
      } yield (quote, quantity, quoteInfoHistory)
    }.map {values =>
      Ok(views.html.user_home(user_data, values))
    }
  }
}
