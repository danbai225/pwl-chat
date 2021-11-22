package com.github.danbai225.pwlchat.ui

import javax.swing.JPanel
import java.awt.BorderLayout
import com.intellij.ui.jcef.JBCefApp
import javax.swing.JLabel
import javax.swing.SwingConstants
import com.intellij.ui.jcef.JBCefBrowser

class ToolWindowContent {
    /**
     * 返回创建的JPanel
     * @return JPanel
     */
    var content: JPanel? = null

    /**
     * 构造函数
     */
    init {
        content = JPanel(BorderLayout())
        // 判断所处的IDEA环境是否支持JCEF
        if (!JBCefApp.isSupported()) {
            content!!.add(JLabel("当前环境不支持JCEF", SwingConstants.CENTER))

        }else{
            // 创建 JBCefBrowser
            val jbCefBrowser = JBCefBrowser()
            // 将 JBCefBrowser 的UI控件设置到Panel中
            content!!.add(jbCefBrowser.component, BorderLayout.CENTER)
            // 加载URL
            jbCefBrowser.loadURL("https://pwl.icu")
        }
    }
}