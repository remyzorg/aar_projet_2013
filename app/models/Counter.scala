package models

case class Counter(value: Int)

object Counter {
    import com.mongodb.casbah.Imports._

    val zeroCounter = MongoDBObject("id" -> 0, "current" -> 0)
    val idQuery = MongoDBObject("id" -> 0)

    def set = {
        Database.counter.save(zeroCounter)
        Some(zeroCounter)
    }

    def reset = 
        Database.counter
            .findAndModify(idQuery, zeroCounter)
            .orElse(Counter.set)

    def getCurrent = {
        val counterObject = Database.counter.findOne(idQuery)
        val value = 
            counterObject
                .orElse(Counter.set).get
                .getAsOrElse("current", 0)
        Counter(value)
    }

    def increment = Database.counter.update(idQuery, $inc("current" -> 1))
}

// vim: set ts=4 sw=4 et:
