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
      val (price, operation) =
        if (action == Transaction.BUY_ACTION)
          (Quote.getAskPrice(result), Transaction.buy _)
        else (Quote.getBidPrice(result), Transaction.sell _)

      try {
        operation(mail, fromUpper, price.as[String].toDouble, number)
        if (action == Transaction.BUY_ACTION)
          Ok(views.html.buy(fromUpper, number, price.as[String]))
        else Ok(views.html.sell(fromUpper, number, price.as[String]))
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


  // Thoses two following Actions are only for a debugging purpose

}
