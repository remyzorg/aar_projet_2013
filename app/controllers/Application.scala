package controllers

import scala.concurrent._

import play.api._
import play.api.libs.json._
import play.api.mvc._

import models.Finance
import models.UserModel
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller with SecuredAsync {

  def index = Action { implicit request =>
    Ok(views.html.home())
  }

  def user_home = withUser {
    user_data =>
    implicit request =>

    Future.traverse(user_data.quotes) {
      case (quote, quantity) => for {
        quoteInfo <- FinanceAPI.rawQuote(quote).recover {
          case e :  JsResultException => None }
      } yield (quote, quantity, quoteInfo)
    }.map {values =>
      Ok(views.html.user_home(user_data, values))
    }
  }
}
