package controller

import operation.NotificationOperation
import view_model.NotificationArea

class NotificationsController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /notifications
  def index = {
    loginUser.map { user =>
      val notificationOperation = inject[NotificationOperation]
      val notificationArea = notificationOperation.getNotifications(user)
      toJSONString(notificationArea)
    } getOrElse haltWithBody(403)
  }

  // --------------
  // POST /notifications
  def state = {
    loginUser.map { user =>
      val notificationOperation = inject[NotificationOperation]
      notificationOperation.updateStateAsRead(user)
      toJSONString(NotificationArea(count = 0, Seq()))
    } getOrElse haltWithBody(403)
  }

}
