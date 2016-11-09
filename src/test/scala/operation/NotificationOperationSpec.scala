package operation

import model._
import model.typebinder.UserId
import org.joda.time.DateTime
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import view_model.Notice

class NotificationOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new NotificationOperationImpl

  var uid1, uid2: UserId = _
  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "name1", u.email -> "hoge1@moge.jp", u.locale -> "ja")
    uid2 = User.createWithNamedValues(u.name -> "name2", u.email -> "hoge2@moge.jp", u.locale -> "ja", u.imageUrl -> "http://example.com/a.png")

    val a = Article.column
    val n = Notification.column
    for (i <- 1 to 16) {
      val aid = Article.createWithNamedValues(a.userId -> uid1, a.title -> s"title$i", a.body -> s"body$i")
      Notification.createWithNamedValues(
        n.userId -> uid1,
        n.articleId -> aid,
        n.fragmentId -> (if (i % 2 == 0) Some(i) else None),
        n.senderId -> uid2,
        n.`type` -> "comment",
        n.state -> (i % 2 == 0),
        n.createdAt -> DateTime.parse(f"2014-09-$i%02dT15:04:05")
      )
    }
  }

  describe("getNotifications") {
    it("should return notifications") { implicit session =>
      val notificationArea = operation.getNotifications(User.findById(uid1).get)
      notificationArea.count should equal(7)
      notificationArea.data should have size 15
      val head: Notice = notificationArea.data.head
      head.image should equal(Some("http://example.com/a.png"))
      head.sender should equal("name2")
      head.isCommenter shouldBe false
      head.title should equal("title16")
      head.fragmentId should equal(Some(16))
      head.`type` should equal("comment")
      head.when should equal(Some(DateTime.parse("2014-09-16T15:04:05")))
      head.state shouldBe true
      val next: Notice = notificationArea.data(1)
      next.image should equal(Some("http://example.com/a.png"))
      next.sender should equal("name2")
      next.isCommenter shouldBe false
      next.title should equal("title15")
      next.fragmentId shouldBe None
      next.`type` should equal("comment")
      next.when should equal(Some(DateTime.parse("2014-09-15T15:04:05")))
      next.state shouldBe false
    }

    it("should return empty when no notification") { implicit session =>
      val notificationArea = operation.getNotifications(User.findById(uid2).get)
      notificationArea.count should equal(0)
      notificationArea.data should have size 0
    }
  }

  describe("updateStateAsRead") {
    it("updates notification state as read") { implicit session =>
      operation.updateStateAsRead(User.findById(uid1).get)
      Notification.findAll().foreach { n =>
        n.state shouldBe true
      }
    }
  }

}