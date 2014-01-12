package models

import play.api.libs.json._
import play.api.libs.ws.WS._
import play.api.libs.ws._

/** 
  * Symbolizes a quote received
  */
case class QuoteInfo (
  longName: String,
  name : String,
  ask : Option[Double],
  bid : Option[Double],
  askRealtime : Option[Double],
  bidRealtime : Option[Double],
  lastTradePrice : Option[Double],
  changePercentage : String,
  change: Double,
  daysLow : Double,
  daysHigh : Double,
  volume : Int
)


object Quote extends Finance {

  /**
    * Request a unique quote value
    */ 
  def request(name: String) = {
    val arg = "symbol in (\"" ++ name ++ "\")"
    processRequest("yahoo.finance.quotes", arg)
  }

  /** 
    * Request for multiple quotes
    */ 
  def request(names: List[String]) = {
    val arg = "symbol in (\"" ++ names.mkString(",") ++ "\")"
    processRequest("yahoo.finance.quotes", arg)
  }

  def parseResponse(response: Response) = {
    val res = parseBodyResponse(response) \ "quote"
    res
  }

  /** 
    * Takes as JSON and returns all the quotes it contains
    */
  def getMultipleValues(json: JsValue) : List[QuoteInfo] = {
    val sequence = json.as[JsArray].value
    def fun(acc: List[QuoteInfo], json: JsValue) = {
      try {
        getQuoteInfo(json) :: acc
      } catch {
        case e: JsResultException => acc
      }
    }
    sequence.foldLeft(List.empty[QuoteInfo])(fun)
  }

  /**
    * Returns the value of the field "name"
    */
  def getValue(value : JsValue, name : String) : JsValue = value \ name

  /**
    * Creates a QuoteInfo from a JSON value
    */
  def getQuoteInfo(value : JsValue) : QuoteInfo = QuoteInfo (
    getValue(value, "Name").as[String],
    getValue(value, "symbol").as[String],
    try { Some(getValue(value, "Ask").as[String].toDouble) } 
    catch {
      case e: JsResultException => None
    },
    try { Some(getValue(value, "Bid").as[String].toDouble) }
    catch {
      case e: JsResultException => None
    },
    try { val ask = getValue(value, "AskRealtime").as[String].toDouble
      if (ask == 0.0) None else Some(ask)
    }
    catch {
      case e: JsResultException => None
    },
    try { val bid = getValue(value, "BidRealtime").as[String].toDouble
      if (bid == 0.0) None else Some(bid)
    }
    catch {
      case e: JsResultException => None
    },
    try { val bid = getValue(value, "LastTradePriceOnly").as[String].toDouble
      if (bid == 0.0) None else Some(bid)
    }
    catch {
      case e: JsResultException => None
    },
    getValue(value, "Change_PercentChange").as[String],
    getValue(value, "ChangeRealtime").as[String].toDouble,
    getValue(value, "DaysLow").as[String].toDouble,
    getValue(value, "DaysHigh").as[String].toDouble,
    getValue(value, "Volume").as[String].toInt
  )
}
