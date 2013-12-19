package models

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS._
import play.api.libs.ws._
import scala.concurrent.Future


object Finance {

  val api = "http://query.yahooapis.com/v1/public/yql"
  val format = "format=json"

  def parseResponse(response: Response) = {
    val res = response.json \ "query" \ "results"\ "rate"
    ((res \ "id").as[String], (res \ "Rate").as[String])
  }

  def processRequest(table: String, args: String) = { 
    val query = ("q", "select * from " ++ table ++ " where " ++ args ++ "")
    val env = ("env", "store://datatables.org/alltableswithkeys")
    val holder = WS.url(api).withQueryString(query, ("format", "json"), env)
    val futureResponse : Future[Response] = holder.get()
    futureResponse
  }

  def requestCurrency(from: String, to: String) = {
    val arg = "pair in (\"" ++ from ++ to ++"\")"
    processRequest("yahoo.finance.xchange", arg)
  }

}
