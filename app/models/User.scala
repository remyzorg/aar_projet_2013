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
      obj.getAsOrElse(username, "Toto")
    )

  def create (user : User, newPassword : String) = {
    val cryptedPassword = newPassword.bcrypt

    val obj =
      MongoDBObject (
        email -> user.email, 
        id -> user.username, 
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

  def findByEmail(targetEmail : String, targetPassword : String) = {
    var cryptedPassword = targetPassword.bcrypt

    val obj = 
      Database.user.findOne(
        MongoDBObject(email -> targetEmail, password -> cryptedPassword)
      )
   
    obj match {
      case Some(obj) => toUser(obj)
      case None => None
    }
  }
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
