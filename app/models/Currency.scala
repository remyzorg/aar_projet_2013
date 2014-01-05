package models


import play.api.libs.json.Json
import play.api.libs.ws.WS._
import play.api.libs.ws._


object Currency extends Finance {

  def request(from: String, to: String) = {
    val arg = "pair in (\"" ++ from ++ to ++"\")"
    processRequest("yahoo.finance.xchange", arg)
  }

  def parseResponse(response: Response) = {
    val res = response.json \ "query" \ "results"\ "rate"
    // ((res \ "id").as[String], (res \ "Rate").as[String])
    (res \ "id", res \ "Rate")
  }

}
