package controllers


import play.api._
import play.api.mvc._
import models._

object Debug extends Controller with Secured{


  def opCapital (value : Double, op : Boolean) =
    withUser {user => implicit request =>

    if (op) UserModel.opCapital(user.email, value, (_+_))
    else UserModel.opCapital(user.email, value, (_-_));

    val opstr = if (op) "adding" else "substracting"
    Ok(opstr + " " + value + " to capital" + "\n" +
      "current: " + user.capital)
  }


  def opQuote (from : String, value : Int, op : Boolean) =
    withUser { user => implicit request =>
      if (op) UserModel.opQuoteByCompany(user.email, from, value, (_+_))
      else UserModel.opQuoteByCompany(user.email, from, value, (_-_));
      val opstr = if (op) "adding" else "substracting"
      val current = user.quotes.get(from) match {
        case None => 0 case Some (i) => i}
      Ok(opstr + " " + value + " to " + from + "\n" +
        "current: " + current)
    }

  def opTransaction (from: String, price: Double, number: Int, action: String) = 
    withUser { user => implicit request =>
      val fromUpper = from.toUpperCase
      val (tradePrice, operation) =
        action match {
          case "sell" =>
            (price * 0.97, Transaction.sell _)
          case "buy" =>
            (price * 1.03, Transaction.buy _)
        }

      val (earned, score) = 
        operation(user.email, fromUpper, price, number, tradePrice)
      Ok("Added the following transaction:\n"
        + "quote: " + from + "\n"
        + "price: " + price + "\n"
        + "number: " + number + "\n"
        + "tradePrice: " + tradePrice + "\n"
        + "action: " + action + "\n"
        + "Resulting in " + earned + " points, giving a total of "
        + score + " point")
  }


  def print = Action {
    UserModel.printAll;
    Ok("" + UserModel.stringAll)
  }
  
  def delete = Action { implicit request =>
    UserModel.deleteAll
    Ok(views.html.home())
  }

  def update = Action { implicit request =>
    UserModel.updateAll
    Ok(views.html.home())
  }

}
