package com.github.danbai225.pwlchat.pj


class RedPack {
    class Who {
        var userMoney = 0
        var time: String? = ""
        var avatar: String? = ""
        var userName: String? = ""
        var userId: String? = ""
    }

    class Info {
        var msg: String? = ""
        var userAvatarURL: String? = ""
        var count = 0
        var userName: String? = ""
        var got = 0
    }

    var msg: String? = ""
    var senderId: String? = ""
    var msgType: String? = ""
    var money = 0
    var count = 0
    var got = 0
    var who: List<Who>? = null
    var info: Info? = Info()
}