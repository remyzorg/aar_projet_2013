package controllers

import play.api._
import play.api.mvc._

import models._

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
}
