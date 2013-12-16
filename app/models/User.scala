

package models

// case class User 

case class User (id : Int, email : String, pseudo : String, password : String)

object UserModel {
  import com.mongodb.casbah.Imports._


  // def save (u) = {
  //   val obj =
  //     MongoDBObject (
  //       "id" -> Counter.next ("userid"),
  //       "email" -> u.email,
  //       "pseudo" -> u.pseudo,
  //       "password" -> u.password)
  //   Database.user.save (obj)
  // }


  // def findById (id) = Database.user.findOne(MongoDBObject("id" -> id))


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
