package com.github.danbai225.pwlchat.ui

import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.*
import kotlin.concurrent.thread


class ToolWindowContent{
    /**
     * 返回创建的JPanel
     * @return JPanel
     */
    var root: JPanel?=null
    var web: JPanel?=null
    var content: JPanel?=null
    var contentPanel: JPanel?=null
    var chatSp: JScrollPane?=null
    var input: JTextArea?=null
    var send: JButton?=null
    /**
     * 构造函数
     */
    init {
        content= JPanel(BorderLayout())
        if (!JBCefApp.isSupported()) {
            content!!.add(JLabel("当前环境不支持JCEF", SwingConstants.CENTER))
        } else {
            var jbCefBrowser = JBCefBrowser()
            content!!.add(jbCefBrowser.component,BorderLayout.CENTER)
            jbCefBrowser?.loadURL("123")
        }

        send?.addActionListener {
            println("123")
            //jbCefBrowser?.loadURL("123")
            //textPane?.text = "123"
        }
    }
}