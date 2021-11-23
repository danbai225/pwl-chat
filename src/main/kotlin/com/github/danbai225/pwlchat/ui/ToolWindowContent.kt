package com.github.danbai225.pwlchat.ui

import com.github.danbai225.pwlchat.client.Client
import com.intellij.openapi.ui.Messages
import javax.swing.*


class ToolWindowContent{
    var root: JPanel?=null
    var send: JButton?=null
    var iChat:JTextArea?=null
    var oChat:JTextPane?=null
    var client: Client?=Client()
    var consoleScroll:JScrollPane?=null
    init {
        send?.addActionListener {
            if (iChat?.text==""){

                client?.userName=Messages.showInputDialog("Username", "鱼油登录", Messages.getInformationIcon())
                client?.password=Messages.showInputDialog("Password", "鱼油登录", Messages.getInformationIcon())
                client?.login()
            }else{
                oChat?.text+=iChat?.text+"\n"
                iChat?.text=""
            }
        }
        client?.oChat=oChat
        client?.consoleScroll=consoleScroll
        client?.connect()
       // BorderLayout.WEST
    }
    fun getContent(): JComponent? {
        return root
    }
}
