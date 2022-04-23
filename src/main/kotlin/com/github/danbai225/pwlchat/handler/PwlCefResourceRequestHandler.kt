package com.github.danbai225.pwlchat.handler

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefCookieAccessFilter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.cef.network.CefURLRequest


class PwlCefResourceRequestHandler : CefRequestHandlerAdapter(), CefResourceRequestHandler {
    override fun getResourceRequestHandler(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        isNavigation: Boolean,
        isDownload: Boolean,
        requestInitiator: String?,
        disableDefaultHandling: BoolRef?
    ): CefResourceRequestHandler {
        return this
    }

    override fun getCookieAccessFilter(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest?): CefCookieAccessFilter? {
        return null
    }

    override fun onBeforeResourceLoad(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest?): Boolean {
        val headerMap: HashMap<String, String> = HashMap()
        p2?.getHeaderMap(headerMap)
        headerMap["referer"] = "https://fishpi.cn/"
        p2?.setHeaderMap(headerMap)
        return false
    }

    override fun getResourceHandler(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest?): CefResourceHandler? {
        return null
    }

    override fun onResourceRedirect(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest?, p3: CefResponse?, p4: StringRef?) {

    }

    override fun onResourceResponse(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest?, p3: CefResponse?): Boolean {
        return false
    }

    override fun onResourceLoadComplete(
        p0: CefBrowser?,
        p1: CefFrame?,
        p2: CefRequest?,
        p3: CefResponse?,
        p4: CefURLRequest.Status?,
        p5: Long
    ) {

    }

    override fun onProtocolExecution(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest?, p3: BoolRef?) {

    }
}