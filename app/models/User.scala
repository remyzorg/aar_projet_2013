package models

import org.bson.types.ObjectId

case class User (id : ObjectId, email : String, username : String)

object UserModel {
  import com.github.t3hnar.bcrypt._
  import com.mongodb.casbah.Imports._

  val email = "email"
  val id = "_id"
  val username = "username"
  val password = "password"

  def toUser(obj : DBObject) = 
    User(
      obj.getAs[ObjectId](id).get,
      obj.getAs[String](email).get,
      obj.getAsOrElse(username, "")
    )

  def create (user : User, newPassword : String) = {
    val cryptedPassword = newPassword.bcrypt

    val obj =
      MongoDBObject (
        email -> user.email, 
        username -> user.username, 
        password -> cryptedPassword
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

    val obj =
      Database.user.findOne(
        MongoDBObject(email -> targetEmail)
      )

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
    val obj =
      Database.user.findOne(
        MongoDBObject(email -> targetEmail)
      )
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


  def printAll = for (x <- Database.user.find ()) println (x)
  def stringAll = Database.user.find().mkString(" ")

  def deleteAll = Database.user.remove(MongoDBObject())


}

// package models

// case class Counter(value: Int)

// object Counter {
//     import com.mongodb.casbah.Imports._

//     val zeroCounter = MongoDBObject("id" -> 0, "current" -> 0)
//     val idQuery = MongoDBObject("id" -> 0)

//     def set = {
//         Database.counter.save(zeroCounter)
//         Some(zeroCounter)
//     }

//     def reset = 
//         Database.counter
//             .findAndModify(idQuery, zeroCounter)
//             .orElse(Counter.set)

//     def getCurrent = {
//         val counterObject = Database.counter.findOne(idQuery)
//         val value = 
//             counterObject
//                 .orElse(Counter.set).get
//                 .getAsOrElse("current", 0)
//         Counter(value)
//     }

//     def increment = Database.counter.update(idQuery, $inc("current" -> 1))
// }

// vim: set ts=4 sw=4 et:
