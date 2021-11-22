package com.github.danbai225.pwlchat.factory
import com.github.danbai225.pwlchat.ui.ToolWindowContent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager

class ToolWindowFactory : ToolWindowFactory {
   override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // 此处方法将会在点击ToolWindow的时候触发
        // 获取ContentManager
        val contentManager: ContentManager = toolWindow.contentManager
        val labelContent: Content = contentManager.factory // 内容管理器获取工厂类
            .createContent( // 创建Content（组件类实例、显示名称、是否可以锁定）
                ToolWindowContent().root,
                "MyTab",
                false
            )
        // 利用ContentManager添加Content
        contentManager.addContent(labelContent)
    }
}