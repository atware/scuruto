package model

import com.github.roundrop.scalikejdbcext.sqlsyntax._
import model.typebinder.TagId
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class Tag(
  tagId: TagId,
  name: String,
  taggingsCount: Option[Int] = None,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None,
  articles: Seq[Article] = Nil
)

object Tag extends SkinnyCRUDMapperWithId[TagId, Tag] with TimestampsFeatureWithId[TagId, Tag] {
  override lazy val tableName = "tags"
  override lazy val defaultAlias = createAlias("t")
  override lazy val primaryKeyFieldName = "tagId"

  hasManyThroughWithFk[Article](
    through = ArticlesTags,
    many = Article,
    throughFk = Tag.column.tagId,
    manyFk = ArticlesTags.column.articleId,
    merge = (t, a) => t.copy(articles = a)
  )

  val t = defaultAlias

  def findByName(name: String)(implicit s: DBSession = autoSession): Option[Tag] = {
    Tag.where(sqls.eqIgnoreCase(column.name, name)).apply().headOption
  }

  def findByNamePrefix(prefix: String)(implicit s: DBSession = autoSession): Seq[Tag] = {
    Tag.findAllBy(sqls.likeIgnoreCase(column.name, prefix + "%"))
  }

  // --------
  def idToRawValue(id: TagId) = id.value
  def rawValueToId(value: Any) = TagId(value.toString.toLong)
  override def extract(rs: WrappedResultSet, t: ResultName[Tag]): Tag = autoConstruct(rs, t, "articles")
}
