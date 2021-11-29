package com.github.danbai225.pwlchat.client

import com.github.danbai225.pwlchat.component.oChat
import com.github.danbai225.pwlchat.pj.*
import com.github.danbai225.pwlchat.utils.StringUtils
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.use
import com.sun.jna.StringArray
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JScrollPane
import kotlin.concurrent.timer

class Client {
    /**
     * 基础属性和方法
     */
    private var cookie: String = ""
    private var online: Int? = 0
    private val client = OkHttpClient()
    var userName: String? = ""
    var password: String? = ""
    var isLogin: Boolean = false
    var eventLog: Boolean = false
    private var onlineVitality = 0.0
    private var pkList: ArrayList<String> = ArrayList()
    private var lastOid: String? = ""
    var project: Project? = null
    var userlist: JList<String>? = null
    var userLabel: JLabel? = null
    var oChat: oChat? = null
    private var ws: ws? = null

    init {
        load()
        connect()
        val timer = timer("定时Thread_name", false, 2000, 1000) {
            if (ws?.isClosed == true) {
                oChat?.addInfoToOChat("ws", "连接已断开")
                connect()
                if (ws?.isClosed == false) {
                    oChat?.addInfoToOChat("ws", "连接已恢复")
                }
            }
        }
    }

    //加载持久数据
    private fun load() {
        PropertiesComponent.getInstance().getValue("pwl_cookie").let {
            if (it != null) {
                cookie = it
            }
        }
        PropertiesComponent.getInstance().getValue("pwl_userName").let {
            if (it != null) {
                userName = it
            }
        }
        PropertiesComponent.getInstance().getValue("pwl_password").let {
            if (it != null) {
                password = it
            }
        }
        PropertiesComponent.getInstance().getBoolean("pwl_eventLog").let {
            eventLog = it
        }
    }

    //数据持久化
    fun save() {
        PropertiesComponent.getInstance().setValue("pwl_cookie", cookie)
        PropertiesComponent.getInstance().setValue("pwl_userName", userName)
        PropertiesComponent.getInstance().setValue("pwl_password", password)
        PropertiesComponent.getInstance().setValue("pwl_eventLog", eventLog)
    }

    fun setOChatApi(o: oChat) {
        this.oChat = o
        this.oChat?.setClient(this)
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

    private fun get(url: String): Call? {
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
                        res.msg?.let { it1 -> oChat?.addErrToOChat("sendMsg", it1) }
                    }
                }
            } catch (e: Exception) {
                e.message?.let { oChat?.addErrToOChat("sendMsg", it) }
            }

            if (pkList.size > 0) {
                val selectedSeries = pkList.toMutableList()
                pkList.clear()
                selectedSeries.forEach {
                    openPacket(it)
                }
            }
        }
    }

    fun openPacket(oid: String) {
        post(PWL_OPEN, "{\"oId\":\"$oid\"}")?.execute().use { rs ->
            val res = Gson().fromJson(rs?.body()?.string(), RedPack::class.java)
            res.who?.forEach { r ->
                if (r.userName == userName) {
                    oChat?.addInfoToOChat("openPacket", "你抢到了${res.info?.userName}的红包,${r.userMoney}$")
                }
            }
        }
    }

    //撤回消息
    fun revoke() {
        delete("$PWL_REVOKE$lastOid")?.execute().use {
            val msg = Gson().fromJson(it?.body()?.string(), Msg::class.java)
            if (msg.code != 0) {
                msg.msg?.let { it1 -> oChat?.addErrToOChat("revoke", it1) }
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

    fun exit() {
        cookie = ""
        userName = ""
        password = ""
        isLogin = false
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
            sendNotify("上传提示", "文件上传中", NotificationType.INFORMATION)
            client.newCall(request).execute().use {
                val msg = Gson().fromJson(it.body()?.string(), Upload::class.java)
                return msg.data?.succMap?.get(file.name)
            }
        } catch (e: Exception) {
            e.message?.let { oChat?.addErrToOChat("upload", it) }
        }
        return ""
    }
    fun more(page:Int){
        get(PWL_MORE+"?page=$page&_=${System.currentTimeMillis().toString()}")?.execute().use {
            val msg = Gson().fromJson(it?.body()?.string(), More::class.java)
            var a=msg.data?.sortedBy { m->m.time }
            a?.forEach { m->
                m.type="msg"
                onMessage(Gson().toJson(m))
            }
        }
    }
    /**
     * WebSocket实现区
     */
    private fun connect() {
        ws = ws()
        ws?.client = this
        ws?.connect()
    }

    fun onMessage(message: String) {
        //logger.info("received message: $message")
        val msg =
            Gson().fromJson(message, Msg::class.java)
        when (msg.type) {
            "msg" -> {
                if (msg.content.indexOf("\"msgType\":\"redPacket\"") > 0) {
                    //红包消息记录oid
                    msg.oId?.let {
                        if (pkList.size > 100) {
                            pkList.removeAt(0)
                        }
                        pkList.add(it)
                    }
                } else {
                    if (msg.userName == userName) {
                        lastOid = msg.oId
                    }
                    if(eventLog){
                        var a:String?=""
                        while (a?.length!! <(msg?.content?.length?.div(4)!!)){
                            a+=" "
                        }
                        sendNotify(msg.userName!!, msg.content!!+"    "+a, NotificationType.INFORMATION)
                    }
                }
            }
            "online" -> {
                online = msg.onlineChatCnt
                var users=msg.users?.sortedBy { it.userName }
                val array = users?.size?.let { arrayOfNulls<String>(it) }
                users?.size.let {
                    if (it != null) {
                        for (i in 0 until it) {
                            array?.set(i, users?.get(i)?.userName)
                        }
                    }
                }
                userlist?.setListData(array)
                userLabel?.text = "Online ${msg.users?.size}"
            }
        }
        //给到绘制消息方法
        oChat?.addMsgToOChat(msg)
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
        private const val PWL_LOGIN = "https://pwl.icu/login"
        private const val PWL_LIVE = "https://pwl.icu/user/liveness"
        private const val PWL_SEND = "https://pwl.icu/chat-room/send"
        private const val PWL_OPEN = "https://pwl.icu/chat-room/red-packet/open"
        private const val PWL_REVOKE = "https://pwl.icu/chat-room/revoke/"
        private const val PWL_UPLOAD = "https://pwl.icu/upload"
        private const val PWL_MORE="https://pwl.icu/chat-room/more"
        private val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
        private val logger: Logger = LoggerFactory.getLogger(Client::class.java)
    }

}