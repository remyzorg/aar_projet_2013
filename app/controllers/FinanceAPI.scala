package controllers

import play.api._
import play.api.mvc._

import models.Finance
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json

object FinanceAPI extends Controller {

  def multipleQuotes = Action.async {
    val resp = models.Quote.request("goog" :: "yhoo" :: "msft" :: "appl" :: Nil)
    resp.map {
      response =>
      val result = models.Quote.parseResponse(response)
      Ok(Json.prettyPrint(result))
    }
  }

  def currency(from: String, to: String) = Action.async { 
    val resp = models.Currency.request(from, to)
    resp.map { response =>
      val (id, rate) = models.Currency.parseResponse(response)
      Ok(views.html.finance(id.as[String], rate.as[String]))
    }
  }

  def quote(name: String) = Action.async {
    val resp = models.Quote.request(name)
    resp.map { response =>
      val result = models.Quote.parseResponse(response)
      val company = (result \ "Symbol").as[String]
      val price = models.Quote.getPrice(result)
      Ok(views.html.quote(company, price.as[String]))
    }
  }

  def history(name: String, from: String, to: String) = Action.async {
    val resp = models.Historic.request(name, from, to)
    resp.map { response =>
      val result = models.Historic.parseResponse(response)
      Ok(Json.prettyPrint(result))
    }
  }

}
