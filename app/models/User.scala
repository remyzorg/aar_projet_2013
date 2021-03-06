package models

import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import scala.language.reflectiveCalls
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat


case class User (
  id : ObjectId,
  email : String,
  username : String,
  capital : Double,
  quotes : Map[String, Int],
  transactions : List[TransactionObject],
  score : Int,
  friends : List[String],
  achievements : List[Int]
)

case class TransactionObject (
  // id : ObjectId,
  action : OpAction,
  quote : String,
  price : Double,
  number : Int,
  capital : Double,
  date : String
)

class UserNotFound(user: String) extends RuntimeException(user)

object UserModel {
  import com.github.t3hnar.bcrypt._
  import com.mongodb.casbah.Imports._

  val email = "email"
  val id = "_id"
  val username = "username"
  val password = "password"
  val capital = "capital"
  val quotes = "quotes"
  val transactions = "transactions"
  val score = "score"
  val friends = "friends"
  val achievements = "achievements"
  
  val action = "action"
  val quote = "quote"
  val price = "price"
  val number = "number"
  val date = "date"

  def toTransaction(obj: DBObject) = 
    TransactionObject (
      // obj.getAs[ObjectId](id).get,
      if (obj.getAs[String](action).get == Transaction.BUY_ACTION)
        BuyAction else SellAction,
      obj.getAs[String](quote).get,
      obj.getAs[Double](price).get,
      obj.getAs[Int](number).get,
      obj.getAs[Double](capital).get,
      obj.getAsOrElse(date, "2014-01-12T00:00:00.316+01:00")
    )

  def createTransactionObject(tr : TransactionObject) =
    MongoDBObject (
      action -> (tr.action match {
        case BuyAction => "buy"
        case SellAction => "sell"
      }),
      quote -> tr.quote,
      price -> tr.price,
      number -> tr.number,
      capital -> tr.capital,
      date -> tr.date
    )

  def createTransactionList(tr : List[TransactionObject]) = {
    val builder = MongoDBList.newBuilder
    for (t <- tr) builder += tr;
    builder.result
  }

  def toUser(obj : DBObject) =
    User(
      obj.getAs[ObjectId](id).get,
      obj.getAs[String](email).get,
      obj.getAsOrElse(username, ""),
      obj.getAs[Double](capital) match {
        case Some (d) => d
        case None => 0.0
      },
      obj.getAs[DBObject](quotes) match {
        case Some (m : DBObject) => { val m2 : MongoDBObject = m;
          m2.toMap[String, AnyRef].asInstanceOf[Map[String, Int]]
        }
        case None => Map.empty[String, Int]
      }, 
      obj.getAs[MongoDBList](transactions) match {
        case Some (m : MongoDBList) => { val m2 : MongoDBList = m;
          m2.toList.asInstanceOf[List[DBObject]]
            .map { obj => toTransaction(obj) }
        }
        case None => Nil
      },
      obj.getAsOrElse(score, 0),
      obj.getAs[MongoDBList](friends) match {
        case Some (m : MongoDBList) => {
          m.toList.asInstanceOf[List[String]]// .map 
          // { obj => obj.getAs[String].get }
        }
        case None => Nil
      },
      obj.getAs[MongoDBList](achievements) match {
        case Some (m : MongoDBList) => {
          m.toList.asInstanceOf[List[Int]]// .map 
          // { obj => obj.getAs[String].get }
        }
        case None => Nil
      }
    )


  def create (user : User, newPassword : String) = {
    val cryptedPassword = newPassword.bcrypt

    val obj =
      MongoDBObject (
        email -> user.email, 
        username -> user.username, 
        password -> cryptedPassword,
        capital -> user.capital,
        quotes -> user.quotes.asDBObject,
        transactions -> createTransactionList(user.transactions),
        score -> user.score,
        friends -> user.friends,
        achievements -> user.achievements
      )
    Database.user.save(obj)
  }


  def findById(targetId : String) = {
    val obj = Database.user.findOne(MongoDBObject(id -> targetId))

    obj match {
      case Some(userObj) => 
        if(userObj.containsField(email)) toUser(userObj) else None
      case None => None
    }
  }


  def findByEmailPassword(targetEmail : String, targetPassword : String) = {
    val obj = Database.user.findOne(MongoDBObject(email -> targetEmail))

    obj match {
      case Some(obj) =>
        var encrypted = obj.getAs[String](password).get
        var resalt = targetPassword.bcrypt(encrypted) 
        if (resalt == encrypted)
          toUser(obj)
        else None
      case None => None
    }
  }


