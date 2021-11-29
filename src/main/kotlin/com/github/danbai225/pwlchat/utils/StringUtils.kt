package com.github.danbai225.pwlchat.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object StringUtils {
    fun md5(input: String?): String? {
        if (input == null || input.isEmpty()) {
            return null
        }
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(input.toByteArray())
            val byteArray = md5.digest()
            val sb = StringBuilder()
            for (b in byteArray) {
                // 一个byte格式化成两位的16进制，不足两位高位补零
                sb.append(String.format("%02x", b))
            }
            return sb.toString().replace("\\s".toRegex(), "").toLowerCase()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }
}