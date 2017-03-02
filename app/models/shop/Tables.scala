package models.shop

import org.joda.time.DateTime
import PgSQL.api._

/**
 * Created by snc on 2/28/17.
 */
object Tables {

  class SizesTbl(tag: Tag) extends Table[Size](tag, "sizes") {
    def size = column[String]("size", O.PrimaryKey)
    def * = (size) <> (Size.apply, Size.unapply)
  }
  val sizes = TableQuery[SizesTbl]

  class GoodsItemsTbl(tag: Tag) extends Table[GoodsItem](tag, "goods_items") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def price = column[BigDecimal]("price")
    def qnt = column[Int]("qnt")
    def display = column[Int]("display")
    def images = column[List[String]]("images")
    def description = column[String]("description")
    def * = (id, name, price, qnt, display, images, description) <> (GoodsItem.tupled, GoodsItem.unapply)
  }
  val goodsItems = TableQuery[GoodsItemsTbl]

  class StatusTbl(tag: Tag) extends Table[String](tag, "status") {
    def staus = column[String]("status", O.PrimaryKey)
    def * = staus
  }

  val statuses = TableQuery[StatusTbl]

  class OrdersTbl(tag: Tag) extends Table[Order](tag, "orders") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def orderDate = column[DateTime]("order_date")
    def address = column[String]("address")
    def total = column[BigDecimal]("total")
    def status = column[String]("status")
    def comments = column[String]("comments")
    def email = column[String]("email")
    def * = (id, orderDate, address, total, status, comments, email) <> ((Order.apply _).tupled, Order.unapply)
    def statusFK = foreignKey("statusFK", status, statuses)(
      _.staus,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade
    )
  }

  val orders = TableQuery[OrdersTbl]
  val ordersInsert = orders.returning(orders.map(_.id)).into((order, id) => order.copy(id = id))

  class OrderedItemsTbl(tag: Tag) extends Table[OrderedItem](tag, "ordered_items") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def orderID = column[Int]("order_id")
    def goodsItemID = column[Int]("goods_item_id")
    def qnt = column[Int]("qnt")
    def size = column[String]("size")
    def total = column[BigDecimal]("total")
    def * = (id, orderID, goodsItemID, qnt, size, total) <> (OrderedItem.tupled, OrderedItem.unapply)
    def orderedIDFK = foreignKey("orderedIDFK", orderID, orders)(
      _.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade
    )
    def goodsItemIDFK = foreignKey("goodsItemIDFK", goodsItemID, goodsItems)(
      _.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade
    )
  }

  val orderedItems = TableQuery[OrderedItemsTbl]
  val orderedItemsInsert = orderedItems.returning(orderedItems.map(_.id)).into((item, id) => item.copy(id = id))

  class OrderedInfoView(tag: Tag) extends Table[OrderedInfo](tag, "ordered_full_info") {
    def name = column[String]("name")
    def price = column[BigDecimal]("price")
    def itemID = column[Int]("item_id")

    def orderedItemID = column[Int]("ordered_item_id")
    def qnt = column[Int]("qnt")
    def size = column[String]("size")
    def itemTotal = column[BigDecimal]("item_total")

    def orderID = column[Int]("order_id")
    def address = column[String]("address")
    def total = column[BigDecimal]("total")
    def email = column[String]("email")
    def comments = column[String]("comments")
    def status = column[String]("status")
    def orderDate = column[DateTime]("order_date")

    def * = (name, price, itemID, orderedItemID, qnt, size, itemTotal, orderID, address,
      total, email, comments, status, orderDate) <> (OrderedInfo.tupled, OrderedInfo.unapply)
    //      total, email, comments, status /*, orderDate*/ ) <> (OrderedInfo.tupled, OrderedInfo.unapply)
  }
  val orderedInfoView = TableQuery[OrderedInfoView]

}
