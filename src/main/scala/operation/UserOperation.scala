package operation

import java.util.Locale

import com.github.t3hnar.bcrypt._
import lib.Sha1Digest
import model.User
import model.typebinder.UserId
import org.joda.time.DateTime
import scalikejdbc.DBSession
import skinny.PermittedStrongParameters
import view_model._

/**
 * The operation for users.
 */
sealed trait UserOperation extends OperationBase {

  def get(email: String)(implicit s: DBSession = User.autoSession): Option[User]
  def register(permittedAttributes: PermittedStrongParameters, locale: Locale)(implicit s: DBSession = User.autoSession): User
  def updateToConfirmable(user: User, permittedAttributes: PermittedStrongParameters, locale: Locale)(implicit s: DBSession = User.autoSession): User
  def getVerifyable(token: String)(implicit s: DBSession = User.autoSession): Option[User]
  def activate(userId: UserId)(implicit s: DBSession = User.autoSession): User

  def updateToPasswordResettable(user: User)(implicit s: DBSession = User.autoSession): User
  def getPasswordResettable(token: String)(implicit s: DBSession = User.autoSession): Option[User]
  def resetPassword(user: User, password: String)(implicit s: DBSession = User.autoSession): User

  def getWithStats(userId: UserId)(implicit s: DBSession = User.autoSession): Option[UserWithStats]

  def update(id: UserId, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = User.autoSession): User

}

class UserOperationImpl extends UserOperation {

  override def get(email: String)(implicit s: DBSession = User.autoSession): Option[User] = {
    User.findByEmail(email)
  }

  override def register(permittedAttributes: PermittedStrongParameters, locale: Locale)(implicit s: DBSession = User.autoSession): User = {
    val u = User.column
    val email = getParameterAsString("email", permittedAttributes)
    val token: String = new Sha1Digest(email + "_" + DateTime.now().toString).digestString
    val id = User.createWithNamedValues(
      u.name -> email.split("@")(0),
      u.email -> email,
      u.password -> getParameterAsString("password", permittedAttributes).bcrypt,
      u.imageUrl -> "/assets/img/avatar_default.jpg",
      u.locale -> locale.toString,
      u.confirmationToken -> token,
      u.confirmationSentAt -> DateTime.now(),
      u.isActive -> false
    )
    User.findById(id).get
  }

  override def updateToConfirmable(user: User, permittedAttributes: PermittedStrongParameters, locale: Locale)(implicit s: DBSession = User.autoSession): User = {
    val u = User.column
    val token: String = new Sha1Digest(user.email + "_" + DateTime.now().toString).digestString
    User.updateById(user.userId).withNamedValues(
      u.password -> getParameterAsString("password", permittedAttributes).bcrypt,
      u.locale -> locale.toString,
      u.confirmationToken -> token,
      u.confirmationSentAt -> DateTime.now(),
      u.isActive -> false
    )
    User.findById(user.userId).get
  }

  override def getVerifyable(token: String)(implicit s: DBSession = User.autoSession): Option[User] = {
    User.findByConfirmationToken(token).filter { user =>
      user.confirmationSentAt.exists { sentAt =>
        val expiredAt = sentAt.plusDays(1)
        expiredAt.isAfterNow
      }
    }
  }

  override def activate(userId: UserId)(implicit s: DBSession = User.autoSession): User = {
    val u = User.column
    User.updateById(userId).withNamedValues(
      u.confirmationToken -> null,
      u.confirmationSentAt -> null,
      u.isActive -> true
    )
    User.findById(userId).get
  }

  override def updateToPasswordResettable(user: User)(implicit s: DBSession = User.autoSession): User = {
    val u = User.column
    val token: String = new Sha1Digest(user.email + "_" + DateTime.now().toString).digestString
    User.updateById(user.userId).withNamedValues(
      u.resetPasswordToken -> token,
      u.resetPasswordSentAt -> DateTime.now()
    )
    User.findById(user.userId).get
  }

  override def getPasswordResettable(token: String)(implicit s: DBSession = User.autoSession): Option[User] = {
    User.findByResetPasswordToken(token).filter { user =>
      user.resetPasswordSentAt.exists { sentAt =>
        val expiredAt = sentAt.plusDays(1)
        expiredAt.isAfterNow
      }
    }
  }

  override def resetPassword(user: User, password: String)(implicit s: DBSession): User = {
    val u = User.column
    User.updateById(user.userId).withNamedValues(
      u.password -> password.bcrypt
    )
    User.findById(user.userId).get
  }

  override def getWithStats(userId: UserId)(implicit s: DBSession = User.autoSession): Option[UserWithStats] = {
    User.findById(userId).map { user =>
      val contribution = User.calcContribution(user.userId)
      val reactions = User.calcReactions(user.userId)
      UserWithStats(user, contribution, reactions._1, reactions._2)
    } orElse None
  }

  override def update(id: UserId, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = User.autoSession): User = {
    User.updateById(id).withPermittedAttributes(permittedAttributes)
    User.findById(id).get
  }

}