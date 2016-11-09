package operation

import model.{ Tag, TestDBSettings }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class TagOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new TagOperationImpl

  override def fixture(implicit session: DBSession): Unit = {
    val t = Tag.column
    Tag.createWithNamedValues(t.name -> "tag1", t.taggingsCount -> 2)
    Tag.createWithNamedValues(t.name -> "tag2", t.taggingsCount -> 6)
    Tag.createWithNamedValues(t.name -> "tag3", t.taggingsCount -> 10)
    Tag.createWithNamedValues(t.name -> "test", t.taggingsCount -> 4)
  }

  describe("get") {
    it("should return a tag that has exactly matched name") { implicit session =>
      val tag = operation.get("tag2")
      tag shouldBe defined
      tag.get.name should equal("tag2")
    }
  }

}