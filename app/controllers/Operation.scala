package controllers

import play.api._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import models._

object Operation extends Controller {

  def buyStock(from: String, number: Int) = Action.async {
    val resp = Quote.request(from)
    resp.map { response =>
      val result = Quote.parseResponse(response)
      val price = Quote.getPrice(result)
      Ok(views.html.buy(from, number, price.as[String]))
    }
  }

}
