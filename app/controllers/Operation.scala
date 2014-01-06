package controllers

import play.api._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import models._

object Operation extends Controller {

  def buyStock(from: String, number: Int) = Action.async { implicit request =>
    val resp = Quote.request(from)
    resp.map { response =>
      val result = Quote.parseResponse(response)
      val price = Quote.getBidPrice(result)
      // The price.as[Double] doesn't work, for now it is the only solution
      val res = Transaction.buy(from, price.as[String].toDouble, number)
      Ok(views.html.buy(from, number, price.as[String]))
    }
  }

  def sellStock(from: String, number: Int) = Action.async { implicit request =>
    val resp = Quote.request(from)
    resp.map { response =>
      val result = Quote.parseResponse(response)
      val price = Quote.getAskPrice(result)
      // The price.as[Double] doesn't work, for now it is the only solution
      val res = Transaction.sell(from, price.as[String].toDouble, number)
      Ok(views.html.sell(from, number, price.as[String]))
    }
  }

}
