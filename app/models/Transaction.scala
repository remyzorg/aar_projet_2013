package models

import play.api.libs.json._
import play.api.libs.ws.WS._
import play.api.libs.ws._
import controllers.AchievementsUnlocker


class TransactionException(msg: String) extends RuntimeException(msg)
class TransactionNotConnected extends RuntimeException

abstract sealed class OpAction ()
case object SellAction extends OpAction
case object BuyAction extends OpAction

object Transaction {

  val START_CAPITAL = 10000.0
  val LIMIT = -1000.0
  val SELL_ACTION = "sell"
  val BUY_ACTION = "buy"

  def stringOfAction(act: OpAction) = 
    act match {
      case SellAction => "sell"
      case BuyAction => "buy"
    }

  def buy(email : String, from: String, price: Double, number: Int, tradePrice: Double) = {
    
    val money : Double = price * number
    println(money + " " + price + " " + number)

    val user = UserModel.findByEmail(email) match {
      case None => throw new TransactionNotConnected
      case Some (u) => u
    }
    if (user.capital < 0 || (user.capital - money) < LIMIT)
      throw new TransactionException (AppStrings.errNotEnoughMoney)

    UserModel.opCapital(email, money, (_-_))
    UserModel.opQuoteByCompany(email, from, number, (_+_))
    UserModel.opTransaction(email, BuyAction, from, price, number)

    AchievementsUnlocker.unlockFirstBuy(user)
    AchievementsUnlocker.unlockLotOfBuys(user)

    val (rawScore, earned, score) = Scoring.updateScoreBuy(user, from, price, number, tradePrice)

    if (!(user.achievements.exists {achId => achId == Achievements.riskyInvestments.id}))
      UserModel.addAchievement(email, Achievements.riskyInvestments)

    (earned, score)
  }


  def sell(email : String, from: String, price: Double, number: Int, tradePrice:
      Double) = {
    val money = price * number

    val user = UserModel.findByEmail(email) match {
      case None => throw new TransactionNotConnected
      case Some (u) => u
    }

    user.quotes.get(from) match {
      case None => throw new TransactionException(AppStrings.errNotEnoughQuote)
      case Some (i) =>
        if (i < number) 
          throw new TransactionException(AppStrings.errNotEnoughQuote)
    }

    UserModel.opCapital(email, money, (_+_))
    UserModel.opQuoteByCompany(email, from, number, (_-_))
    UserModel.opTransaction(email, SellAction, from, price, number)

    AchievementsUnlocker.unlockFirstSell(user)
    AchievementsUnlocker.unlockLotOfSells(user)

    val (rawScore, earned, score) = Scoring.updateScoreSell(user, from, price, number, tradePrice)

    AchievementsUnlocker.unlockFirstSell(user)
    AchievementsUnlocker.unlockLotOfSells(user)
    AchievementsUnlocker.unlockMoneyGain(user, rawScore)



    (earned, score)
  }

}
