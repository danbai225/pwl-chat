package com.github.danbai225.pwlchat.ui

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.notify.sendNotify
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.ImageUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*


class ToolWindowContent(p: Project?) {
    var root: JPanel? = null
    var send: JButton? = null
    var iChat: JTextArea? = null
    var oChat: JTextPane? = null
    var client: Client? = Client()
    var consoleScroll: JScrollPane? = null
    var userLabel: JLabel? = null
    var c:JPanel?=null
    var userlist:JList<String>?=null
    private var userListModel: DefaultListModel<String>? = null
    private var  project: Project?= p
    private var history: ArrayDeque<String> = ArrayDeque(4)
    private fun sendNotify(title: String, content: String, type: NotificationType) {
        project?.let { sendNotify(it, title, content, type) }
    }
    init {
        userListModel = DefaultListModel()
        for (i in 0..9) {
            userListModel!!.addElement(i.toString())
        }
        userlist?.model = userListModel

        even()

        client?.project=project
        client?.oChat = oChat
        client?.consoleScroll = consoleScroll
        client?.userListModel = userListModel
        client?.userLabel = userLabel
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
                oChat?.text += "帮助命令：命令都是以#开头 参数用空格分割\n#help - 输出本帮助命令\n#packet - 发送红包，参数1(个数) 参数2(总额) 参数3(消息)\n#revoke - 撤回最后一条发送的消息\n#exit - 退出登陆\n#eventLog - 在事件中输出聊天 参数1(是否开启1 or 0)\n"
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
                client?.save()
                return
            }
        }
        msg?.let { client?.sendMsg(it) }
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
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val transferable = clipboard.getContents(null)
                    var url:String?=""
                    try {
                        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            val fileList = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                            url = client?.upload(fileList[0])
                            iChat?.text=iChat?.text?.replace(fileList[0].name,"")
                        } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                            val image = transferable.getTransferData(DataFlavor.imageFlavor) as Image
                                ByteArrayOutputStream().use { out ->
                                    val bufferedImage: BufferedImage = ImageUtil.toBufferedImage(image)
                                    ImageIO.write(bufferedImage, "jpg", out)
                                    var file = File(System.currentTimeMillis().toString() + "pwl-chat.jpg")
                                    file.writeBytes(out.toByteArray())
                                    url = client?.upload(file)
                                }
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                    if (!url?.isEmpty()!!){
                        iChat?.text+="![image.png]($url)"
                    }
                }
            }
        })

        userlist?.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val list = e.source as JList<*>
                    val index = list.selectedIndex //已选项的下标
                    val obj = list.model.getElementAt(index) //取出数据
                    iChat?.text+="@${obj} :"
                    iChat?.requestFocusInWindow()
                }
            }
        })
    }
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ToolWindowContent::class.java)
    }
}
