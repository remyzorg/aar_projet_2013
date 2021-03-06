package models

import play.api.libs.json._
import play.api.libs.ws.WS._
import play.api.libs.ws._

import java.util.Date

/**
  * Asks for the last 14 days history of a Stock quote
  */ 
object Historic extends Finance {

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

  def request(name: String) = {
    val date = new java.util.GregorianCalendar()
    // date.add(java.util.Calendar.DATE, -1)
    val to = dateFormat.format(date.getTime())
    date.add(java.util.Calendar.DATE, -15)
    val from = dateFormat.format(date.getTime())
    val start = "\" and startDate = \"" ++ from
    val end = "\" and endDate = \"" ++ to ++ "\""
    val arg = "symbol = \"" ++ name ++ start ++ end
    processRequest("yahoo.finance.historicaldata", arg)
  }

  //Returns a (string * double) list containing the date and the corresponding value
  def parseResponse(response: Response) = {
    val historyJson = parseBodyResponse(response) \ "quote"
    val sequence = historyJson.as[JsArray].value
    val res = sequence.foldLeft(List.empty[(String, Double)])(
      (l, json) => 
      try {
        (((json \ "Date").as[String]),((json \ "High").as[String].toDouble)) :: l
      } catch {
        case e: JsResultException => l
      })

    res
  }

}
