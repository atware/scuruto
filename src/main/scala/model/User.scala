package model

import lib.ldap.LDAPUser
import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.micro.Params
import skinny.oauth2.client.google.GoogleUser
import skinny.orm._
import skinny.orm.feature._

case class User(
  userId: UserId,
  name: String,
  email: String,
  password: Option[String] = None,
  imageUrl: Option[String] = None,
  comment: Option[String] = None,
  locale: String,
  confirmationToken: Option[String] = None,
  confirmationSentAt: Option[DateTime] = None,
  resetPasswordToken: Option[String] = None,
  resetPasswordSentAt: Option[DateTime] = None,
  isActive: Boolean,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None,
  articles: Seq[Article] = Nil
)

object User extends SkinnyCRUDMapperWithId[UserId, User] with TimestampsFeatureWithId[UserId, User] {
  override lazy val tableName = "users"
  override lazy val defaultAlias = createAlias("u")
  override lazy val primaryKeyFieldName = "userId"

  val articlesRef = hasMany[Article](
    many = Article -> Article.defaultAlias,
    on = (a, c) => sqls.eq(a.userId, c.userId),
    merge = (user, articles) => user.copy(articles = articles)
  )

  def fromParams(params: Params, user: User): User = {
    val comment = params.get("comment")
    val locale = params.getOrElse("locale", user.locale)
    user.copy(comment = comment, locale = locale)
  }

  def findByEmail(email: String)(implicit s: DBSession = autoSession): Option[User] = {
    User.findBy(sqls.eq(User.column.email, email))
  }

  def findActivatedByEmail(email: String)(implicit s: DBSession = autoSession): Option[User] = {
    User.findBy(sqls.eq(User.column.email, email).and.eq(User.column.isActive, true))
  }

  def calcContribution(userId: UserId)(implicit s: DBSession = autoSession): Long = {
    val uid = userId.value
    sql"""
       |SELECT
       |  (sc.count*3)+lc.count AS contribution
       |FROM
       |  (select COUNT(*) AS count
       |   from stocks s
       |   left join articles a on s.article_id = a.article_id
       |   where a.user_id = ${uid} and s.user_id <> ${uid}) AS sc
       |  ,
       |  (select COUNT(*) AS count
       |   from likes l
       |   left join articles a on l.article_id = a.article_id
       |   where a.user_id = ${uid} and l.user_id <> ${uid}) AS lc
    """.stripMargin
      .map(_.long("contribution")).single().apply().getOrElse(0)
  }

  def calcContributionRanking(limit: Int)(implicit s: DBSession = autoSession): Seq[Map[String, Any]] = {
    sql"""
       |SELECT
       |  u.image_url AS image, u.name AS name, u.user_id AS uid,
       |  (COALESCE(sc.count,0)*3)+COALESCE(lc.count,0) AS contribution
       |FROM
       |  users u
       |  left join
       |  (select a.user_id, COUNT(*) AS count
       |   from stocks s
       |   inner join articles a on s.article_id = a.article_id
       |   inner join users u on u.user_id = a.user_id
       |   where a.user_id <> s.user_id
       |   group by a.user_id) AS sc ON sc.user_id = u.user_id
       |  left join
       |  (select a.user_id, COUNT(*) AS count
       |   from likes l
       |   inner join articles a on l.article_id = a.article_id
       |   inner join users u on u.user_id = a.user_id
       |   where a.user_id <> l.user_id
       |   group by a.user_id) AS lc ON lc.user_id = u.user_id
       |WHERE u.is_active = true
       |  AND (COALESCE(sc.count,0)*3)+COALESCE(lc.count,0) > 0
       |ORDER BY contribution DESC
       |LIMIT
       |  $limit
    """.stripMargin
      .map(_.toMap()).list().apply()
  }

  def calcReactions(userId: UserId)(implicit s: DBSession = autoSession): (Int, Int) = {
    sql"""
       |SELECT
       |  COALESCE(SUM(a.stocks_count),0) AS stocks,
       |  COALESCE(SUM(a.likes_count),0) AS likes
       |FROM
       |  articles a
       |WHERE
       |  a.user_id = ${userId.value}
    """.stripMargin
      .map { rs =>
        (rs.int("stocks"), rs.int("likes"))
      }
      .single().apply.getOrElse((0, 0))
  }

  /* for App login feature */

  def findByConfirmationToken(token: String)(implicit s: DBSession = autoSession): Option[User] = {
    User.findBy(sqls.eq(User.column.confirmationToken, token).and.eq(User.column.isActive, false))
  }

  def findByResetPasswordToken(token: String)(implicit s: DBSession = autoSession): Option[User] = {
    User.findBy(sqls.eq(User.column.resetPasswordToken, token))
  }

  /* for Google login feature */

  def create(gUser: GoogleUser)(implicit s: DBSession = autoSession): UserId = {
    User.createWithAttributes(
      'name -> makeName(gUser),
      'email -> gUser.emails.head.value,
      'image_url -> gUser.image.map(_.url.replace("?sz=50", "")),
      'locale -> "ja"
    )
  }

  private def makeName(gUser: GoogleUser): String = {
    val name = gUser.name.familyName + ' ' + gUser.name.givenName
    if (name.trim.isEmpty) gUser.emails.head.value.split("@")(0) else name
  }

  // --------
  def idToRawValue(id: UserId) = id.value
  def rawValueToId(value: Any) = UserId(value.toString.toLong)
  override def extract(rs: WrappedResultSet, u: ResultName[User]): User = autoConstruct(rs, u, "articles")

  /* for LDAP login feature */

  def createFromLdap(ldapUser: LDAPUser)(implicit s: DBSession = autoSession): UserId = {
    User.createWithAttributes(
      'name -> ldapUser.name,
      'email -> ldapUser.email,
      'image_url -> ldapUser.imageUrl,
      'locale -> "ja"
    )
  }

}
