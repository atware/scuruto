package model

import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class UpdateHistory(
  updateHistoryId: Long,
  articleId: ArticleId,
  newTitle: String,
  newTags: Option[String] = None,
  newBody: String,
  oldTitle: String,
  oldTags: Option[String] = None,
  oldBody: String,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None
)

object UpdateHistory extends SkinnyCRUDMapper[UpdateHistory] with TimestampsFeature[UpdateHistory] {
  override lazy val tableName = "update_histories"
  override lazy val defaultAlias = createAlias("uh")
  override lazy val primaryKeyFieldName = "updateHistoryId"

  override def extract(rs: WrappedResultSet, uh: ResultName[UpdateHistory]): UpdateHistory = autoConstruct(rs, uh)
}
