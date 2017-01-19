package controller

import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import skinny.ServletContext

class ControllersSpec extends FlatSpec with Matchers with MockitoSugar {

  it should "mount" in {
    Controllers.mount(mock[ServletContext])
  }

  it should "have urls" in {
    Controllers.api.previewUrl.toString should equal("/api/preview")
    Controllers.api.tagsUrl.toString should equal("/api/tags")

    Controllers.root.indexUrl.toString should equal("/")
    Controllers.root.scrollUrl.toString should equal("/more/:maxId")

    Controllers.login.loginUrl.toString should equal("/session")
    Controllers.login.loginCallbackUrl.toString should equal("/session/callback")
    Controllers.login.logoutUrl.toString should equal("/session")

    Controllers.articles.showUrl.toString should equal("/articles/:id")
    Controllers.articles.newUrl.toString should equal("/articles/new")
    Controllers.articles.editUrl.toString should equal("/articles/:id/edit")
    Controllers.articles.updateUrl.toString should equal("/articles/:id")
    Controllers.articles.showStockersUrl.toString should equal("/articles/:id/stockers")

    Controllers.tags.indexUrl.toString should equal("/tags/:name")

    Controllers.comments.createUrl.toString should equal("/comments")

    Controllers.stocks.createUrl.toString should equal("/stocks")
    Controllers.stocks.destroyUrl.toString should equal("/stocks")

    Controllers.likes.createUrl.toString should equal("/likes")
    Controllers.likes.destroyUrl.toString should equal("/likes")

    Controllers.users.showUrl.toString should equal("/users/:id")
    Controllers.users.stocksUrl.toString should equal("/users/:id/stocks")
    Controllers.users.editUrl.toString should equal("/users/:id/edit")
    Controllers.users.updateUrl.toString should equal("/users/:id")

    Controllers.upload.policiesUrl.toString should equal("/upload/policies")
    Controllers.upload.uploadUrl.toString should equal("/upload/file")

    Controllers.search.searchUrl.toString should equal("/search")

    Controllers.notifications.notificationUrl.toString should equal("/notifications")
    Controllers.notifications.notificationStateUrl.toString should equal("/notifications")
  }

}
