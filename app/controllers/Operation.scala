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
      val result = Quote.parseResponse(response)
      val quoteInfo = Quote.getQuoteInfo(result)
      val price = quoteInfo.askRealtime
      // The price.as[Double] doesn't work, for now it is the only solution
      try {
        Transaction.buy(mail, from, price, number)
        Ok(views.html.buy(from, number, price.toString))
      }
      catch {
        case e: TransactionException =>
          Ok(e.getMessage)
        case e: TransactionNotConnected => onUnauthorized(request)
        case e: JsResultException =>
          BadRequest(views.html.error(e.getMessage))
      }
    }
  }
}
