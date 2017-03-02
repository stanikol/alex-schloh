package views.shop

/**
 * Created by snc on 3/2/17.
 */
import com.google.inject.{Inject, Singleton}
import models.shop.DAO
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.Await
import scala.concurrent.duration._

@Singleton
class FormsData @Inject() (val db: DAO) {
  import scala.language.postfixOps
  private val sizes: Seq[String] = Await.result(db.getAllSizes, 5 seconds).map(_.size)

  case class AddToShopCart(goodsItemID: Int, qnt: Int, size: String)

  val addToShopCart = Form(mapping(
    "id" -> number,
    "qnt" -> number,
    "size" -> nonEmptyText
  )(AddToShopCart.apply)(AddToShopCart.unapply))

}
