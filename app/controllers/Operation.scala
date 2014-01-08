package controllers

import play.api._
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import models._


object Operation extends Controller with Secured {

  def buyStock(from: String, number: Int) = Action.async { implicit request =>
    val resp = Quote.request(from)

    resp.map { response =>
      val result = Quote.parseResponse(response)
      val price = Quote.getBidPrice(result)
      // The price.as[Double] doesn't work, for now it is the only solution
      Auth.getUser match {
        case Some (mail) =>
          val res = Transaction.buy(mail, from, price.as[String].toDouble,
            number)
          Ok(views.html.buy(from, number, price.as[String]))
        case None => onUnauthorized(request)
      }
    }
  }

  def sellStock(from: String, number: Int) = Action.async { implicit request =>
    val resp = Quote.request(from)

    resp.map { response =>
      val result = Quote.parseResponse(response)
      val price = Quote.getAskPrice(result)
      // The price.as[Double] doesn't work, for now it is the only solution
      
      Auth.getUser match {
        case Some (mail) =>
          val res = Transaction.sell(mail, from, price.as[String].toDouble,
            number)
          Ok(views.html.sell(from, number, price.as[String]))
        case None => onUnauthorized(request)
      }
    }
  }


  def opCapital (value : Double, op : Boolean) = Action { implicit request =>
    Auth.getUser match {
      case Some (mail) =>
        if (op) UserModel.opCapital(mail, value, (_+_))
        else UserModel.opCapital(mail, value, (_-_));

        UserModel.findByEmail(mail) match {
          case None => onUnauthorized(request)
          case Some(u) =>
            val opstr = if (op) "adding" else "substracting"
            Ok(opstr + " " + value + " to capital" + "\n" +
              "current: " + u.capital)
        }
      case None => onUnauthorized(request)
    }
  }


  def opQuote (from : String, value : Int, op : Boolean) =
    Action { implicit request =>
    Auth.getUser match {
      case Some (mail) =>
        if (op) UserModel.opQuoteByCompany(mail, from, value, (_+_))
        else UserModel.opQuoteByCompany(mail, from, value, (_-_));
        UserModel.findByEmail(mail) match {
          case None => onUnauthorized(request)
          case Some(u) =>
            val opstr = if (op) "adding" else "substracting"
            val current = u.quotes.get(from) match {
              case None => 0 case Some (i) => i}
            Ok(opstr + " " + value + " to " + from + "\n" +
              "current: " + current)
        }
      case None => onUnauthorized(request)
    }
  }

}
