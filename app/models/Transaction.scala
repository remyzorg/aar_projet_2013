package models

import play.api.libs.json._
import play.api.libs.ws.WS._
import play.api.libs.ws._

object Transaction {


  val START_CAPITAL = 10000.0



  def buy(email : String, from: String, price: Double, number: Int) = {
    // What to do when buying
    // Bdd updates, verifications, etc
    true
  }

  def sell(email : String, from: String, price: Double, number: Int) = {
    // What to do when selling
    // Bdd updates, verifications, etc
    true
  }

}
