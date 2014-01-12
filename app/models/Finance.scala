package models

import scala.concurrent.Future
import play.api.libs.ws.WS._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.data.validation.ValidationError

import com.fasterxml.jackson.core.JsonParseException


/**
  * Abstract class that defines a Yahoo API request, for YQL requests
  */
abstract class Finance {

  val api = "http://query.yahooapis.com/v1/public/yql"
  val format = ("format", "json")
  val env = ("env", "store://datatables.org/alltableswithkeys")  

  /**
    * Takes a table and the "where" clause arguments. Returns a
    * Future[Response], which will be processed by an Async Action
    */
  def processRequest(table: String, args: String) = { 
    val query = ("q", "select * from " ++ table ++ " where " ++ args ++ "")
    val holder = WS.url(api).withQueryString(query, format, env)
    println(api + "?q="+ query._2 + "&env=" + env._2 + "&format=json")

    val futureResponse : Future[Response] = holder.get()
    futureResponse
  }
  
  /**
    * Parses the body of a response, returning the "results" value of the JSON
    * response
    */
  def parseBodyResponse(response: Response) = {
    val res = try {response.json \ "query" \ "results"}
    catch {
      case e: JsonParseException => {println(e + "\n" + response); throw e}
    }
    res
  }

}
