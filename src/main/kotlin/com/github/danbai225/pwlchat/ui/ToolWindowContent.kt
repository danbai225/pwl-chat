package com.github.danbai225.pwlchat.ui

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.notify.sendNotify
import com.intellij.notification.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*


class ToolWindowContent(p: Project?) {
    var root: JPanel? = null
    var send: JButton? = null
    var iChat: JTextArea? = null
    var oChat: JTextPane? = null
    var client: Client? = Client()
    var consoleScroll: JScrollPane? = null
    private var  project: Project?= p
    private var history: ArrayDeque<String> = ArrayDeque(4)
    private fun sendNotify(title: String, content: String, type: NotificationType) {
        project?.let { sendNotify(it, title, content, type) }
    }
    init {
        even()
        client?.project=project
        client?.oChat = oChat
        client?.consoleScroll = consoleScroll
        client?.connect()
        if (client?.verifyLogin() == true) {
            send?.text = "send"
        }
    }
    fun getContent(): JComponent? {
        return root
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
        if(history.size>10){
            history.removeFirst()
        }
        msg?.let { history.addLast(it) }
        iChat?.text = ""
        val split = msg?.split(" ")
        //命令解析
        when (split?.get(0)?.toLowerCase()) {
            "#help","#帮助" -> {
                oChat?.text += "帮助命令：命令都是以#开头 参数用空格分割\n#help - 输出本帮助命令\n#packet - 发送红包，参数1(个数) 参数2(总额) 参数3(消息)\n#revoke - 撤回最后一条发送的消息\n#exit - 退出登陆\n#eventLog - 在事件中输出聊天 参数1(是否开启1 or 0)"
                return
            }
            "#packet","#红包" -> {
                client?.packet(split[1].toInt(), split[2].toInt(), split[3])
                return
            }
            "#revoke","#撤回" -> {
                client?.revoke()
                return
            }
            "#exit","#退出" -> {
                client?.exit()
                send?.text="Login"
                return
            }
            "#eventlog","#事件输出" -> {
                client?.eventLog = split[1].toInt()==1
                return
            }
        }
       // msg?.let { client?.sendMsg(it) }
    }
    private fun even() {
        send?.addActionListener {
            sendMsg()
        }
        iChat!!.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when(e.keyCode){
                    KeyEvent.VK_ENTER->{
                        e.consume()
                        sendMsg()
                    }
                    KeyEvent.VK_DOWN->{
                        if(history.size>0){
                            iChat?.text=history.first()
                            history.addLast(history.first())
                            history.removeFirst()
                        }
                    }
                    KeyEvent.VK_UP->{
                        if(history.size>0){
                            iChat?.text=history.last()
                            history.addFirst(history.last())
                            history.removeLast()
                        }
                    }
                }
            }

            override fun keyReleased(e: KeyEvent) {
                if ((e.isControlDown || e.isMetaDown) && e.keyCode == KeyEvent.VK_V) {
                    if (client?.isLogin == false) {
                        return
                    }
                    // 粘贴图片
//                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
//                    val transferable = clipboard.getContents(null)
//                    try {
//                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//                            val fileList = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
//                            UploadUtils.uploadImageFile(fileList[0])
//                        } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
//                            val image = transferable.getTransferData(DataFlavor.imageFlavor) as Image
//                            UploadUtils.uploadImage(image)
//                        }
//                    } catch (exception: Exception) {
//                        exception.printStackTrace()
//                    }
                }
            }
        })
    }
}
