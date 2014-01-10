
package controllers


import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.bson.types.ObjectId
import models._


object AccountFinance extends Controller with Secured {


  def setup = withUser { user => implicit request =>

    Ok(views.html.account_finance(user.capital, user.quotes))

  }


}

