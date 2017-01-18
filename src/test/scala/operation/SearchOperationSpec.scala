package operation

import model.{ Article, Tag, TestDBSettings, User }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class SearchOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new SearchOperationImpl

  override def fixture(implicit session: DBSession): Unit = {
    val u = User.column
    val uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)

    val a = Article.column
    val t = Tag.column
    for (i <- 1 to 11) {
      val aid = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
      Tag.createWithNamedValues(t.name -> s"title$aid", t.taggingsCount -> aid)
    }
    Tag.createWithNamedValues(t.name -> "hoge", t.taggingsCount -> 32)
  }

  describe("search") {
    it("should return search result of first page") { implicit session =>
      val q = "tl"
      val result = operation.search(q, 1, 10)
      result.q should equal("tl")
      result.tag shouldBe None
      result.articles.currentPage should equal(1)
      result.articles.totalCount should equal(11)
      result.articles.totalPages should equal(2)
      result.articles.data should have size 10
    }
    it("should return search result of last page") { implicit session =>
      val q = "tl"
      val result = operation.search(q, 2, 10)
      result.q should equal("tl")
      result.tag shouldBe None
      result.articles.currentPage should equal(2)
      result.articles.totalCount should equal(11)
      result.articles.totalPages should equal(2)
      result.articles.data should have size 1
    }

    it("should return tag info when tag name exactly matched") { implicit session =>
      val q = "hoge"
      val result = operation.search(q, 1)
      result.tag shouldBe defined
      result.tag.get.name should equal("hoge")
      result.tag.get.taggingsCount should equal(Some(32))
    }
  }

}
