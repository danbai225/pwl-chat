package com.github.danbai225.pwlchat.client

import com.github.danbai225.pwlchat.pj.Msg
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import okhttp3.*
import org.freedesktop.Hexdump.toHex
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.JTextPane


val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
val client = OkHttpClient()

class Client : WebSocketClient {
    var key: String? = null
    var oChat: JTextPane?=null
    var online:Int?=0
    var consoleScroll: JScrollPane?=null
    var userName:String?=null
    var password:String?=null
    constructor(draft: Draft?) : super(URI.create(PWL_WSS), draft) {}
    constructor() : super(URI.create(PWL_WSS)) {
        //加载数据
        load()
    }
    fun md5(str: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(str.toByteArray())
        return toHex(result).replace("\\s".toRegex(), "")
    }
    private fun load(){
        key = PropertiesComponent.getInstance().getValue("key")
        userName = PropertiesComponent.getInstance().getValue("userName")
        password = PropertiesComponent.getInstance().getValue("password")
    }
    fun save(){
       PropertiesComponent.getInstance().setValue("key",key)
        PropertiesComponent.getInstance().setValue("userName",userName)
        PropertiesComponent.getInstance().setValue("password",password)
    }
    override fun onOpen(handshakedata: ServerHandshake) {
        send("Hello, it is me. Mario :)")
        println("new connection opened")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("closed with exit code $code additional info: $reason")
    }

    override fun onMessage(message: String) {
        println("received message: $message")
        val msg=
            Gson().fromJson(message,Msg::class.java)
        when(msg.type){
            "msg" ->{
                val doc: Document = Jsoup.parse(msg.content)
                addMsgToOChat(doc.text(),msg.userName)
            }
            "online"->{
                online=msg.onlineChatCnt
            }
        }

    }

    override fun onMessage(message: ByteBuffer) {
        println("received ByteBuffer")
    }

    override fun onError(ex: Exception) {
        System.err.println("an error occurred:$ex")
    }
    fun addMsgToOChat(msg:String?,UserName:String?){
        val time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        oChat?.text+= "[$time] $UserName: $msg\n"
        gotoConsoleLow()
    }
    @Synchronized
    fun gotoConsoleLow() {
        val scrollBar: JScrollBar = consoleScroll?.verticalScrollBar!!
        scrollBar.value = scrollBar.maximum
        consoleScroll?.updateUI()
    }
    fun sendMsg(mgs:String){

    }
    fun post(url: String, json: String): String? {
        var requestBody: RequestBody? = RequestBody.create(JSON, java.lang.String.valueOf(json))
        val request: Request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        client.newCall(request).execute().use { response -> return response.body()?.string() }
    }
    fun login():Boolean{
        val md5p= password?.let { md5(it) }
        println(password+md5p)
        val rJson = post(PWL_LOGIN, "{\"nameOrEmail\":\"$userName\",\"userPassword\":\"$md5p\"}")
        println(rJson)
        return true
    }
    companion object {
        private const val PWL_WSS = "wss://pwl.icu/chat-room-channel"
        private const val PWL_LOGIN = "https://pwl.icu/api/getKey"
        private const val PWL_SEND = "https://pwl.icu/chat-room/send"
    }
}