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
    val res = parseBodyResponse(response) \ "quote"
    res // Json.prettyPrint(res)
  }

  def getMultipleValues(json: JsValue) : List[(String, Double, Double)] = {
    val sequence = json.as[JsArray].value
    def fun(acc: List[(String, Double, Double)], json: JsValue) = {
      try {
        val ask = getAskPrice(json)
        val bid = getBidPrice(json)
        if (ask.isInstanceOf[JsValue]) {
          ((json \ "symbol").as[String], // 0.0, 0.0)
            ask.as[String].toDouble,
            bid.as[String].toDouble) :: acc
        } else { acc }
      } catch {
        case e: JsResultException => acc
      }
    }
    
    val res = sequence.foldLeft(List.empty[(String, Double, Double)])(fun)
    res
  }

  def getAskPrice(value: JsValue) = value \ "Ask" 

  def getBidPrice(value: JsValue) = value \ "Bid"

}
