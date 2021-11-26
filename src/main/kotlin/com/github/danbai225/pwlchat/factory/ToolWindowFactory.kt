package com.github.danbai225.pwlchat.factory

import com.github.danbai225.pwlchat.ui.ToolWindowContent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager

class ToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager: ContentManager = toolWindow.contentManager
        val labelContent: Content = contentManager.factory
            .createContent(
                ToolWindowContent(project).getContent(),
                "DebugCommandLine",
                false
            )
        contentManager.addContent(labelContent)
    }
}