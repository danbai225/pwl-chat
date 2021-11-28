package com.github.danbai225.pwlchat.component

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.pj.Msg
import com.github.danbai225.pwlchat.pj.RedPack
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JComponent
import javax.swing.JTextPane

open class TextChat : JTextPane(), oChat {
    init {
        layout = BorderLayout()
    }

    private var lines = 0
    override fun addMsgToOChat(msg: Msg) {
        when (msg.type) {
            "msg" -> {
                if (msg.content.indexOf("\"msgType\":\"redPacket\"") > 0) {
                    //红包消息
                    val red =
                        Gson().fromJson(msg.content, RedPack::class.java)
                    addMsgToOChat(red.msg + "(🧧红包消息)", msg.userName)
                } else {
                    val doc: Document = Jsoup.parse(msg.content)
                    var m = doc.text()
                    if (m.isEmpty()) {
                        m = "(表情.jpg)"
                    }
                    addMsgToOChat(m, msg.userName)
                }
                linesADD()
            }
            //抢红包消息
            "redPacketStatus" -> {
                addInfoToOChat("openPacket", "${msg.whoGot}抢到了${msg.whoGive}的红包")
                linesADD()
            }
        }
    }

    override fun addErrToOChat(op: String, msg: String) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        text += "[Err-$time] $op: $msg\n"
    }

    override fun addInfoToOChat(op: String, msg: String) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        text += "[Info-$time] $op: $msg\n"
    }

    override fun getComponent(): JComponent {
        return this
    }
    override fun setClient(client: Client) {

    }

    private fun addMsgToOChat(msg: String?, UserName: String?) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        text += "[$time] $UserName: $msg\n"
    }

    private fun linesADD() {
        lines++
        if (lines > 200) {
            val split = text?.split("\n")
            val newText = split?.slice(split.size.minus(100)..split.size.minus(1))
            text = newText?.joinToString(separator = "\n")
            lines = newText?.size!!
        }
    }
    companion object{
        private val logger: Logger = LoggerFactory.getLogger(TextChat::class.java)
    }
}