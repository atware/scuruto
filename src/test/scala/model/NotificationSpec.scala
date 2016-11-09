package model

import model.typebinder.UserId
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

class NotificationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var uid1, uid2, uid3: UserId = _

  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "en", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true)

    val a = Article.column
    val aid1 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
    val aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 0)

    val n = Notification.column
    Notification.createWithNamedValues(n.userId -> uid1, n.articleId -> aid1, n.senderId -> uid2, n.`type` -> "comment")
    Notification.createWithNamedValues(n.userId -> uid1, n.articleId -> aid1, n.senderId -> uid2, n.`type` -> "comment")
    Notification.createWithNamedValues(n.userId -> uid1, n.articleId -> aid1, n.senderId -> uid2, n.`type` -> "comment")
    Notification.createWithNamedValues(n.userId -> uid2, n.articleId -> aid2, n.senderId -> uid1, n.`type` -> "comment")
    Notification.createWithNamedValues(n.userId -> uid1, n.articleId -> aid1, n.senderId -> uid3, n.`type` -> "stock")
    Notification.createWithNamedValues(n.userId -> uid1, n.articleId -> aid1, n.senderId -> uid2, n.`type` -> "stock")
  }

  describe("findRecentsByUserId") {
    it("should return notifications") { implicit session =>
      val notifications = Notification.findRecentsByUserId(uid1, 3)
      notifications should have size 3
      notifications.head.senderId should equal(uid2)
      notifications.head.`type` should equal("stock")
      notifications(1).senderId should equal(uid3)
      notifications(1).`type` should equal("stock")
      notifications(2).senderId should equal(uid2)
      notifications(2).`type` should equal("comment")
    }
    it("should return empty") { implicit session =>
      val notifications = Notification.findRecentsByUserId(uid3, 10)
      notifications should have size 0
    }
  }

  describe("updateStateAsRead") {
    it("should update a specific user's state as read") { implicit session =>
      Notification.updateStateAsRead(uid1)
      Notification.findAllBy(sqls.eq(Notification.column.userId, uid1)).foreach(
        n => n.state shouldBe true
      )
    }
  }

}