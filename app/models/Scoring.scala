package models

case class RankingInfo(username: String, capital: Double, score: Int)

object Scoring {

  def transactionValue(price: Double, transaction: TransactionObject) = {
  
    val diff = price - transaction.price
    val percent = (Math.abs(diff) / transaction.price) * 100
    val score = 
      if (percent < 5) 1
      else if (percent < 10) 2
      else if (percent < 15) 3
      else 4
  
    if (diff < 0) -score else score
  }

  def updateScore(user: User, from: String, price: Double, number: Int) {
    val transaction = UserModel.getLastTransactionFrom(user, from, Transaction.BUY_ACTION)

    transaction match {
      case Some(tr) =>
        val score = transactionValue(price, tr) * number
        UserModel.opScore(user.email, user.score + score)
      case None => ()
    }
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
