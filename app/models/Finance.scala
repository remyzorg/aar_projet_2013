package models

import scala.concurrent.Future
import play.api.libs.ws.WS._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.data.validation.ValidationError

abstract class Finance {

  val api = "http://query.yahooapis.com/v1/public/yql"
  val format = ("format", "json")
  val env = ("env", "store://datatables.org/alltableswithkeys")  

  def processRequest(table: String, args: String) = { 
    val query = ("q", "select * from " ++ table ++ " where " ++ args ++ "")
    val holder = WS.url(api).withQueryString(query, format, env)
    val futureResponse : Future[Response] = holder.get()
    futureResponse
  }
  
  def parseBodyResponse(response: Response) = {
    val res = response.json \ "query" \ "results"
    res // Json.prettyPrint(res)
  }

  // def requestQuote(name: String) = {
  //   val arg = "symbol in (\"" ++ name ++ "\")"
  //   processRequest("yahoo.finance.quotes", arg)
  // }

  // def parseQuoteResponse(response: Response) = {
  //   val res = response.json \ "query" \ "results" \ "quote"
  //   Json.prettyPrint(res)
  // }

  // def requestCurrency(from: String, to: String) = {
  //   val arg = "pair in (\"" ++ from ++ to ++"\")"
  //   processRequest("yahoo.finance.xchange", arg)
  // }

  // def parseCurrencyResponse(response: Response) = {
  //   val res = response.json \ "query" \ "results"\ "rate"
  //   ((res \ "id").as[String], (res \ "Rate").as[String])
  // }

}
