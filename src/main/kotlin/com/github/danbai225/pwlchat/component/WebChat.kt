package com.github.danbai225.pwlchat.component

import com.github.danbai225.pwlchat.handler.PwlCefResourceRequestHandler
import com.github.danbai225.pwlchat.pj.Msg
import com.intellij.ui.jcef.JBCefBrowser
import java.text.SimpleDateFormat
import java.util.*


class WebChat : JBCefBrowser(), oChat {
    private var content = CSS + JS + ""
    private var currentUserName = ""

    init {
        loadHTML(content)
        // API 6 - JBCefJSQuery JS 调用 Java 回调函数
        /*
        val myJSQuery = JBCefJSQuery.create(this)
        myJSQuery.addHandler { args: String ->
            println("JS 调用了 这个函数，参数是：$args")
            if ("null" == args) {
                return@addHandler JBCefJSQuery.Response(null, 1, "不允许 null")
            } else if ("undefined" == args) {
                return@addHandler JBCefJSQuery.Response(null)
            }
            JBCefJSQuery.Response("Java 的返回值")
        }
        jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                browser.executeJavaScript(
                    "window.JavaPanelBridge = {" +
                            "callJava : function(arg) {" +
                            myJSQuery.inject(
                                "JSON.stringify(arg)",
                                "response => console.log('callJava 成功', response)",
                                "(error_code, error_message) => console.log('callJava 失败', error_code, error_message)"
                            ) +
                            "}" +
                            "};" +
                            "setInterval(()=>{JavaPanelBridge.callJava(); JavaPanelBridge.callJava(null); JavaPanelBridge.callJava({a:1}); JavaPanelBridge.callJava(\"这是参数\");}, 5000)",
                    "https://rectcircle.cn/js/js-bridge.js", 0
                )
            }
        }, cefBrowser)
        */
        //请求头处理
        jbCefClient.addRequestHandler(PwlCefResourceRequestHandler(), cefBrowser)
        cefBrowser.devTools
    }

    private fun addHtml(html: String) {
        val html2 = Base64.getEncoder().encodeToString(html.toByteArray())
        cefBrowser.executeJavaScript(
            """$("body").append(decodeURIComponent(escape(window.atob('$html2')))); window.scrollTo(0, document.documentElement.clientHeight);""",
            "https://pwl.icu/js/main.js",
            0
        )
    }

    override fun setCurrentUserName(username: String) {
        currentUserName = username
    }

    override fun addMsgToOChat(msg: Msg) {
        when (msg.type) {
            "msg" -> {
                if (msg.content.indexOf("\"msgType\":\"redPacket\"") > 0) {
                } else {
                    val html = msg.userName?.let { msg.userAvatarURL?.let { it1 -> msgString(it, it1, msg.content) } }
                    html?.let { addHtml(it) }
                }
            }   //抢红包消息
            "redPacketStatus" -> {

            }
        }
    }

    override fun addErrToOChat(op: String, msg: String) {
        TODO("Not yet implemented")
    }

    override fun addInfoToOChat(op: String, msg: String) {
        TODO("Not yet implemented")
    }

    private fun msgString(userName: String, headUrl: String, content: String): String {
        return String.format(
            MsgFmt, if (currentUserName == userName) "chats__item--me" else "", headUrl, selfMsg1(userName), content,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        )
    }

    private fun selfMsg1(username: String): String {
        if (username != currentUserName) {
            return String.format(SelfMsgFmt1, username)
        }
        return ""
    }

    companion object {
        private const val JS = """
            <script src="https://cdn.bootcdn.net/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
        """
        private const val CSS = """<style>
    body {
        margin: 0;
        font-family: "Helvetica Neue", "Luxi Sans", "DejaVu Sans", Tahoma, "Hiragino Sans GB", "Microsoft Yahei", sans-serif;
        font-size: 14px;
        color: #263859;
        background-color: rgb(60, 63, 65);
    }

    .module {
        margin-bottom: 20px;
        border-radius: 3px;
        width: 100%;
        word-wrap: break-word;
        box-sizing: border-box;
        min-width: 1px;
    }

    .fn-flex, .fn__flex {
        display: flex;
    }

    .chats__content {
        background-color: #6B778D;
        border-radius: 5px;
        padding: 8px 15px;
        margin: 0 20px 15px 15px;
        overflow: initial;
        max-width: 85%;
        box-sizing: border-box;
    }

    .chats__arrow {
        position: relative;
    }

    .ft__fade {
        color: #263859;
    }

    .ft__smaller {
        font-size: 12px;
    }

    .vditor-reset {
        color: #24292e;
        font-variant-ligatures: no-common-ligatures;
        font-family: "Helvetica Neue", "Luxi Sans", "DejaVu Sans", Tahoma, "Hiragino Sans GB", "Microsoft Yahei", sans-serif;
        word-wrap: break-word;
        overflow: auto;
        line-height: 1.5;
        font-size: 16px;
        word-break: break-word;
    }

    .fn__right {
        float: right;
    }

    .fn__left {
        float: left;
    }

    .chats__item--me {
        flex-direction: row-reverse;
    }

    img.emoji {
        cursor: auto;
        max-width: 20px;
        vertical-align: sub;
    }

    .chats__item .avatar {
        margin-right: 0;
    }

    .avatar {
        height: 38px;
        width: 38px;
        margin-right: 10px;
    }

    .avatar, .avatar-small, .avatar-middle, .avatar-mid, .avatar-big {
        border-radius: 3px 3px 3px 3px;
        background-size: cover;
        background-repeat: no-repeat;
        background-position: center center;
        display: inline-block;
        vertical-align: middle;
        background-color: rgba(0, 0, 0, 0.02);
    }

    .chats__arrow:after {
        position: absolute;
        width: 0;
        height: 0;
        content: "";
        top: 5px;
        left: -25px;
        border: 5px solid transparent;
        border-right: 5px solid #24292e;
    }

    .chats__item--me .chats__content .chats__arrow:after {
        right: -25px;
        border-left-color: #24292e;
        left: auto;
        border-right-color: transparent;
    }
    p{
        margin: 0;
    }
</style>"""
        private const val MsgFmt = """
<div style="display: block;">
    <div class="fn__flex chats__item %s">
            <div class="avatar tooltipped__user"
                 style="background-image: url('%s');"></div>
        <div class="chats__content">
            <div class="chats__arrow"></div>
            %s
            <div style="margin-top: 4px" class="vditor-reset ft__smaller  blur">
                %s
            </div>
            <div class="ft__smaller ft__fade fn__right">
               %s
            </div>
        </div>
    </div>
</div>"""
        private const val SelfMsgFmt1 =
            """<div class="ft__fade ft__smaller" style="padding-bottom: 3px;border-bottom: 1px solid #263859"><span class="ft-gray">%s</span></div>"""
    }
}