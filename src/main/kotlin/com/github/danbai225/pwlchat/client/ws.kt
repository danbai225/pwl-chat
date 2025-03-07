package com.github.danbai225.pwlchat.client

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.ByteBuffer

class Ws(c:Client,apikey:String) : WebSocketClient(URI.create(apikey)) {
    var client: Client? = c

    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.info("new connection opened")
    }

    override fun onMessage(message: String?) {
        message?.let { client?.onMessage(it) }
    }

    override fun onMessage(message: ByteBuffer) {
        logger.info("received ByteBuffer")
    }

    override fun onError(ex: Exception) {
        logger.error("an error occurred:$ex")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        logger.info("closed with exit code $code additional info: $reason")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Client::class.java)
    }
}