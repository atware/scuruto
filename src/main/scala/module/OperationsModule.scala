package module

import operation._

class OperationsModule extends scaldi.Module {

  bind[ArticleOperation] to new ArticleOperationImpl
  bind[CommentOperation] to new CommentOperationImpl
  bind[LikeOperation] to new LikeOperationImpl
  bind[StockOperation] to new StockOperationImpl
  bind[TagOperation] to new TagOperationImpl
  bind[UserOperation] to new UserOperationImpl
  bind[SearchOperation] to new SearchOperationImpl
  bind[NotificationOperation] to new NotificationOperationImpl
  bind[OgpOperation] to new OgpOperationImpl

}
