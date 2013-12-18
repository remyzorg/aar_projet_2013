package models

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS._
import play.api.libs.ws._
import scala.concurrent.Future

class Finance {

  val api = "http://query.yahooapis.com/v1/public/yql?q="
  val format = "format=json"

  def parseResponse(response: Response) =
    (response.json \ "results").as[String]

  def processRequest(table: String, args: String) = { 
    val query = "select * from " ++ table ++ " where " ++ args
    val url = api ++ query ++ "&" ++ format
    val holder : WSRequestHolder = WS.url(url)
    val futureResponse : Future[Response] = holder.get()
    futureResponse
  }

  def requestCurrency(from: String, to: String) = {
    val arg = "pair in (" ++ from ++ to ++")"
    processRequest("yahoo.finance.xchange", arg)
  }

}
