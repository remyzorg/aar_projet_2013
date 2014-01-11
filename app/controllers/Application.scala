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
  def index = Action.async { implicit request =>
    request.session.get(Security.username).map { username =>
      UserModel.findByEmail(username).map { user_data =>
        Future.traverse(user_data.quotes) {
          case (quote, quantity) => for {
            quoteInfoHistory <- FinanceAPI.rawQuoteWithHistory(quote).recover {
              case e :  JsResultException => None }
          } yield (quote, quantity, quoteInfoHistory)
        }.map {values =>
          Ok(views.html.user_home(user_data, values))
        }
      }.getOrElse { future  { BadRequest("internal error") } }
    }.getOrElse {
      future { Ok(views.html.home()) }
    }
  }
}
