package com.github.danbai225.pwlchat.client

import com.github.danbai225.pwlchat.pj.*
import com.github.danbai225.pwlchat.utils.StringUtils
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.use
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URI
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class Client{
    /**
     * 基础属性和方法
     */
    private var cookie: String = ""
    private var online: Int? = 0
    private val client = OkHttpClient()
    var userName: String? = ""
    var password: String? = ""
    var isLogin: Boolean = false
    var eventLog:Boolean=false
    private var onlineVitality= 0.0
    private var pkList: ArrayList<String> = ArrayList()
    private var numberOfReconnections = 0
    private var lines = 0
    private var lastOid: String? = ""
    var consoleScroll: JScrollPane? = null
    var oChat: JTextPane? = null
    var project: Project?= null
    var userListModel: DefaultListModel<String>? = null
    var userLabel: JLabel? = null
    private var ws:ws?=null
    init {
        load()
        connect()
        val timer = timer("定时Thread_name", false, 2000, 1000){
            gotoConsoleLow()
            if(ws?.isClosed==true){
                addInfoToOChat("ws","连接已断开")
                connect()
                if(ws?.isClosed==false){
                    addInfoToOChat("ws","连接已恢复")
                }
            }
        }
    }
    //加载持久数据
    private fun load() {
          PropertiesComponent.getInstance().getValue("pwl_cookie").let {
              if (it != null) {
                  cookie=it
              }
          }
        PropertiesComponent.getInstance().getValue("pwl_userName").let {
            if (it != null) {
                userName=it
            }
        }
        PropertiesComponent.getInstance().getValue("pwl_password").let {
            if (it != null) {
                password=it
            }
        }
        PropertiesComponent.getInstance().getBoolean("pwl_eventLog").let {
            eventLog=it
        }
    }
    //数据持久化
    fun save() {
        PropertiesComponent.getInstance().setValue("pwl_cookie", cookie)
        PropertiesComponent.getInstance().setValue("pwl_userName", userName)
        PropertiesComponent.getInstance().setValue("pwl_password", password)
        PropertiesComponent.getInstance().setValue("pwl_eventLog", eventLog)
    }

    /**
     * HTTP方法区
     */
    private fun post(url: String, json: String): Call? {
        val requestBody: RequestBody = RequestBody.create(JSON, java.lang.String.valueOf(json))
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Cookie", cookie)
            .post(requestBody)
            .build()
        return client.newCall(request)
    }

    private fun delete(url: String): Call? {
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Cookie", cookie)
            .delete()
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

    /**
     * 客户端接口区
     */
    //登陆有效性验证
    fun verifyLogin(): Boolean {
        if (cookie != "") {
            val get = get(PWL_LIVE)
            get?.execute().use { response ->
                if (response?.code() == 200) {
                    val msg = Gson().fromJson(response?.body()?.string(), Liveness::class.java)
                    onlineVitality = msg.liveness
                    isLogin = true
                    return true
                }
            }
        }
        return false
    }
    //发送红包
    fun packet(count: Int, money: Int, msg: String) {
        var mm = msg
        if (msg.isEmpty()) {
            mm = "摸鱼红包"
        }
        sendMsg("[redpacket]{\\\"money\\\":\\\"$money\\\",\\\"count\\\":\\\"$count\\\",\\\"msg\\\":\\\"$mm\\\"}[/redpacket]")
    }
    //发送消息
    fun sendMsg(msg: String) {
        if (msg.isEmpty()) {
            return
        }
        GlobalScope.launch {
            val call = post(
                PWL_SEND,
                "{\"content\":\"$msg\"}"
            )
            try {
                call?.execute().use {
                    val res =
                        Gson().fromJson(it?.body()?.string(), Msg::class.java)
                    if (res.code != 0) {
                        logger.error(res.msg)
                        addErrToOChat("sendMsg", res.msg)
                    }
                }
            } catch (e: Exception) {
                addErrToOChat("sendMsg", e.message)
            }

            if (pkList.size > 0) {
                val selectedSeries = pkList.toMutableList()
                pkList.clear()
                selectedSeries.forEach {
                    post(PWL_OPEN, "{\"oId\":\"$it\"}")?.execute().use { rs ->
                        val res = Gson().fromJson(rs?.body()?.string(), RedPack::class.java)
                        res.who?.forEach {
                            r->
                            if (r.userName == userName) {
                                addInfoToOChat("openPacket", "你抢到了${res.info?.userName}的红包,${r.userMoney}$")
                            }
                        }
                    }
                }
            }
        }
    }

    //撤回消息
    fun revoke() {
        delete("$PWL_REVOKE$lastOid")?.execute().use {
            val msg = Gson().fromJson(it?.body()?.string(), Msg::class.java)
            if (msg.code != 0) {
                addErrToOChat("revoke", msg.msg)
            }
        }
    }
    //登陆
    fun login(): Boolean {
        val md5p = password?.let { StringUtils.md5(it) }
        val call = post(
            PWL_LOGIN,
            "{\"nameOrEmail\":\"$userName\",\"userPassword\":\"$md5p\",\"rememberLogin\":true,\"captcha\":\"\"}"
        )
        call?.execute().use { response ->
            val msg = Gson().fromJson(response?.body()?.string(), loginInfo::class.java)
            if (msg.code == 0) {
                isLogin = true
                cookie = "sym-ce=${msg.token}; "
                save()
                if (verifyLogin()) {
                    return true
                }
            }
        }
        return false
    }
    fun exit(){
        cookie=""
        userName=""
        password=""
        isLogin=false
    }
    fun upload(file: File): String? {
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file[]", file.name,
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            )
            .build()
        val request: Request = Request.Builder()
            .url(PWL_UPLOAD)
            .addHeader("Cookie", cookie)
            .post(requestBody)
            .build()
        try {
            sendNotify("上传提示","文件上传中",NotificationType.INFORMATION)
            client.newCall(request).execute().use {
                val msg = Gson().fromJson(it.body()?.string(), Upload::class.java)
                return msg.data?.succMap?.get(file.name)
            }
        } catch (e: Exception) {
            addErrToOChat("upload", e.message)
        }
        return ""
    }
    /**
     * WebSocket实现区
     */
    private fun connect() {
        ws=ws()
        ws?.client=this
        ws?.connect()
    }

     fun onMessage(message: String) {
        //println("received message: $message")
        val msg =
            Gson().fromJson(message, Msg::class.java)
        when (msg.type) {
            "msg" -> {
                if (msg.content.indexOf("\"msgType\":\"redPacket\"") > 0) {
                    //红包消息
                    val red =
                        Gson().fromJson(msg.content, RedPack::class.java)
                    msg.oId?.let {
                        if (pkList.size > 100) {
                            pkList.removeAt(0)
                        }
                        pkList.add(it)
                    }
                    addMsgToOChat(red.msg + "(🧧红包消息)", msg.userName)
                } else {
                    val doc: Document = Jsoup.parse(msg.content)
                    var m = doc.text()
                    if (m.isEmpty()) {
                        m = "(表情.jpg)"
                    }
                    if (msg.userName == userName) {
                        lastOid = msg.oId
                    }
                    addMsgToOChat(m, msg.userName)
                }
            }
            "online" -> {
                online = msg.onlineChatCnt
                userListModel?.clear()
                msg.users?.forEach { user ->
                    userListModel?.addElement(user.userName)
                }
                userLabel?.text="Online ${msg.users?.size}"
            }
            //抢红包消息
            "redPacketStatus" -> {
                addInfoToOChat("openPacket", "${msg.whoGot}抢到了${msg.whoGive}的红包")
            }
        }

    }


    /**
     * UI控制区
     */
    private fun addMsgToOChat(msg: String?, UserName: String?) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        oChat?.text += "[$time] $UserName: $msg\n"
        linesADD()
        if(eventLog){
            var a:String?=""
            while (a?.length!! <(msg?.length?.div(4)!!)){
                a+=" "
            }
            sendNotify(UserName!!, msg!!+" "+a, NotificationType.INFORMATION)
        }
    }

    private fun addErrToOChat(op: String?, msg: String?) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        oChat?.text += "[Err-$time] $op: $msg\n"
        linesADD()
    }

    private fun addInfoToOChat(op: String?, msg: String?) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())
        oChat?.text += "[Info-$time] $op: $msg\n"
        linesADD()
    }

    private fun linesADD() {
        lines++
        if (lines > 200) {
            val split = oChat?.text?.split("\n")
            val newText = split?.slice(split.size.minus(100)..split.size.minus(1))
            oChat?.text = newText?.joinToString(separator = "\n")
            lines = newText?.size!!
        }
    }

    @Synchronized
    fun gotoConsoleLow() {
        consoleScroll?.verticalScrollBar?.value = consoleScroll?.verticalScrollBar?.maximum!!
        consoleScroll?.updateUI()
    }
    private fun sendNotify(title: String, content: String, type: NotificationType) {
        project?.let { com.github.danbai225.pwlchat.notify.sendNotify(it, title, content, type) }
    }
    /**
     * 静态常量
     */
    //teyxBFF7JjkXHv
    companion object {
         const val PWL_WSS = "wss://pwl.icu/chat-room-channel"
         const val PWL_LOGIN = "https://pwl.icu/login"
         const val PWL_LIVE = "https://pwl.icu/user/liveness"
         const val PWL_SEND = "https://pwl.icu/chat-room/send"
         const val PWL_OPEN = "https://pwl.icu/chat-room/red-packet/open"
         const val PWL_REVOKE = "https://pwl.icu/chat-room/revoke/"
         const val PWL_UPLOAD="https://pwl.icu/upload"
         val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
         val logger: Logger = LoggerFactory.getLogger(Client::class.java)
    }

}