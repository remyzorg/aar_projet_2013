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
      val bid = models.Quote.getBidPrice(result)
      val ask = models.Quote.getAskPrice(result)
      Ok(views.html.quote(company, bid.as[String], ask.as[String]))
    }
  }

  def history(name: String) = Action.async { implicit request =>
    val resp = models.Historic.request(name)
    resp.map { response =>
      // val result = models.Historic.parseResponse(response)
      val result = models.Historic.getWeekHistory(response)
      Ok(views.html.history(name, result))
    }
  }

}
