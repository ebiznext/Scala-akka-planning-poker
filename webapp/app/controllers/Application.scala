package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.data.validation.Validation
import play.data.validation.Constraints
import play.api.mvc.QueryStringBindable
import play.Configuration
import play.api.Play

object Application extends Controller {

  // Just display the ebiz planning poker logo
  def index = Action { implicit request =>
    Ok(views.html.index("Planning Poker"))
  }
}

