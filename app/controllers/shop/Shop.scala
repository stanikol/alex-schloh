/**
 * Created by snc on 2/28/17.
 */
package controllers.shop

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.mohiva.play.silhouette.api.actions.{UserAwareAction, UserAwareRequest}
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import controllers.BaseController
import models.auth.AuthEnv
import models.shop.{DAO, GoodsItem, Order, Size}
import models.{RequestMessage, ResponseMessage}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.socket.SocketService
import utils.{Application, Logging}
import utils.metrics.Instrumented
import utils.web.MessageFrameFormatter
import models.shop.Const._
import play.api.data.FormError
import play.api.i18n.Messages.Message
import views.shop.FormsData

import scala.concurrent.Future

@javax.inject.Singleton
class Shop @javax.inject.Inject() (
    val db: DAO,
    val formsData: FormsData,
    //                                    val app: Application,
    val silhouette: Silhouette[AuthEnv],
    val messagesApi: MessagesApi
//                                    implicit val system: ActorSystem,
//                                    implicit val materializer: Materializer
) extends Controller with Instrumented with Logging with I18nSupport {
  import formsData._

  def index = silhouette.UserAwareAction.async { implicit request =>
    displayShop
  }

  def buy = silhouette.UserAwareAction.async { implicit request =>
    addToShopCart.bindFromRequest().fold(
      { withErrors =>
        val errorMsg = withErrors.errors.map {
          case FormError("id", m, a) => Messages("Please select an item to buy!")
          case FormError("size", m, a) => Messages("Size is not specified")
          case FormError("qnt", m, a) => Messages("Quantity is unspecified")
        }
        Future(Redirect(routes.Shop.index()).flashing("error" -> errorMsg.mkString("<br>")))
      },
      ok => db.addToShoppingCart(getUserOrTmpID(), ok.goodsItemID, ok.qnt, ok.size).map { _ => Redirect(routes.Shop.index()) }
    )
  }

  def showShopCart = silhouette.UserAwareAction.async { implicit request =>
    db.getOpenOrderedItems(getUserOrTmpID()).map { itemsOrdered =>
      Ok(s"${itemsOrdered.mkString("\n<br>")}")
    }
  }

  private def displayShop(implicit request: UserAwareRequest[AuthEnv, AnyContent]) = {
    val emailOrTmpID: String = getUserOrTmpID()
    db.getAllSizes.flatMap { sizes: Seq[Size] =>
      db.getOpenOrderedItems(emailOrTmpID).flatMap { itemsOrdered =>
        db.gelAllGoodsItems.map { goods: Seq[GoodsItem] =>
          Ok(views.html.shop.index(request.identity, goods, sizes, itemsOrdered)).withCookies(Cookie(USER_COOKIE, emailOrTmpID))
        }
      }
    }
  }

  private def getUserOrTmpID(cookie: String = USER_COOKIE)(implicit request: UserAwareRequest[AuthEnv, AnyContent]) = {
    request.identity match {
      case Some(user) => user.profile.providerKey
      case _ =>
        if (request.cookies.get(cookie).isEmpty) {
          val rundomUUID = { java.util.UUID.randomUUID().toString }
          val format = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm:ss_S")
          val now: String = format.print(DateTime.now)
          s"$rundomUUID@@$now"
        } else
          request.cookies.get(cookie).get.value
    }
  }

  //  private def userCookie = Cookie(USER_COOKIE, getUserOrTmpID())

}
