package models

object Scoring {

  def transactionValue(price: Double, transaction: TransactionObject) = {

    val diff = (transaction.price / price) * 100
    val score = 
      if (diff < 5) 1
      else if (diff < 10) 2
      else if (diff < 15) 3
      else 4
  
    val sign = transaction.price - price
    if (sign < 0) -score else score
  }

  def updateScore(user: User, from: String, price: Double, number: Int) {
    val transaction = UserModel.getLastTransactionFrom(user, from, Transaction.BUY_ACTION)

    transaction match {
      case Some(tr) =>
        val score = transactionValue(price, tr) * number
        val prevScore = try {
          user.score
        } catch {
          case e => 0
        }
        UserModel.opScore(user.email, prevScore + score)
      case None => ()
    }
  }

}
