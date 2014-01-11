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
    println(Json.prettyPrint(res))
    res // Json.prettyPrint(res)
  }

  def getMultipleValues(json: JsValue) : List[QuoteInfo] = {
    val sequence = json.as[JsArray].value
    def fun(acc: List[QuoteInfo], json: JsValue) = {
      try {
        getQuoteInfo(json) :: acc
      } catch {
        case e: JsResultException => acc
      }
    }

    val res = sequence.foldLeft(List.empty[QuoteInfo])(fun)
    res
  }

  def getValue(value : JsValue, name : String) : JsValue = value \ name

  def getQuoteInfo(value : JsValue) : QuoteInfo = QuoteInfo (
    getValue(value, "Name").as[String],
    getValue(value, "symbol").as[String],
    getValue(value, "Ask").as[String].toDouble,
    getValue(value, "Bid").as[String].toDouble,
    getValue(value, "AskRealtime").as[String].toDouble,
    getValue(value, "BidRealtime").as[String].toDouble,
    getValue(value, "Change_PercentChange").as[String],
    getValue(value, "ChangeRealtime").as[String].toDouble,
    getValue(value, "DaysLow").as[String].toDouble,
    getValue(value, "DaysHigh").as[String].toDouble,
    getValue(value, "Volume").as[String].toInt
  )
}
