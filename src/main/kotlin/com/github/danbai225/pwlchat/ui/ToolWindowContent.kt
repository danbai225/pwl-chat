package com.github.danbai225.pwlchat.ui

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.component.TextChat
import com.github.danbai225.pwlchat.component.WebChat
import com.github.danbai225.pwlchat.component.oChat
import com.github.danbai225.pwlchat.notify.sendNotify
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.ImageUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.*
import javax.imageio.ImageIO
import javax.swing.*


class ToolWindowContent(p: Project?) : JPanel() {
    var root: JPanel? = null
    var send: JButton? = null
    var iChat: JTextArea? = null
    var oChat: oChat? = null
    var oChatPanel: JPanel? = null
    var client: Client? = Client()
    var userLabel: JLabel? = null
    var draw: JPanel? = null
    var userlist: JList<String>? = null
    private var project: Project? = p
    private var history: ArrayDeque<String> = ArrayDeque(4)
    private var WebView: Boolean = false
    private fun sendNotify(title: String, content: String, type: NotificationType) {
        project?.let { sendNotify(it, title, content, type) }
    }

    init {
        //组件初始化
        component()
        //注册事件
        even()
    }

    fun getContent(): JComponent? {
        return this
    }

    fun sendMsg() {
        if (!client?.isLogin!!) {
            client?.userName = Messages.showInputDialog("Username", "鱼油登录", Messages.getInformationIcon())
            client?.password = Messages.showPasswordDialog("Password", "鱼油登录")
            if (client?.login() == true) {
                send?.text = "send"
            } else {
                Messages.showMessageDialog(":(验证未通过", "MSG", Messages.getInformationIcon())
                return
            }
        }
        val msg = iChat?.text
        if (history.size >= 20) {
            history.removeFirst()
        }
        msg?.let { history.addLast(it) }
        iChat?.text = ""
        val split = msg?.split(" ")
        //命令解析
        when (split?.get(0)?.toLowerCase()) {
            "#help", "#帮助" -> {
                oChat?.addInfoToOChat(
                    "help",
                    "帮助命令：命令都是以#开头 参数用空格分割\n#help - 输出本帮助命令\n#packet - 发送红包，参数1(个数) 参数2(总额) 参数3(消息)\n#revoke - 撤回最后一条发送的消息\n#exit - 退出登陆\n#eventLog - 在事件中输出聊天 参数1(是否开启1 or 0)\n" +
                            "#web - 切换输出模式 参数1(开启) 0 or 1"
                )
                return
            }
            "#packet", "#红包" -> {
                if (split.size<4){
                    oChat?.addInfoToOChat("commandLineInfo","参数有误请输入#help查看帮助")
                    return
                }
                client?.packet(split[1].toInt(), split[2].toInt(), split[3])
                return
            }
            "#revoke", "#撤回" -> {
                client?.revoke()
                return
            }
            "#exit", "#退出" -> {
                client?.exit()
                send?.text = "Login"
                return
            }
            "#eventlog", "#事件输出" -> {
                if (split.size<2){
                    oChat?.addInfoToOChat("commandLineInfo","参数有误请输入#help查看帮助")
                    return
                }
                client?.eventLog = split[1].toInt() == 1
                client?.save()
                return
            }
            "#web" -> {
                if (split.size<2){
                    oChat?.addInfoToOChat("commandLineInfo","参数有误请输入#help查看帮助")
                    return
                }
                PropertiesComponent.getInstance().setValue("pwl_web", split[1].toInt() == 1)
                if ((split[1].toInt() == 1) != WebView) {
                    WebView = (split[1].toInt() == 1)
                    var chat: oChat = if (WebView) {
                        WebChat()
                    } else {
                        TextChat()
                    }
                    oChat = chat
                    oChatPanel?.removeAll()
                    oChatPanel?.add(chat.getComponent(),BorderLayout.CENTER)
                    client?.setOChatApi(oChat!!)
                }
                return
            }
        }
        msg?.let { client?.sendMsg(it) }
    }
    private fun component() {
        //自身属性
        layout = BorderLayout()
        add(root, BorderLayout.CENTER)
        //聊天渲染
        PropertiesComponent.getInstance().getBoolean("pwl_web").let {
            WebView = it
        }
        var chat: oChat = if (WebView) {
            WebChat()
        } else {
            TextChat()
        }
        oChat = chat
        oChatPanel?.removeAll()
        oChatPanel?.add(chat.getComponent(),BorderLayout.CENTER)
        client?.setOChatApi(oChat!!)
        //pwl客户端
        client?.project = project
        client?.setOChatApi(oChat!!)
        client?.userLabel = userLabel
        client?.userlist=userlist
        if (client?.verifyLogin() == true) {
            send?.text = "send"
        }
        //其他
//        var jbCefBrowser = JBCefBrowser("https://pwl.icu")
//        draw?.add(jbCefBrowser.component)
    }

    private fun even() {
        //发送按钮
        send?.addActionListener {
            sendMsg()
        }
        //输入框中的事件
        iChat!!.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    //回车 发送
                    KeyEvent.VK_ENTER -> {
                        e.consume()
                        sendMsg()
                    }
                    //方向键上 向上翻滚发送记录
                    KeyEvent.VK_DOWN -> {
                        if (history.size > 0) {
                            iChat?.text = history.first()
                            history.addLast(history.first())
                            history.removeFirst()
                        }
                    }
                    //方向键下 向下翻滚发送记录
                    KeyEvent.VK_UP -> {
                        if (history.size > 0) {
                            iChat?.text = history.last()
                            history.addFirst(history.last())
                            history.removeLast()
                        }
                    }
                }
            }

            override fun keyReleased(e: KeyEvent) {
                //粘贴快捷键
                if ((e.isControlDown || e.isMetaDown) && e.keyCode == KeyEvent.VK_V) {
                    if (client?.isLogin == false) {
                        return
                    }
                    // 粘贴图片
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val transferable = clipboard.getContents(null)
                    var url: String? = ""
                    try {
                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            val fileList = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                            url = client?.upload(fileList[0])
                            iChat?.text = iChat?.text?.replace(fileList[0].name, "")
                        } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                            val image = transferable.getTransferData(DataFlavor.imageFlavor) as Image
                            val temp = File.createTempFile(System.currentTimeMillis().toString(), ".png")
                            temp.deleteOnExit()
                            var out = FileOutputStream(temp)
                            ImageIO.write(ImageUtil.toBufferedImage(image), "png", out)
                            out.close()
                            logger.info(temp.length().toString())
                            url = client?.upload(temp)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                    if (!url?.isEmpty()!!) {
                        iChat?.text += "![image.png]($url)"
                    }
                }
            }
        })
        //聊天列表双击@
        userlist?.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val list = e.source as JList<*>
                    val index = list.selectedIndex //已选项的下标
                    if (index >= 0 && index < list.model.size) {
                        val obj = list.model.getElementAt(index) //取出数据
                        iChat?.text += "@${obj} :"
                        iChat?.requestFocusInWindow()
                    }
                }
            }
        })
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ToolWindowContent::class.java)
    }
}
