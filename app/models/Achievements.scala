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
  val doubleGain= 
    Achievement(6, "Investments master", "As if earning money wasn't enough, you even doubled your investment")

  val achievements = signup :: firstBuy :: firstSell :: lostMoney :: smallGain :: goodGain :: doubleGain :: Nil

  def toAchievement(id: Int) =
    achievements.find {ach => ach.id == id}

}
