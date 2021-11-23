package com.github.danbai225.pwlchat.ui

import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.*


class ToolWindowContent{
    var root: JPanel?=JPanel(BorderLayout())
    var send: JButton?=JButton("发送")
    var pan:JTextArea?=JTextArea()
    val browser: JBCefBrowser=JBCefBrowser()
    init {
        root?.add(centerContent(), BorderLayout.NORTH)
        root?.add(inputControl(),BorderLayout.SOUTH)
        //browser.loadURL("https://p00q.cn")
       // browser.openDevtools()
        send?.addActionListener {
            println("123")

        }
    }
    fun getContent(): JComponent? {
        return root
    }
    private fun inputControl(): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout()
        pan?.text="123"
        panel.add(pan,BorderLayout.WEST)
        panel.add(send,BorderLayout.EAST)
        return panel
    }
    private fun centerContent(): JPanel {
        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(browser.component, BorderLayout.CENTER)
        return panel
    }
}
