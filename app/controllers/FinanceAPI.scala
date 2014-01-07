package controllers

import play.api._
import play.api.mvc._

import models.Finance
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._

object FinanceAPI extends Controller {

  def multipleQuotes = Action.async { implicit request =>
    val resp = models.Quote.request("goog" :: "yhoo" :: "msft" :: "aapl" :: Nil)
    resp.map {
      response =>
      val result = models.Quote.parseResponse(response)
      try {
        val values = models.Quote.getMultipleValues(result)
        Ok(views.html.quotes(values))
      } catch {
        case e: JsResultException => 
          BadRequest("The quotes you requested doesn't have value, or the external API responded badly")
      }
    }
  }

  def currency(from: String, to: String) = Action.async { implicit request =>
    val resp = models.Currency.request(from, to)
    resp.map { response =>
      val (id, rate) = models.Currency.parseResponse(response)
      Ok(views.html.finance(id.as[String], rate.as[String]))
    }
  }

  def quote(name: String) = Action.async { implicit request =>
    val resp = models.Quote.request(name)
    resp.map { response =>
      val result = models.Quote.parseResponse(response)
      try {
        val company = (result \ "Symbol").as[String]
        val bid = models.Quote.getBidPrice(result)
        val ask = models.Quote.getAskPrice(result)
        Ok(views.html.quote(company, bid.as[String], ask.as[String]))
      } catch {
        case e: JsResultException => 
          BadRequest("The Quote requested doesn't exist or have value")
      }
    }
  }

  def history(name: String) = Action.async { implicit request =>
    val resp = models.Historic.request(name)
    resp.map { response =>
      try {
        val result = models.Historic.parseResponse(response)
        Ok(views.html.history(name, result))
      } catch {
        case e: JsResultException =>
          BadRequest("The Quote requested doesn't exist or have value")
      }
    }
  }

  // def quoteWithHistory(name: String) = Action.async { implicit request =>
  //   val resp = models.Quote.request(name)
  //   resp.map { response =>
  //     val result = models.Quote.parseResponse(response)
  //     val company = (result \ "Symbol").as[String]
  //     val bid = models.Quote.getBidPrice(result)
  //     val ask = models.Quote.getAskPrice(result)
  //     // Ok(views.html.quote(company, bid.as[String], ask.as[String]))
  //     val respHist = models.Historic.request(name)
  //     resp.map { response =>
  //       val resulthist = models.Historic.parseResponse(response)
  //       Ok(views.html.history(name, resultHist))
  //   }
  //   }
  // }

}
