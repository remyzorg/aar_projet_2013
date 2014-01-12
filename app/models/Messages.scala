package models

import org.bson.types.ObjectId

case class Message(
  id : ObjectId,
  owner : ObjectId,
  read : Boolean,
  subject: String,
  body : String
)

object Message {
  import com.mongodb.casbah.Imports._

  val id = "_id"
  val owner = "owner"
  val read = "read"
  val subject = "subject"
  val body = "body"

  def toMessage(obj : MongoDBObject) = Message(
    obj.getAs[ObjectId](id).get,
    obj.getAs[ObjectId](owner).get,
    obj.getAsOrElse(read, false),
    obj.getAsOrElse(subject, ""),
    obj.getAsOrElse(body, "")
  )

  def getAllUnreadMessages(user : User) = {
    val objs = Database.message.find(MongoDBObject(owner -> user.id, read -> false))
    for(obj <- objs) yield toMessage(obj)
  }

  def addMessage(message : Message) = {
    val obj = MongoDBObject(
      owner -> message.owner,
      read -> message.read,
      subject -> message.subject,
      body -> message.body
    )
    Database.message.save(obj)
  }

  def setRead(message : Message) =
    Database.message.findAndModify(
      MongoDBObject(id -> message.id),
      $set(read -> true)
    )
}
