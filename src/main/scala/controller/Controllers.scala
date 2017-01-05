package controller

import lib.UploadedBaseURL
import model.typebinder._
import skinny._
import skinny.controller.AssetsController

object Controllers {

  def mount(ctx: ServletContext): Unit = {
    AssetsController.mount(ctx)
    api.mount(ctx)
    root.mount(ctx)
    login.mount(ctx)
    articles.mount(ctx)
    tags.mount(ctx)
    comments.mount(ctx)
    stocks.mount(ctx)
    likes.mount(ctx)
    users.mount(ctx)
    upload.mount(ctx)
    search.mount(ctx)
    notifications.mount(ctx)

    if (login.providor.isApp) {
      signup.mount(ctx)
      recover.mount(ctx)
    }
    if (upload.destination.isLocal) {
      uploadAssets.mount(ctx)
    }
  }

  val trackingEnabled: Boolean = SkinnyConfig.booleanConfigValue("tracking.enabled").getOrElse(false)

  object api extends ApiController with Routes {
    // --------------
    // preview
    val previewUrl = post("/api/preview")(preview).as('preview)

    // --------------
    // tags
    val tagsUrl = get("/api/tags")(tags).as('tags)
  }

  object root extends RootController with Routes {
    val indexUrl = get("/")(index).as('index)
    val scrollUrl = get("/more/:maxId") {
      params.getAs[Long]("maxId").map(maxId => more(ArticleId(maxId))).getOrElse(haltWithBody(404))
    }.as('scroll)
  }

  object signup extends SignupController with Routes {

    // --------------
    // input
    val inputUrl = get("/signup")(input).as('input)

    // --------------
    // create
    val createUrl = post("/signup")(create).as('create)

    // --------------
    // create redirect
    val createRedirectUrl = get("/signup/_")(createRedirect).as('createRedirect)

    // --------------
    // verify
    val verifyUrl = get("/signup/verify/:token") {
      params.getAs[String]("token").map(token => verify(token)).getOrElse(haltWithBody(404))
    }.as('verify)

  }

  object recover extends RecoverController with Routes {
    // --------------
    // GET /recover
    val inputUrl = get("/recover")(input).as('input)

    // --------------
    // POST /recover
    val recoverUrl = post("/recover")(recover).as('recover)

    // --------------
    // GET /recover/_
    val recoverRedirectUrl = get("/recover/_")(recoverRedirect).as('recoverRedirect)

    // --------------
    // GET /recover/verify/:token
    val verifyUrl = get("/recover/verify/:token") {
      params.getAs[String]("token").map(token => verify(token)).getOrElse(haltWithBody(404))
    }.as('verify)

    // --------------
    // POST /recover/password
    val resetPasswordUrl = post("/recover/password")(reset).as('resetPassword)

  }

  // --------------
  // login
  def login: LoginController = LoginControllerFactory.create

  object articles extends ArticlesController with Routes {

    // --------------
    // show
    val showUrl = get("/articles/:id") {
      params.getAs[Long]("id").map(id => index(ArticleId(id))).getOrElse(haltWithBody(404))
    }.as('show)

    // --------------
    // create
    val newUrl = get("/articles/new")(input).as('new)
    val createUrl = post("/articles")(create)

    // --------------
    // update
    val editUrl = get("/articles/:id/edit") {
      params.getAs[Long]("id").map(id => edit(ArticleId(id))).getOrElse(haltWithBody(404))
    }.as('edit)
    val updateUrl = post("/articles/:id") {
      params.getAs[Long]("id").map(id => update(ArticleId(id))).getOrElse(haltWithBody(404))
    }.as('update)

    // --------------
    // stockers
    val showStockersUrl = get("/articles/:id/stockers") {
      params.getAs[Long]("id").map(id => stockers(ArticleId(id))).getOrElse(haltWithBody(404))
    }.as('showStockers)

  }

  object tags extends TagsController with Routes {
    // --------------
    // show
    val indexUrl = get("/tags/:name") {
      params.getAs[String]("name").map(name => index(name)).getOrElse(haltWithBody(404))
    }.as('list)
  }

  object comments extends CommentsController with Routes {
    // --------------
    // create
    val createUrl = post("/comments")(create).as('create)

    // --------------
    // update
    val updateUrl = post("/comments/:id") {
      params.getAs[Long]("id").map(id => update(CommentId(id))).getOrElse(haltWithBody(404))
    }.as('update)

    // --------------
    // delete
    val deleteUrl = delete("/comments/:id") {
      params.getAs[Long]("id").map(id => delete(CommentId(id))).getOrElse(haltWithBody(404))
    }.as('delete)
  }

  object stocks extends StocksController with Routes {
    // --------------
    // create
    val createUrl = post("/stocks")(stock).as('create)

    // --------------
    // delete
    val destroyUrl = delete("/stocks")(unstock).as('destroy)
  }

  object likes extends LikesController with Routes {
    // --------------
    // create
    val createUrl = post("/likes")(like).as('create)

    // --------------
    // delete
    val destroyUrl = delete("/likes")(unlike).as('destroy)
  }

  object users extends UsersController with Routes {

    // --------------
    // show
    val showUrl = get("/users/:id") {
      params.getAs[Long]("id").map(id => index(UserId(id))).getOrElse(haltWithBody(404))
    }.as('show)

    // --------------
    // stocks
    val stocksUrl = get("/users/:id/stocks")(stocks).as('stocks)

    // --------------
    // update
    val editUrl = get("/users/:id/edit") {
      params.getAs[Long]("id").map(id => edit(UserId(id))).getOrElse(haltWithBody(404))
    }.as('edit)
    val updateUrl = post("/users/:id") {
      params.getAs[Long]("id").map(id => update(UserId(id))).getOrElse(haltWithBody(404))
    }.as('update)
  }

  // --------------
  // uploads
  def upload = UploadControllerFactory.create
  val uploadedFileBaseUrl: UploadedBaseURL = upload.uploadedFileBaseUrl

  object uploadAssets extends _root_.controller.upload.LocalUploadAssetsController with Routes {
    val fileUrl = get(s"${uploadedFileBaseUrl.value}*")(file).as('file)
  }

  object search extends SearchController with Routes {

    // --------------
    // search
    val searchUrl = get("/search") {
      params.getAs[String]("q").map(q => search(q)).getOrElse(haltWithBody(404))
    }.as('search)
  }

  object notifications extends NotificationsController with Routes {

    // --------------
    // index
    val notificationUrl = get("/notifications")(index).as('index)

    // --------------
    // state
    val notificationStateUrl = post("/notifications")(state).as('state)
  }

}
