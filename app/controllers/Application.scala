package controllers

import play.api._
import play.api.mvc._

import models.Finance
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.home())
  }

  // def index = Action {

    // var current = models.Counter.getCurrent
    // models.Counter.increment
    // Ok(views.html.home(current));

  //   var current = models.Counter.getCurrent
  //   models.Counter.increment
  //   Ok(views.html.dummy_home(current));
  // }

  // def reset = {
  //   models.Counter.reset
  //   Application.index
  // }

}
