package com.github.danbai225.pwlchat.client

import com.github.danbai225.pwlchat.pj.Liveness
import com.github.danbai225.pwlchat.pj.Msg
import com.github.danbai225.pwlchat.pj.RedPack
import com.github.danbai225.pwlchat.pj.loginInfo
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.jetbrains.rd.util.use
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.JTextPane


val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
val client = OkHttpClient()
val logger = LoggerFactory.getLogger(Client::class.java)
class Client : WebSocketClient {
    var cookie: String? = ""
    var oChat: JTextPane? = null
    var online: Int? = 0
    var consoleScroll: JScrollPane? = null
    var userName: String? = ""
    var password: String? = ""
    var islogin: Boolean = false
    var liveness = 0.0
    var pklist:ArrayList<String> =ArrayList()
    var numberOfReconnections=0
    constructor(draft: Draft?) : super(URI.create(PWL_WSS), draft) {}
    constructor() : super(URI.create(PWL_WSS)) {
        //加载数据
        load()
    }
    private fun md5(input: String?): String? {
        if (input == null || input.length == 0) {
            return null
        }
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(input.toByteArray())
            val byteArray = md5.digest()
            val sb = StringBuilder()
            for (b in byteArray) {
                // 一个byte格式化成两位的16进制，不足两位高位补零
                sb.append(String.format("%02x", b))
            }
            return sb.toString().replace("\\s".toRegex(), "").toLowerCase()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    fun verifyLogin(): Boolean {
        if (cookie != "") {
            var get = get(PWL_LIVE)
            get?.execute().use { response ->
                if (response?.code() == 200) {
                    var j = response?.body()?.string()
                    val msg =
                        Gson().fromJson(j, Liveness::class.java)
                    liveness = msg.liveness
                    islogin = true
                    return true
                }
            }
        }
        return false
    }

    private fun load() {
        if (PropertiesComponent.getInstance().getValue("pwl_cookie") != null) {
            cookie = PropertiesComponent.getInstance().getValue("pwl_cookie")
        }
        if (PropertiesComponent.getInstance().getValue("pwl_userName") != null) {
            userName = PropertiesComponent.getInstance().getValue("pwl_userName")
        }
        if (PropertiesComponent.getInstance().getValue("pwl_password") != null) {
            password = PropertiesComponent.getInstance().getValue("pwl_password")
        }
    }

    fun save() {
        PropertiesComponent.getInstance().setValue("pwl_cookie", cookie)
        PropertiesComponent.getInstance().setValue("pwl_userName", userName)
        PropertiesComponent.getInstance().setValue("pwl_password", password)
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        logger.info("new connection opened")
        numberOfReconnections=0
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        logger.info("closed with exit code $code additional info: $reason")
        if(numberOfReconnections<MAX_R){
            numberOfReconnections++
            Thread.sleep((numberOfReconnections*1500).toLong())
            connect()
        }
    }

    override fun onMessage(message: String) {
        //println("received message: $message")
        val msg =
            Gson().fromJson(message, Msg::class.java)
        when (msg.type) {
            "msg" -> {
                if (msg.content.indexOf("\"msgType\":\"redPacket\"")>0){
                    //红包消息
                    val red =
                        Gson().fromJson(msg.content, RedPack::class.java)
                    msg.oId?.let { pklist.add(it) }
                    addMsgToOChat(red.msg+"(🧧红包消息)", msg.userName)
                }else{
                    val doc: Document = Jsoup.parse(msg.content)
                    var m=doc.text()
                    if (m.length==0){
                        m="(表情.jpg)"
                    }
                    addMsgToOChat(m, msg.userName)
                }
            }
            "online" -> {
                online = msg.onlineChatCnt
            }
        }

    }

    override fun onMessage(message: ByteBuffer) {
        logger.info("received ByteBuffer")
    }

    override fun onError(ex: Exception) {
        logger.error("an error occurred:$ex")
    }

    fun addMsgToOChat(msg: String?, UserName: String?) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        oChat?.text += "[$time] $UserName: $msg\n"
        gotoConsoleLow()
    }

    @Synchronized
    fun gotoConsoleLow() {
        val scrollBar: JScrollBar = consoleScroll?.verticalScrollBar!!
        scrollBar.value = scrollBar.maximum
        consoleScroll?.updateUI()
    }

    fun sendMsg(msg: String) {
        if(isClosed){
            connect()
        }
        if (msg.isEmpty()){
            return
        }
        val call = post(
            PWL_SEND,
            "{\"content\":\"$msg\"}"
        )
        call?.execute().use {}
        if(pklist.size>0){
            val selectedSeries = pklist.toMutableList()
            pklist.clear()
            GlobalScope.launch {
                selectedSeries.forEach {
                    post(PWL_OPEN, "{\"oId\":\"$it\"}")?.execute()
                }
            }
        }
    }

    fun post(url: String, json: String): Call? {
        var requestBody: RequestBody? = RequestBody.create(JSON, java.lang.String.valueOf(json))
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Cookie", cookie)
            .post(requestBody)
            .build()
        return client.newCall(request)
    }

    fun get(url: String): Call? {
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Cookie", cookie)
            .get()
            .build()
        return client.newCall(request)
    }

    fun login(): Boolean {
        val md5p = password?.let { md5(it) }
        println(password + md5p)
        val call = post(
            PWL_LOGIN,
            "{\"nameOrEmail\":\"$userName\",\"userPassword\":\"$md5p\",\"rememberLogin\":true,\"captcha\":\"\"}"
        )
        call?.execute().use { response ->
            var j = response?.body()?.string()
            val msg =
                Gson().fromJson(j, loginInfo::class.java)
            if (msg.code == 0) {
                islogin = true
                response?.headers("set-cookie")?.forEach {
                    cookie += it
                }
                cookie = response?.header("set-cookie")
                save()
                return true
            }
        }
        return false
    }

    //teyxBFF7JjkXHv
    companion object {
        private const val PWL_WSS = "wss://pwl.icu/chat-room-channel"
        private const val PWL_KEY = "https://pwl.icu/api/getKey"
        private const val PWL_LOGIN = "https://pwl.icu/login"
        private const val PWL_LIVE = "https://pwl.icu/user/liveness"
        private const val PWL_SEND = "https://pwl.icu/chat-room/send"
        private const val PWL_OPEN = "https://pwl.icu/chat-room/red-packet/open"
        private const val MAX_R = 15
    }

}