package models

case class Achievement(id: Int, name: String, desc: String)

object Achievements {

  val signup = 
    Achievement(0 ,"Signing Up", "You successfuly signed up on Yolo! Welcome aboard!")
  val firstBuy =
    Achievement(1, "First buy!", "Whoa, you bought your first stock, congratulations!")
  val firstSell =
    Achievement(2, "First sell!", "Maybe is it the beginning of your fortune?")
  val lostMoney =
    Achievement(3, "Money lost", "Well, that wasn't a good investments finally.")
  val smallGain = 
    Achievement(4, "Small gain", "You've earned less than 5% of the original price.")
  val goodGain =
    Achievement(5, "Not so small gain", "Hey, you've earned at least 5% of the original price you bought, that's good!")
  val doubleGain =
    Achievement(6, "Investments master", "As if earning money wasn't enough, you even doubled your investment")
  val tenthBuy =
    Achievement(7, "Good call taker", "You've bought your tenth stock, monopoly has no secrets for you isn't it ?")
  val hundredthBuy =
    Achievement(8, "Money whisperer", "You've bought your hundredth stock, well your jewellery box is growing.")
  val thousandBuy =
    Achievement(9, "Begin to buy", "At your thousandth stock bought, you're just starting to earn, at last.")
  val tenthSell =
    Achievement(10, "Good servant", "You've sold your tenth stock, you've sold as much as your neighbor at the last yard sale !")
  val hundredthSell =
    Achievement(11, "Sell me this pen", "You've sold your hundredth stock, you can now buy this book that explain what is trading.")
  val thousandSell =
    Achievement(12, "Begin to sell", "Thousandth sell, impressive, welcome to the beginning of trading.")
  val allNight =
    Achievement(13, "All nighter", "You're trading buy night, indeed, stocks won't trade by themselves.")

  val achievements = signup :: firstBuy :: firstSell :: lostMoney :: smallGain :: goodGain :: doubleGain ::
    tenthBuy :: hundredthBuy :: thousandBuy :: tenthSell :: hundredthSell :: thousandSell :: allNight :: Nil

  def toAchievement(id: Int) =
    achievements.find {ach => ach.id == id}

}
