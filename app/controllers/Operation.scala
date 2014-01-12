package controllers

import play.api._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.json.JsResultException
import models._


object Operation extends Controller with SecuredAsync {

  def opStock (action: String, from: String, number: Int) = withAuth {
    mail =>
    implicit request =>

    val fromUpper = from.toUpperCase
    val resp = Quote.request(fromUpper)

    resp.map { response =>
      try {
        val result = Quote.parseResponse(response)
        val quoteInfo = Quote.getQuoteInfo(result)
      
        val (price, operation) =
          if (action == Transaction.BUY_ACTION)
            (quoteInfo.askRealtime, Transaction.buy _)
          else (quoteInfo.bidRealtime, Transaction.sell _)

        val tradePrice = quoteInfo.lastTradePrice match {
          case Some(v) => v
          case None => 0.0
        }

        price match {
          case Some(price) =>
            {
              try {
                val (earned, oldScore) = operation(mail, fromUpper, price, number, tradePrice)
                if (action == Transaction.BUY_ACTION)
                  Ok(views.html.buy(fromUpper, number, price.toString, oldScore, earned))
                else Ok(views.html.sell(fromUpper, number, price.toString, oldScore, earned))
              }
              catch {
                case e: TransactionException =>
                  Ok(e.getMessage)
                case e: TransactionNotConnected => onUnauthorized(request)
                case e: JsResultException =>
                  BadRequest(views.html.error(e.getMessage))
              }
            }
          case None =>
            BadRequest(views.html.error("The price for this action is unavailable"))
        }
      } catch {
        case e: JsResultException => 
          BadRequest(views.html.error("The price for this action is unavailable"))
      }
    }
  }
}
