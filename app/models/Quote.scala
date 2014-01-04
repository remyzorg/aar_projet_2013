package models

import play.api.libs.json._
import play.api.libs.ws.WS._
import play.api.libs.ws._

object Quote extends Finance {

  def request(name: String) = {
    val arg = "symbol in (\"" ++ name ++ "\")"
    processRequest("yahoo.finance.quotes", arg)
  }

  def request(names: List[String]) = {
    val arg = "symbol in (\"" ++ names.mkString(",") ++ "\")"
    processRequest("yahoo.finance.quotes", arg)
  }

  def parseResponse(response: Response) = {
    val res = response.json \ "query" \ "results" \ "quote"
    res // Json.prettyPrint(res)
  }

  def getAskPrice(value: JsValue) = value \ "Ask" 

  def getBidPrice(value: JsValue) = value \ "Bid"

}
