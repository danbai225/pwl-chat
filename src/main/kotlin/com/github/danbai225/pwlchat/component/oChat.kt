package com.github.danbai225.pwlchat.component

import com.github.danbai225.pwlchat.pj.Msg

interface oChat {
    fun addMsgToOChat(msg: Msg)
    fun addErrToOChat(op: String, msg: String)
    fun addInfoToOChat(op: String, msg: String)
}