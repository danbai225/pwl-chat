package com.github.danbai225.pwlchat.component

import com.github.danbai225.pwlchat.client.Client
import com.github.danbai225.pwlchat.pj.Msg
import javax.swing.JComponent

interface oChat {
    fun addMsgToOChat(msg: Msg)
    fun addErrToOChat(op: String, msg: String)
    fun addInfoToOChat(op: String, msg: String)
    fun getComponent(): JComponent
    fun setClient(client: Client)
    fun loadHistory(boolean: Boolean) :Boolean
    fun clear()
    fun close()
}