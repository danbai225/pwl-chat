package com.github.danbai225.pwlchat.notify

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

fun sendNotify(project: Project, title: String?, content: String?, type: NotificationType?) {
    NotificationGroupManager.getInstance().getNotificationGroup("com.github.danbai225.pwlchat.notify")
        .createNotification(content!!, type!!)
        .setTitle(title)
        .notify(project)
}
