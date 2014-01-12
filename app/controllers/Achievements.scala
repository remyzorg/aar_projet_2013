package controllers

import models._
import models.Achievement
import models.User
import org.bson.types.ObjectId

object AchievementsUnlocker {

  def unlockMessage(owner : ObjectId, achievement : Achievement) = Message(
    new ObjectId(),
    owner,
    false,
    "Achievement unlocked",
    achievement.name + " : " + achievement.desc
  )

  def simplyUnlockAchievement(user : User, achievement : Achievement) = {
    if (!(user.achievements.exists {achId => achId == achievement.id})) {
      UserModel.addAchievement(user.email, achievement)

      Message.addMessage(unlockMessage(user.id, achievement))
    }
  }

  def unlockFirstBuy(user : User) = simplyUnlockAchievement(user, Achievements.firstBuy)
  def unlockFirstSell(user : User) = simplyUnlockAchievement(user, Achievements.firstSell)
  def unlockRiskyInvestments(user: User, rawScore) =
    if (rawScore > 0)
      simplyUnlockAchievement(user, Achievements.riskyInvestements)


  def unlockLotOfBuys(user: User) = {
    val buyNumber = user.transactions.foldLeft(0) {
      case (acc, transaction) => transaction.action match {
        case BuyAction => acc + 1
        case _ => acc
      }
    }

    println("buynumber : " + buyNumber)

    if(buyNumber >= 1000) {
      simplyUnlockAchievement(user, Achievements.thousandBuy)
    } else if(buyNumber >= 100) {
      simplyUnlockAchievement(user, Achievements.hundredthBuy)
    } else if(buyNumber >= 10) {
      simplyUnlockAchievement(user, Achievements.tenthBuy)
    }
  }

  def unlockLotOfSells(user : User) = {
    val sellNumber = user.transactions.foldLeft(0) {
      case (acc, transaction) => transaction.action match {
        case SellAction => acc + 1
        case _ => acc
      }
    }

    if(sellNumber >= 1000) {
      simplyUnlockAchievement(user, Achievements.thousandSell)
    } else if(sellNumber >= 100) {
      simplyUnlockAchievement(user, Achievements.hundredthSell)
    } else if(sellNumber >= 10) {
      simplyUnlockAchievement(user, Achievements.tenthSell)
    }
  }

  def unlockMoneyGain(user: User, rawScore : Int) = {
    if (rawScore < 0)
      simplyUnlockAchievement(user, Achievements.lostMoney)
    else if (rawScore == 1)
      simplyUnlockAchievement(user, Achievements.smallGain)
    else if (rawScore == 2)
      simplyUnlockAchievement(user, Achievements.goodGain)
    else if (rawScore == 10)
      simplyUnlockAchievement(user, Achievements.doubleGain)
  }
}
