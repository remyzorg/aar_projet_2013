package models

import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import scala.language.reflectiveCalls



case class User (
  id : ObjectId,
  email : String,
  username : String,
  capital : Double,
  quotes : Map[String, Int],
  transactions : List[TransactionObject]
)

case class TransactionObject (
  id : ObjectId,
  action : String,
  quote : String,
  price : Double,
  number : Int,
  capital : Double
)

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
  
  val action = "action"
  val quote = "quote"
  val price = "price"
  val number = "number"

  def toTransaction(obj: DBObject) = 
    TransactionObject (
      obj.getAs[ObjectId](id).get,
      obj.getAs[String](action).get,
      obj.getAs[String](quote).get,
      obj.getAs[Double](price).get,
      obj.getAs[Int](number).get,
      obj.getAs[Double](capital).get
    )

  def createTransactionObject(tr : TransactionObject) =
    MongoDBObject (
      action -> tr.action,
      quote -> tr.quote,
      price -> tr.price,
      number -> tr.number,
      capital -> tr.capital
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
        case Some (m : DBObject) => { val m2 : MongoDBList = m;
          m2.toList.asInstanceOf[List[DBObject]]
            .map { obj => toTransaction(obj) }
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
        transactions -> createTransactionList(user.transactions)
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
    // transaction: TransactionObject,
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


  def printAll = for (x <- Database.user.find ()) println (x)
  def stringAll = Database.user.find().mkString(" ")
  def deleteAll = Database.user.remove(MongoDBObject())


}
