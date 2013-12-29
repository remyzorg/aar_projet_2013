package models

import play.api.libs.json.Json
import play.api.libs.ws.WS._
import play.api.libs.ws._

object Quote extends Finance {

  def request(name: String) = {
    val arg = "symbol in (\"" ++ name ++ "\")"
    processRequest("yahoo.finance.quote", arg)
  }

  def request(names: List[String]) = {
    val arg = "symbol in (\"" ++ names.mkString(",") ++ "\")"
    processRequest("yahoo.finance.quote", arg)
  }

  def parseResponse(response: Response) = {
    val res = response.json \ "query" \ "results" \ "quote"
    Json.prettyPrint(res)
  }

}