  def findByEmail(targetEmail : String) = {
    val obj = Database.user.findOne(MongoDBObject(email -> targetEmail))

    obj match {
      case Some(obj) => Some(toUser(obj))
      case None => None
    }
  }

  def findByUsername(targetUsername : String) = {
    val obj = Database.user.findOne(MongoDBObject(username -> targetUsername))

    obj match {
      case Some(obj) => Some(toUser(obj))
      case None => None
    }
  }

  def updateByEmail(targetEmail : String, newEmail : Option[String],
    newPassword : Option[String], newUsername : Option[String]) = {

    val target = MongoDBObject(email -> targetEmail)

    newEmail match {
      case Some (s) =>
        Database.user.update(target, $set(email -> s))
      case None => ()
    }
    newPassword match {
      case Some (s) =>
        Database.user.update(target, $set(password -> s.bcrypt))
      case None => ()
    }
    newUsername match {
      case Some (s) =>
        Database.user.update(target, $set(username -> s))
      case None => ()
    }
  }

  def addFriend(targetEmail: String, friendUsername: String) = {
    val target = MongoDBObject(email -> targetEmail)
    val obj = Database.user.findOne(target)

    //check existence
    findByUsername(friendUsername) match {
      case Some(_) => ()
      case None => throw new UserNotFound(friendUsername)
    }

    obj match {
      case Some(obj) =>
        Database.user.update(target, 
          $push(friends -> friendUsername))
      case None => ()
    }

  }


  def opCapital(targetEmail: String, value: Double,
    op : (Double, Double) => Double){

    val target = MongoDBObject(email -> targetEmail)
    val obj = Database.user.findOne(target)

    obj match {
      case Some(obj) =>
        Database.user.update(target,
          $set(capital -> op (
            obj.getAs[Double](capital) match {
              case None => 0.0
              case Some (current) => current},
            value)))
      case None => ()
    }
  }


  def opQuoteByCompany(targetEmail: String,
    from: String,
    value: Int,
    op: (Int, Int) => Int) {

    val target = MongoDBObject(email -> targetEmail)
    val obj = Database.user.findOne(target)

    obj match {
      case Some(obj) =>
        Database.user.update(target,
          $set((quotes + "." + from) -> op (
            obj.getAs[DBObject](quotes) match {
              case Some (m) => m.getAs[Int](from) match {
                case Some (i) => i case None => 0}
              case None =>  0},
            value)))
      case None => ()
    }

  }


  def opTransaction(targetEmail: String, 
    action: OpAction,
    from: String,
    price: Double,
    number: Int) = {

    val target = MongoDBObject(email -> targetEmail)
    val obj = Database.user.findOne(target)

    val capitalVal = obj match {
      case Some(obj) => obj.getAs[Double](capital).get
      case None => 0.0
    }

    val transaction = TransactionObject(
      action, from, price, number, capitalVal, DateTime.now.toString)

    obj match {
      case Some(obj) =>
        Database.user.update(target, 
          $push(transactions -> createTransactionObject(transaction)))
      case None => ()
    }
  }

  def opIncScore(targetEmail: String, delta: Int) = {
    val target = MongoDBObject(email -> targetEmail)
    val obj = Database.user.findOne(target)

    obj match {
      case Some(obj) =>
        Database.user.update(target,
          $inc(score -> delta))
      case None => ()
    }
  }

  def opDecScore(targetEmail: String, delta: Int) = opIncScore(targetEmail, -delta)

  def addAchievement(targetEmail: String, achievement: Achievement) = {
    val target = MongoDBObject(email -> targetEmail)
    val obj = Database.user.findOne(target)

    obj match {
      case Some(obj) =>
        Database.user.update(target,
          $push(achievements -> achievement.id))
      case None => ()
    }
  }

  def getLastTransactionFrom(user: User, from: String, action: String) =
      user.transactions.reverse.find((tr: TransactionObject) => 
        tr.quote == from && Transaction.stringOfAction(tr.action) == action
      )


  def printAll = for (x <- Database.user.find ()) println (x)
  def stringAll = "[" + Database.user.find().mkString(",") + "]"
  def deleteAll = Database.user.remove(MongoDBObject())
  def updateAll = for (x <- Database.user.find ()) {
    try {
      x.getAs[Int](score).get
    } catch {
      case e => Database.user.update(x, $set(score -> 0))
    }
  }

}
