package controllers

import play.api._
import play.api.mvc._

import models.Finance
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {

  def index2 = Action {
    var current = models.Counter.getCurrent
    models.Counter.increment
    Ok(views.html.home(current));
  }

  def index = Action {
    var current = models.Counter.getCurrent
    models.Counter.increment
    Ok(views.html.dummy_home(current));
  }

  def reset = {
    models.Counter.reset
    Application.index
  }

  def currency(from: String, to: String) = Action.async { 
    val resp = models.Currency.request(from, to)
    resp.map { response =>
      val (id, rate) = models.Currency.parseResponse(response)
      Ok(views.html.finance(id, rate))
    }
  }

  def quote(name: String) = Action.async {
    val resp = models.Quote.request(name)
    resp.map { response =>
      Ok(views.html.quote(models.Quote.parseResponse(response)))
    }
  }

}
