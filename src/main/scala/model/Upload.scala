package model

import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class Upload(
  uploadId: Long,
  userId: UserId,
  originalFilename: String,
  filename: String,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None
)

object Upload extends SkinnyCRUDMapper[Upload] with TimestampsFeature[Upload] {
  override lazy val tableName = "uploads"
  override lazy val defaultAlias = createAlias("u")
  override lazy val primaryKeyFieldName = "uploadId"

  override def extract(rs: WrappedResultSet, u: ResultName[Upload]): Upload = autoConstruct(rs, u)
}
