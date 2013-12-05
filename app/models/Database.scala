package models

import com.mongodb.casbah.Imports._


object Database {
    val mongoClient = MongoClient("localhost", 27017)

    val db = mongoClient("yolo")

    val counter = db("counter")
}

// vim: set ts=4 sw=4 et:
