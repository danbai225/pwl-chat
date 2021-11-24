package com.github.danbai225.pwlchat.pj

import com.github.danbai225.pwlchat.pj.RedPack.Who




class RedPack {
    class Who {
        var userMoney = 0
        var time: String? = null
        var avatar: String? = null
        var userName: String? = null
        var userId: String? = null
    }
    class Info {
        var msg: String? = null
        var userAvatarURL: String? = null
        var count = 0
        var userName: String? = null
        var got = 0
    }
    var msg: String? = null
    var senderId: String? = null
    var msgType: String? = null
    var money = 0
    var count = 0
    var got = 0
    var who: List<Who>? = null
    var info: Info? = null
}