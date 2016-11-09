package model

import model.typebinder.{ ArticleId, TagId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class ArticlesTagsSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var aid2: ArticleId = _
  var tid3: TagId = _

  override def fixture(implicit session: DBSession) {
    val u = User.column
    val uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    val a = Article.column
    val aid1 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
    aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 0)
    val aid3 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title3", a.body -> "body3", a.stocksCount -> 0)
    val t = Tag.column
    val tid1 = Tag.createWithNamedValues(t.name -> "tag1")
    val tid2 = Tag.createWithNamedValues(t.name -> "tag2")
    tid3 = Tag.createWithNamedValues(t.name -> "tag3")
    val at = ArticlesTags.column
    ArticlesTags.createWithNamedValues(at.articleId -> aid1, at.tagId -> tid3)
    ArticlesTags.createWithNamedValues(at.articleId -> aid2, at.tagId -> tid2)
    ArticlesTags.createWithNamedValues(at.articleId -> aid2, at.tagId -> tid1)
    ArticlesTags.createWithNamedValues(at.articleId -> aid2, at.tagId -> tid3)
    ArticlesTags.createWithNamedValues(at.articleId -> aid3, at.tagId -> tid3)
  }

  describe("countByTagId") {
    it("should return count") { implicit session =>
      ArticlesTags.countByTagId(TagId(3)) should equal(3)
      ArticlesTags.countByTagId(TagId(2)) should equal(1)
      ArticlesTags.countByTagId(TagId(1)) should equal(1)
      ArticlesTags.countByTagId(TagId(4)) should equal(0)
    }
  }

  describe("deleteAllByArticleId") {
    it("should delete article's tags") { implicit session =>
      ArticlesTags.deleteAllByArticleId(aid2) should equal(3)
    }
  }

  describe("findAllByTagWithPaginationOrderByDesc") {
    it("should return first page") { implicit session =>
      val page = ArticlesTags.findAllByTagWithPaginationOrderByDesc(tid3, 1, 2)
      page should have size 2
    }
    it("should return second page") { implicit session =>
      val page = ArticlesTags.findAllByTagWithPaginationOrderByDesc(tid3, 2, 2)
      page should have size 1
    }
  }

}