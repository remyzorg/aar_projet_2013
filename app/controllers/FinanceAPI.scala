package controllers

import play.api._
import play.api.mvc._

import models.Finance
import models.QuoteInfo
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.IntegerDeserializer





object FinanceAPI extends Controller {


  def rawMultipleQuotes(quotes : List[String]) : Future[List[QuoteInfo]] = {
    val resp = models.Quote.request(quotes)
    resp.map {
      response =>
      println(response.json)
      val result = models.Quote.parseResponse(response)
      val values = models.Quote.getMultipleValues(result)
      values
    }
  }

  def multipleQuotes = Action.async { implicit request =>
    val quotes = "goog" :: "yhoo" :: "msft" :: "aapl" :: "aal" :: Nil
    rawMultipleQuotes(quotes).map { values =>
        Ok(views.html.quotes(values))
    }.recover {
      case e: JsResultException => 
        BadRequest(
          views.html.error("The quotes you requested doesn't have value"
            + ", or the external API responded badly"))
    }
  }

  def currency(from: String, to: String) = Action.async { implicit request =>
    val resp = models.Currency.request(from, to)
    resp.map { response =>
      val (id, rate) = models.Currency.parseResponse(response)
      Ok(views.html.finance(id.as[String], rate.as[String]))
    }
  }

  def rawQuote(name: String) : Future[Option[QuoteInfo]] = {
    val resp = models.Quote.request(name)
    resp.map { response =>
      println("Hey hey hey !\n")
      val result = models.Quote.parseResponse(response)
      println("Hey !\n" + result)
      println("querying " + name)
      println("result : " + result)
      Some(models.Quote.getQuoteInfo(result))
    }
  }

  def quote(name: String) = Action.async { implicit request =>
    rawQuote(name).map {
      case Some(quoteInfo) => Ok(views.html.quote(quoteInfo, Nil))
      case None => 
        BadRequest(
          views.html.error("The Quote requested doesn't exist or have value"))
    }.recover {
      case e : JsResultException => 
        BadRequest(
          views.html.error("The Quote requested doesn't exist or have value"))
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
          BadRequest(
            views.html.error("The Quote requested doesn't exist or have value"))
      }
    }
  }

  def rawQuoteWithHistory(name: String) = {
    val resp = models.Quote.request(name)
    val respHist = models.Historic.request(name)
    val responseHist = Await result(respHist, 10 seconds)
    resp.map { response =>
      val result = models.Quote.parseResponse(response)
      val resultHist = models.Historic.parseResponse(responseHist)
      val quoteInfo = models.Quote.getQuoteInfo(result)
      Some((quoteInfo, resultHist))
    }
  }

  def quoteWithHistory(name: String) = Action.async { implicit request =>
    rawQuoteWithHistory(name).map {
      case Some((quoteInfo, resultHist)) => Ok(views.html.quote(quoteInfo, resultHist))
      // case None should never happen actually
    }.recover {
      case e: JsResultException =>
        BadRequest(
          views.html.error("The Quote requested doesn't exist or have value"))
    }
  }
}
