package models

import org.bson.types.ObjectId

case class User (
  id : ObjectId,
  email : String,
  username : String,
  capital : Double)

object UserModel {
  import com.github.t3hnar.bcrypt._
  import com.mongodb.casbah.Imports._

  val email = "email"
  val id = "_id"
  val username = "username"
  val password = "password"
  val capital = "capital"

  def toUser(obj : DBObject) = 
    User(
      obj.getAs[ObjectId](id).get,
      obj.getAs[String](email).get,
      obj.getAsOrElse(username, ""),
      obj.getAs[Double](capital) match {
        case Some (d) => d
        case None => 0.0
      }

    )

  def create (user : User, newPassword : String) = {
    val cryptedPassword = newPassword.bcrypt

    val obj =
      MongoDBObject (
        email -> user.email, 
        username -> user.username, 
        password -> cryptedPassword,
        capital -> user.capital
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
    val update = MongoDBObject.newBuilder

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
    val update = MongoDBObject.newBuilder

    val obj = Database.user.findOne(target)

    obj match {
      case Some(obj) =>
        Database.user.update(target,
          $set(capital -> op (obj.as[Double](capital), value)))
      case None => ()
    }
  }


  def opQuoteByCompany(targetEmail: String,
    from: String,
    quote: Int,
    op: (Int, Int) => Int) {

  }


  def printAll = for (x <- Database.user.find ()) println (x)
  def stringAll = Database.user.find().mkString(" ")
  def deleteAll = Database.user.remove(MongoDBObject())





}
