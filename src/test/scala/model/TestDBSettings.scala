package model

import org.flywaydb.core.Flyway
import org.scalatest.{ BeforeAndAfterEach, Suite }
import scalikejdbc.ConnectionPool
import skinny._
import skinny.exception.DBSettingsException

trait TestDBSettings extends BeforeAndAfterEach with DBSettings { this: Suite =>

  override protected def beforeEach(): Unit = {
    clean()
    dbmigration.DBMigration.migrate()
  }

  private def clean(env: String = SkinnyEnv.Test, poolName: String = ConnectionPool.DEFAULT_NAME.name): Unit = {
    val skinnyEnv = SkinnyEnv.get()
    try {
      System.setProperty(SkinnyEnv.PropertyKey, env)
      DBSettings.initialize()
      try {
        val pool = ConnectionPool.get(Symbol(poolName))
        val flyway = new Flyway
        flyway.setDataSource(pool.dataSource)
        flyway.clean()
      } catch {
        case e: IllegalStateException =>
          throw new DBSettingsException(s"ConnectionPool named $poolName is not found.")
      }
    } finally {
      skinnyEnv.foreach { env => System.setProperty(SkinnyEnv.PropertyKey, env) }
      DBSettings.initialize()
    }
  }

}
