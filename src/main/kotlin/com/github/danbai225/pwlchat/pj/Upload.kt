package com.github.danbai225.pwlchat.pj

class Upload {
    var msg: String? = null
    var code = 0
    var data: Data? = null

    class Data {
        var errFiles: List<String>? = null
        var succMap: Map<String, String>? = null
    }
}
