package com.github.danbai225.pwlchat.component

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.pj.Msg
import com.github.danbai225.pwlchat.pj.RedPack
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JComponent
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.JTextPane


class TextChat : JScrollPane(), oChat {
    private var loadHistory: Boolean = false
    var oChat: JTextPane = JTextPane()
    var clientApi: Client? = null
    var autoBottom: Boolean = true

    init {
        oChat.layout = BorderLayout()
        setViewportView(oChat)
        oChat.isEditable = false
    }

    private var lines = 0

    @Synchronized
    private fun gotoConsoleLow() {
        if (!getVerticalScrollBar().valueIsAdjusting) {
            oChat.caretPosition = oChat.document.length
        }
    }

    override fun addMsgToOChat(msg: Msg) {
        when (msg.type) {
            "msg" -> {
                if (msg.content.indexOf("\"msgType\":\"redPacket\"") > 0) {
                    //红包消息
                    val red =
                        Gson().fromJson(msg.content, RedPack::class.java)
                    addMsgToOChat(red.msg + "(🧧红包消息)", msg.userName, msg.time)
                } else {
                    val doc: Document = Jsoup.parse(msg.content)
                    var m = doc.text()
                    if (m.isEmpty()) {
                        m = "(表情.jpg)"
                    }
                    addMsgToOChat(m, msg.userName, msg.time)
                }
                linesADD()
            }
            //抢红包消息
            "redPacketStatus" -> {
                PropertiesComponent.getInstance().getBoolean("pwl_openMsg").let {
                    if (!it) {
                        addInfoToOChat("openPacket", "${msg.whoGot}抢到了${msg.whoGive}的红包")
                        linesADD()
                    }
                }
            }
        }
    }

    override fun addErrToOChat(op: String, msg: String) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        oChat.text += "[Err-$time] $op: $msg\n"
        gotoConsoleLow()
    }

    override fun addInfoToOChat(op: String, msg: String) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        oChat.text += "[Info-$time] $op: $msg\n"
        gotoConsoleLow()
    }

    override fun getComponent(): JComponent {
        return this
    }

    override fun setClient(client: Client) {
        clientApi = client
        clientApi?.more(1)
    }

    override fun loadHistory(boolean: Boolean): Boolean {
        if (boolean) {
            loadHistory = true
        }
        return loadHistory
    }

    override fun clear() {
        oChat.text = ""
    }

    override fun close() {
        isEnabled = true
    }

    private fun addMsgToOChat(msg: String?, UserName: String?, time: String) {
        oChat.text += "[$time] $UserName: $msg\n"
        gotoConsoleLow()
    }

    private fun linesADD() {
        lines++
        if (lines > 600) {
            val split = oChat.text?.split("\n")
            val newText = split?.slice(split.size.minus(100)..split.size.minus(1))
            oChat.text = newText?.joinToString(separator = "\n")
            lines = newText?.size!!
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TextChat::class.java)
    }
}