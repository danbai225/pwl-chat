package com.github.danbai225.pwlchat.ui

import com.github.danbai225.pwlchat.client.Client
import com.intellij.openapi.ui.Messages
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*


class ToolWindowContent {
    var root: JPanel? = null
    var send: JButton? = null
    var iChat: JTextArea? = null
    var oChat: JTextPane? = null
    var client: Client? = Client()
    var consoleScroll: JScrollPane? = null

    init {
        even()
        client?.oChat = oChat
        client?.consoleScroll = consoleScroll
        client?.connect()
        if (client?.verifyLogin() == true) {
            send?.text = "send"
        }
        // BorderLayout.WEST
    }

    fun getContent(): JComponent? {
        return root
    }

    fun sendMsg() {
        if (!client?.islogin!!) {
            client?.userName = Messages.showInputDialog("Username", "鱼油登录", Messages.getInformationIcon())
            client?.password = Messages.showPasswordDialog("Password", "鱼油登录")
            if (client?.login() == true) {
                send?.text = "send"
            } else {
                Messages.showMessageDialog("验证未通过", "MSG", Messages.getInformationIcon())
                return
            }
        }
        var msg = iChat?.text
        iChat?.text = ""
        var split = msg?.split(" ")
        //命令解析
        when (split?.get(0)?.toLowerCase()) {
            "#help" -> {
                oChat?.text += "帮助命令：命令都是以#开头 参数用空格分割\n#help - 输出本帮助命令\n#packet - 发送红包，参数1(个数) 参数2(总额) 参数3(消息)\n"
                return
            }
            "#packet" -> {
                client?.packet(split?.get(1)?.toInt(), split[2].toInt(), split[3])
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
                if (KeyEvent.VK_ENTER == e.keyCode) {
                    // 阻止默认事件
                    e.consume()
                    sendMsg()
                }
            }

            override fun keyReleased(e: KeyEvent) {
                if ((e.isControlDown || e.isMetaDown) && e.keyCode == KeyEvent.VK_V) {
                    if (client?.islogin == false) {
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
