package models.shop

import com.google.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import PgSQL.api._

import scala.concurrent.Future
import scala.util.{Success, Try}
import models.shop.Const._

/**
 * Created by snc on 2/28/17.
 */

@Singleton
class DAO @Inject() (dbConfigProvider: DatabaseConfigProvider) {

  private val db = dbConfigProvider.get.db

  import Tables._

  def getAllSizes: Future[Seq[Size]] = {
    db.run(sizes.result)
  }

  def gelAllGoodsItems = {
    db.run(goodsItems.filter(_.qnt > 0).sortBy(_.display).result)
  }

  def addToShoppingCart(email: String, goodsItemID: Int, qnt: Int, size: String): Future[Seq[OrderedInfo]] = {
    getOrCreateOpenOrderByEmail(email).flatMap { openOrder =>
      val selectItemFromPriceListQry = goodsItems.filter(_.id === goodsItemID).result.head
      db.run(selectItemFromPriceListQry).flatMap { orderedItem =>
        val addItemToCartQry = orderedItemsInsert += OrderedItem(-1, openOrder.id, orderedItem.id, qnt, size, qnt * orderedItem.price)
        db.run(addItemToCartQry).flatMap { _ =>
          getOpenOrderedItems(email).flatMap { allItems =>
            val orderTotal: BigDecimal = allItems.map(_.itemTotal).foldLeft(0: BigDecimal)((a, b) => a + b)
            val updateTotalQry = orders.filter(o => o.email === email && o.status === OPEN_ORDER).map(_.total).update(orderTotal)
            db.run(updateTotalQry).flatMap(_ => getOpenOrderedItems(email))
          }
        }
      }
    }
  }

  def delItemFromShoppingCart(email: String, orderedItemID: Int): Future[Seq[OrderedInfo]] = {
    getOpenOrderedItems(email).flatMap { items =>
      if (!items.map(_.orderedItemID).contains(orderedItemID))
        throw new Exception(s"Open order does'n contain item $orderedItemID")
      db.run(orderedItems.filter(_.id === orderedItemID).delete).flatMap { _ =>
        val deletedItem = items.filter(_.itemID == orderedItemID)
        Logger.info(s"Deleted from oreder_items: $deletedItem")
        getOpenOrderedItems(email)
      }
    }
  }

  def getOpenOrderedItems(email: String): Future[Seq[OrderedInfo]] = {
    val qry = orderedInfoView.filter(o => o.status === OPEN_ORDER && o.email === email).result
    db.run(qry)
  }

  private def getOrCreateOpenOrderByEmail(email: String): Future[Order] = {
    val qry = orders.filter(o => o.email === email && o.status === OPEN_ORDER).result
    db.run(qry).flatMap { foundOrders =>
      foundOrders.length match {
        case 0 =>
          val order: Future[Order] = db.run(ordersInsert += Order.empty(email))
          Logger.debug(s"Created empty $order for $email.")
          order
        case 1 =>
          Logger.debug(s"For $email found 1 open order.")
          Future(foundOrders.head)
        case x if x > 1 =>
          val errorMsg = s"Multiple open orders for customer $email. Must be only 1 !"
          Logger.error(errorMsg)
          throw new Exception(errorMsg)
      }
    }
  }

}
