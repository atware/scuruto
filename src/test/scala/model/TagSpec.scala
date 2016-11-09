package model

import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class TagSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  override def fixture(implicit session: DBSession): Unit = {
    val t = Tag.column
    Tag.createWithNamedValues(t.name -> "tag1", t.taggingsCount -> 2)
    Tag.createWithNamedValues(t.name -> "tag2", t.taggingsCount -> 6)
    Tag.createWithNamedValues(t.name -> "tag3", t.taggingsCount -> 10)
    Tag.createWithNamedValues(t.name -> "test", t.taggingsCount -> 4)
  }

  describe("findByName") {
    it("should return a specific named tag") { implicit session =>
      val tag = Tag.findByName("tag1")
      tag shouldBe defined
      tag.get.name should equal("tag1")
    }
    it("should return empty") { implicit session =>
      val tag = Tag.findByName("foo")
      tag shouldBe None
    }
  }

  describe("findByNamePrefix") {
    it("should return tags having prefix") { implicit session =>
      val tags = Tag.findByNamePrefix("tag")
      tags should have size 3
      tags.head.name should equal("tag1")
      tags.last.name should equal("tag3")
    }
    it("should return empty list") { implicit session =>
      val tags = Tag.findByNamePrefix("foo")
      tags should have size 0
    }
  }

}