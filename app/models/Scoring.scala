package models

import scala.concurrent._
import scala.concurrent.duration._


case class RankingInfo(username: String, capital: Double, score: Int)

/**
  * Computes all the scoring
  */
object Scoring {

  /**
    * In the case of selling, computes the gain or loss
    */
  def transactionValue(price: Double, transaction: TransactionObject) = {
  
    val diff = price - transaction.price
    val percent = (Math.abs(diff) / transaction.price) * 100
    val score = 
      if (percent < 5) 1
      else if (percent < 10) 2
      else if (percent < 15) 3
      else if (percent >= 100) 10
      else 4
  
    if (diff < 0) -score else score
  }

  /**
    * Evaluates the risk taken while buying a stock
    */
  def riskValue(tradePrice: Double, from: String): Int = {
    val req = Historic.request(from)
    try {
      val result = Await result(req, 3 seconds)
      val history = Historic.parseResponse(result)
      val sum = history.map({value => value._2}).sum
      val mean = sum / history.length
      println(tradePrice + "; " + mean)

      val diff = tradePrice - mean
      val percent = (Math.abs(diff) / mean) * 100
      val score = 
        if (percent < 5) 0
        else if (percent < 15) 2
        else 4

      if (diff > 0) score else 0 //No risk since it is lower than usual
    } catch {
      case e: Throwable => 0
    }
  }

  def updateScoreSell(user: User, from: String, price: Double, number: Int,
    tradePrice: Double) : (Int, Int, Int) = {
    val transaction = UserModel.getLastTransactionFrom(user, from, Transaction.BUY_ACTION)

    transaction match {
      case Some(tr) =>
        val rawScore = transactionValue(price, tr)
        val score = rawScore * number
        UserModel.opIncScore(user.email, score)
        (rawScore, score, user.score + score)
      case None => (0, 0, user.score)
    }
  }


  def updateScoreBuy(user: User, from: String, price: Double, number: Int,
    tradePrice: Double): (Int, Int, Int) = {
    val rawScore = riskValue(tradePrice, from)
    val score = rawScore * number
    UserModel.opIncScore(user.email, score)
    (rawScore, score, user.score + score)
  }

  def ranking(user : User): List[RankingInfo] = {
    val friends = user.friends.map {
      username =>
      val friend = UserModel.findByUsername(username)
      friend match {
        case Some(friend) =>
          RankingInfo(friend.username, friend.capital, friend.score)
        case None => throw new UserNotFound(username)
      }
    }

    val users: List[RankingInfo] = RankingInfo(user.username, user.capital, user.score) :: friends
    users.sortBy({ user => user.score }).reverse
  }

}
