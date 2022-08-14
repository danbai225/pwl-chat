package com.github.danbai225.pwlchat.client

import com.github.danbai225.pwlchat.component.oChat
import com.github.danbai225.pwlchat.pj.*
import com.github.danbai225.pwlchat.utils.StringUtils
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.use
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JProgressBar
import kotlin.concurrent.timer

class Client {
    /**
     * 基础属性和方法
     */
    private var apiKey:String=""
    private var online: Int? = 0
    private val client = OkHttpClient()
    var userName: String? = ""
    var password: String? = ""
    var mfaCode:String?=""
    var isLogin: Boolean = false
    var eventLog: Boolean = false
    private var onlineVitality = 0.0
    private var pkList: ArrayList<String> = ArrayList()
    private var lastOid: String? = ""
    var project: Project? = null
    var userlist: JList<String>? = null
    var userLabel: JLabel? = null
    var hot : JProgressBar?=null
    var oChat: oChat? = null
    var black:Boolean=false
    private var ws: Ws? = null

    init {
        load()
        verifyLogin()
        if (isLogin){
            connect()
        }
        timer("定时Thread_name", false, 2000, 1000) {
            if (isLogin){
                if (ws?.isClosed == true&&!black) {
                    oChat?.addInfoToOChat("ws", "连接已断开")
                    connect()
                    if (ws?.isClosed == false) {
                        oChat?.addInfoToOChat("ws", "连接已恢复")
                    }
                }
            }
        }
    }

    //加载持久数据
    private fun load() {
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
        PropertiesComponent.getInstance().getValue("pwl_apiKey").let {
            if (it != null) {
                apiKey = it
            }
        }
    }

    //数据持久化
    fun save() {
        PropertiesComponent.getInstance().setValue("pwl_userName", userName)
        PropertiesComponent.getInstance().setValue("pwl_password", password)
        PropertiesComponent.getInstance().setValue("pwl_eventLog", eventLog)
        PropertiesComponent.getInstance().setValue("pwl_apiKey", apiKey)
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
            .url(if (url.indexOf("?",0,false)>0) "$url&apiKey=$apiKey" else "$url?apiKey=$apiKey")
            .post(requestBody)
            .build()
        return client.newCall(request)
    }

    private fun delete(url: String): Call? {
        val request: Request = Request.Builder()
            .url(if (url.indexOf("?",0,false)>0) "$url&apiKey=$apiKey" else "$url?apiKey=$apiKey")
            .delete()
            .build()
        return client.newCall(request)
    }

    private fun get(url: String): Call? {
        val request: Request = Request.Builder()
            .url(if (url.indexOf("?",0,false)>0) "$url&apiKey=$apiKey" else "$url?apiKey=$apiKey")
            .get()
            .build()
        return client.newCall(request)
    }

    /**
     * 客户端接口区
     */
    //登陆有效性验证
    fun verifyLogin(): Boolean {
        if (apiKey != "") {
            val get = get(PWL_LIVE)
            get?.execute().use { response ->
                if (response?.code() == 200) {
                    val msg = Gson().fromJson(response.body()?.string(), Liveness::class.java)
                    onlineVitality = msg.liveness
                    hot?.value= onlineVitality.toInt()
                    isLogin = true
                    yesterdayReward()
                    return true
                }
            }
        }
        isLogin = false
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
    @OptIn(DelicateCoroutinesApi::class)
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
    //用户名联想
    fun names(n: String):List<String> {
        val arrayList = ArrayList<String>()
        post(PWL_NAMES,"{\"name\": \"$n\"}")?.execute().use {
            val msg = Gson().fromJson(it?.body()?.string(), Names::class.java)
            if (msg.code != 0) {
                msg.msg.let { it1 -> oChat?.addErrToOChat("names", it1) }
            }
            msg.data?.forEach { item->arrayList.add(item.userName) }
        }
        return arrayList
    }
    private fun yesterdayReward(){
        get(PWL_YESTERDAY)?.execute()
    }
    //登陆
    fun login(): Boolean {
        val md5p = password?.let { StringUtils.md5(it) }
        val call = post(
            PWL_API_Key,
            "{\"nameOrEmail\":\"$userName\",\"userPassword\":\"$md5p\",\"rememberLogin\":true,\"mfaCode\":\"$mfaCode\"}"
        )
        call?.execute().use { response ->
            val msg = Gson().fromJson(response?.body()?.string(), loginInfo::class.java)
            if (msg.code == 0) {
                apiKey = msg.Key
                if (verifyLogin()) {
                    isLogin = true
                    save()
                    connect()
                    return true
                }
            }
        }//teyxBFF7JjkXHv
        return false
    }

    fun exit() {
        apiKey = ""
        userName = ""
        password = ""
        isLogin = false
        save()
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
            .url("$PWL_UPLOAD?apiKey=$apiKey")
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
        get(PWL_MORE+"?page=$page&_=${System.currentTimeMillis()}")?.execute().use {
            val string = it?.body()?.string()
            try {
                val msg = Gson().fromJson(string, More::class.java)
                val a=msg.data?.sortedBy { m->m.time }
                a?.forEach { m->
                    m.type="msg"
                    onMessage(Gson().toJson(m))
                }
            }catch (e: Exception){
                string?.let { it1 -> oChat?.addErrToOChat("接口错误", it1) }
                black=true
                return
            }
        }
    }

    /**
     * WebSocket实现区
     */
    private fun connect() {
        if (isLogin){
            ws = Ws(this,apiKey)
            ws?.connect()
        }
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
                        sendNotify(msg.userName, msg.content +"    "+a, NotificationType.INFORMATION)
                    }
                }
            }
            "online" -> {
                online = msg.onlineChatCnt
                val users=msg.users?.sortedBy { it.userName }
                val array = users?.size?.let { arrayOfNulls<String>(it) }
                users?.size.let {
                    if (it != null) {
                        for (i in 0 until it) {
                            array?.set(i, users.get(i).userName)
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
        const val PWL_WSS = "wss://fishpi.cn/chat-room-channel"
        private const val PWL_LIVE = "https://fishpi.cn/user/liveness"
        private const val PWL_SEND = "https://fishpi.cn/chat-room/send"
        private const val PWL_OPEN = "https://fishpi.cn/chat-room/red-packet/open"
        private const val PWL_REVOKE = "https://fishpi.cn/chat-room/revoke/"
        private const val PWL_UPLOAD = "https://fishpi.cn/upload"
        private const val PWL_MORE="https://fishpi.cn/chat-room/more"
        private const val PWL_NAMES="https://fishpi.cn/users/names"
        private const val PWL_YESTERDAY="https://fishpi.cn/yesterday-liveness-reward-api"
        private const val PWL_API_Key="https://fishpi.cn/api/getKey"
        private val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
        private val logger: Logger = LoggerFactory.getLogger(Client::class.java)
    }

}