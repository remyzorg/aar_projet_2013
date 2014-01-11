package models

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
