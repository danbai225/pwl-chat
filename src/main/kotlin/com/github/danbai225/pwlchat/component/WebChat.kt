package com.github.danbai225.pwlchat.component

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.component.js.jq
import com.github.danbai225.pwlchat.handler.PwlCefResourceRequestHandler
import com.github.danbai225.pwlchat.handler.PwlContextMenuHandler
import com.github.danbai225.pwlchat.pj.Msg
import com.github.danbai225.pwlchat.pj.RedPack
import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


class WebChat : JBCefBrowser(), oChat {
    private var loadHistory:Boolean=false
    var clientApi:Client?=null
    var init=false
    init {
        //初始化加载内容
        loadHTML(CSS + JS + HTML)
        //请求头处理
        jbCefClient.addRequestHandler(PwlCefResourceRequestHandler(), cefBrowser)
        //右键菜单
        jbCefClient.addContextMenuHandler(PwlContextMenuHandler(this),cefBrowser)
        //js
        callBackJs()
    }
    fun loadChatPage(){
        loadHTML(CSS + JS + HTML)
    }
    //添加js回调
    private fun callBackJs(){
        //打开红包

        val openRedPacket=JBCefJSQuery.create(this as JBCefBrowserBase)

        openRedPacket.addHandler { args: String ->
            clientApi?.openPacket(args)
            JBCefJSQuery.Response("ok")
        }
        jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                //onLoadEnd 在加载iframe也会触发
                //初次加载
                if(browser.url.contains("about:blank")&&!init){
                    clientApi?.more(1)
                    browser.executeJavaScript(
                        "window.openRedPacket = function(arg) {${openRedPacket.inject(
                            "arg",
                            "response => console.log('调用成功', response)",
                            "(error_code, error_message) => console.log('调用失败', error_code, error_message)"
                        )}};",
                        "https://fishpi.cn/js/openRedPacket.js", 0
                    )
                    init=true
                }
            }
        }, cefBrowser)
    }
    //通过js加载添加html到web

    private fun addHtml(html: String) {
        val html2 = Base64.getEncoder().encodeToString(html.toByteArray())
        cefBrowser.executeJavaScript(
            """$("#chat").append(decodeURIComponent(escape(window.atob('$html2')))); if ((${'$'}("#chat").height()-document.body.clientHeight-${'$'}(this).scrollTop())<333){window.scrollTo(0, ${'$'}("#chat").height())};var chats=${'$'}("#chat").children();if(chats.length>200){chats[0].remove();}""",
            "https://fishpi.cn/js/main.js",
            0)
    }

    //消息类型分析组成添加到web
    override fun addMsgToOChat(msg: Msg) {
        when (msg.type) {
            "msg" -> {
                addHtml(msgString(msg))
            }   //抢红包消息
            "redPacketStatus" -> {
                PropertiesComponent.getInstance().getBoolean("pwl_openMsg").let {
                    if (!it){
                        val html = msg.whoGive?.let { msg.whoGot?.let { it1 -> openRedPacketString(it1, it,msg.count,msg.got) } }
                        html?.let { addHtml(it) }
                    }
                }
            }
        }
    }

    override fun addErrToOChat(op: String, msg: String) {
        addHtml(String.format(ErrFmt,"$op-$msg"))
    }

    override fun addInfoToOChat(op: String, msg: String) {
        addHtml(String.format(InfoFmt,"$op-${msg.replace("\n","<br>")}"))
    }

    override fun setClient(client: Client) {
        this.clientApi=client
    }

    override fun loadHistory(boolean: Boolean): Boolean {
        if (boolean){
            loadHistory=true
        }
        return loadHistory
    }

    override fun clear() {
        loadChatPage()
    }

    override fun close() {
        dispose()
    }

    //消息主体
    private fun msgString(msg:Msg): String {
        val self= clientApi?.userName == msg.userName
        return String.format(
            MsgFmt, if(self)"chats__item--me" else "", msg.userAvatarURL, selfMsg1(msg.userName), contentMsgString(msg),
            msg.time
        )
    }
    //内容消息
    private fun contentMsgString(msg: Msg): String {
        if (msg.content.indexOf("\"msgType\":\"redPacket\"") > 0) {
            val red =
                Gson().fromJson(msg.content, RedPack::class.java)
           return String.format(RedPacketFmt,msg.oId,red.msg)
        }
        return String.format(ChatMSGFmt,msg.content)
    }
    //红包打开消息
    private fun openRedPacketString(u1: String,u2: String, count: Int,got :Int): String {
        return String.format(OpenRedPacketFmt, u1, u2, got,count)
    }
    //自己发的消息额外补充
    private fun selfMsg1(username: String): String {
        if (username != clientApi?.userName) {
            return String.format(SelfMsgFmt1, username)
        }
        return ""
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WebChat::class.java)
        private const val HTML = """
           <svg style="height: 0">
    <symbol id="redPacketIcon" viewBox="0 0 1024 1024"><path d="M705.2 445.28C689.12 536.48 608.608 606.256 512 606.256c-91.232 0-171.728-64.4-187.84-150.272l-134.16-80.496V783.36c0 59.04 48.304 101.968 101.968 101.968h440.064c59.04 0 101.968-48.288 101.968-101.968V370.128l-128.8 75.136zM512 219.856c91.232 0 166.368 64.4 187.84 150.256l134.16-85.856v-48.304c0-59.04-48.304-101.968-101.968-101.968H291.968c-53.664 0-101.968 42.928-101.968 101.968v59.04l134.16 80.48c16.112-91.216 96.608-155.616 187.84-155.616z" fill="#FF705A"></path><path d="M565.664 434.528h-26.832v-21.456h26.832c16.112 0 26.832-10.736 26.832-26.832 0-16.112-10.72-26.848-26.832-26.848h-16.096l32.208-32.192c10.72-10.72 10.72-26.832 0-37.568-10.736-10.72-26.848-10.72-37.568 0L512 327.2l-32.192-37.568c-10.736-10.72-26.848-10.72-37.568 0-10.736 10.72-10.736 26.832 0 37.568l32.192 32.192h-16.096c-16.096 0-26.832 10.736-26.832 26.848 0 16.096 10.72 26.832 26.832 26.832h32.192v21.456h-32.192c-16.096 0-26.832 10.736-26.832 26.832 0 16.112 10.72 26.848 26.832 26.848h32.192v37.568c0 16.096 10.736 26.816 26.848 26.816 16.096 0 26.832-10.72 26.832-26.816v-37.568h21.456c16.112 0 26.832-10.736 26.832-26.848 0-16.096-10.72-26.832-26.832-26.832z" fill="#FF705A" opacity=".4"></path></symbol>
</svg>
<div id="chat">
</div>
        """
        private  val JS = """
            <script>${jq}</script>
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

    p {
        margin: 0;
    }

    svg {
        fill: currentColor;
        display: inline-block;
        stroke-width: 0;
        stroke: currentColor;
        width: 14px;
        height: 14px;
    }

    .hongbao__item {

        color: #3A3A59;
        cursor: pointer;
        padding: 5px;
        border-radius: 3px;
        box-shadow: 0 1px 2px;
        margin: 3px 5px 8px 3px;
    }

    .fn__flex-inline {
        display: inline-flex;
        align-items: center;
    }

    .hongbao__icon {
        height: 34px !important;
        width: 34px !important;
        margin-right: 5px;
    }
    .chats__content .vditor-reset img:not(.emoji) {
        max-width: 150px;
        max-height: 300px;
    }
    .blur img {
        -webkit-filter: blur(20px) saturate(1.4);
        filter: blur(20px) saturate(1.4);
        transition: all .15s ease-in-out;
    }
    .blur img:hover {
        filter: none;
        -webkit-filter: none;
    }
    .hongbao__item:hover {
        box-shadow: 0 0 3px rgb(0 0 0 / 13%), 0 3px 6px rgb(0 0 0 / 26%);
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
            %s
            <div class="ft__smaller ft__fade fn__right">
               %s
            </div>
        </div>
    </div>
</div>"""
        private const val ChatMSGFmt="""<div style="margin-top: 4px" class="vditor-reset ft__smaller  blur">%s</div>"""
        private const val SelfMsgFmt1 =
            """<div class="ft__fade ft__smaller" style="padding-bottom: 3px;border-bottom: 1px solid #263859"><span class="ft-gray">%s</span></div>"""
        private const val RedPacketFmt="""
                        <div style="margin-top: 4px" class="vditor-reset ft__smaller  blur">
                <div class="hongbao__item fn__flex-inline" onclick="$(this).attr('onclick','');openRedPacket('%s')">
                    <svg class="ft__red hongbao__icon">
                        <use xlink:href="#redPacketIcon"></use>
                    </svg>
                    <div>
                        <div>%s</div>
                        <div class="ft__smaller ft__fade redPacketDesc">
                        </div>
                    </div>
                </div>
            </div>
        """
        private const val OpenRedPacketFmt="""
            <div style="color: #FF7777;margin-bottom: 10px;text-align: center;">
    <svg>
        <use xlink:href="#redPacketIcon"></use>
    </svg>
    <span>&nbsp;%s 抢到了 %s 的 红包 (%d/%d)</span>
</div>
        """
        private const val InfoFmt="""<div style="color: #EEE;margin-bottom: 10px;text-align: center;"><span>%s</span></div>"""
        private const val ErrFmt="""<div style="color: #FA4659;margin-bottom: 10px;text-align: center;"><span>%s</span></div>"""
    }
}