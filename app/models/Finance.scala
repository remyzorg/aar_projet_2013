package models

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS._
import play.api.libs.ws._
import scala.concurrent.Future


object Finance {

  val api = "http://query.yahooapis.com/v1/public/yql"
  val format = "format=json"

  def parseResponse(response: Response) =
    (response.json // \ "results"
    ).toString()// .as[String]

  def processRequest(table: String, args: String) = { 
    val query = "select * from " ++ table ++ " where " ++ args ++ ""
    println(query);
    // val url = api ++ query ++ "&" ++ format
    // val urlE = "http://" ++ java.net.URLEncoder.encode(url, "UTF-8")
    val holder = WS.url(api).withQueryString(("q", query), ("format", "json"))
    val futureResponse : Future[Response] = holder.get()
    futureResponse
  }

  def requestCurrency(from: String, to: String) = {
    val arg = "pair in (\"" ++ from ++ to ++"\")"
    processRequest("yahoo.finance.xchange", arg)
  }

}
