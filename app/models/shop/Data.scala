package models.shop

import org.joda.time.{DateTime, DateTimeZone}
import models.shop.Const.OPEN_ORDER

/**
 * Created by snc on 2/28/17.
 */
case class Size(size: String)

case class GoodsItem(id: Int, name: String, price: BigDecimal, qnt: Int, display: Int, images: List[String], description: String)

case class Order(id: Int, orderDate: DateTime, address: String, total: BigDecimal, status: String, comments: String, email: String)

object Order {
  //  def apply(id: Int, ordered: DateTime, address: String, total: BigDecimal, status: String, comments: String, email: String): Order =
  //    new Order(id, ordered, address, total, status, comments, email)

  def empty(email: String): Order = new Order(-1, DateTime.now(DateTimeZone.UTC), "", 0, OPEN_ORDER, "", email)
}

case class OrderedItem(id: Int, orderID: Int, goodsItemID: Int, qnt: Int, size: String, total: BigDecimal)

case class OrderedInfo(name: String, price: BigDecimal, itemID: Int, orderedItemID: Int,
  qnt: Int, size: String, itemTotal: BigDecimal,
  orderID: Int, address: String, total: BigDecimal, email: String, comments: String, status: String, orderDate: DateTime)

//object Data {
//
//}
