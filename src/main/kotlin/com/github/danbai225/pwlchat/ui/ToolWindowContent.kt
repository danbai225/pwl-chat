package com.github.danbai225.pwlchat.ui

import com.github.danbai225.pwlchat.client.Client
import com.intellij.openapi.ui.Messages
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.File
import java.lang.Exception
import javax.swing.*


class ToolWindowContent{
    var root: JPanel?=null
    var send: JButton?=null
    var iChat:JTextArea?=null
    var oChat:JTextPane?=null
    var client: Client?=Client()
    var consoleScroll:JScrollPane?=null
    init {
        even()
        client?.oChat=oChat
        client?.consoleScroll=consoleScroll
        client?.connect()
        if (client?.verifyLogin()==true){
            send?.text="send"
        }
       // BorderLayout.WEST
    }
    fun getContent(): JComponent? {
        return root
    }
    fun sendMsg(){
        iChat?.text?.let { it1 -> client?.sendMsg(it1) }
        iChat?.text=""
    }
    private fun even(){
        send?.addActionListener {
            if (!client?.islogin!!){
                client?.userName=Messages.showInputDialog("Username", "鱼油登录", Messages.getInformationIcon())
                client?.password=Messages.showPasswordDialog("Password", "鱼油登录")
                if (client?.login() == true){
                    send?.text="send"
                }else{
                    Messages.showMessageDialog("验证未通过","MSG",Messages.getInformationIcon())
                }
            }else{
                sendMsg()
            }
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
                    if (client?.islogin==false) {
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
