package models

import play.api.libs.json.Json
import play.api.libs.ws.WS._
import play.api.libs.ws._

object Historic extends Finance {

  def request(name: String, from: String, to: String) = {
    val start = "\" and startDate = \"" ++ from
    val end = "\" and endDate = \"" ++ to ++ "\""
    val arg = "symbol = \"" ++ name ++ start ++ end
    processRequest("yahoo.finance.historicaldata", arg)
  }

  def parseResponse(response: Response) = {
    val res = response.json \ "query" \ "results" \ "quote"
    res
  }

}
