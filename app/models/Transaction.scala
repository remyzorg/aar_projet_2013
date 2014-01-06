package models

import play.api.libs.json._
import play.api.libs.ws.WS._
import play.api.libs.ws._

object Transaction {

  def buy(from: String, price: Double, number: Int) = {
    // What to do when buying
    // Bdd updates, verifications, etc
    true
  }

  def sell(from: String, price: Double, number: Int) = {
    // What to do when selling
    // Bdd updates, verifications, etc
    true
  }

}
