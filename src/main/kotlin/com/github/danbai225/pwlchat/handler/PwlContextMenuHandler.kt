package com.github.danbai225.pwlchat.handler

import com.github.danbai225.pwlchat.component.WebChat
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandler


class PwlContextMenuHandler() : CefContextMenuHandler {
    private var wb :WebChat?=null

    constructor(w:WebChat) : this() {
       wb=w
    }
    override fun onBeforeContextMenu(p0: CefBrowser?, p1: CefFrame?, p2: CefContextMenuParams?, p3: CefMenuModel?) {
        p3?.clear()//清除默认的菜单项
        p3?.addItem(CefMenuModel.MenuId.MENU_ID_USER_LAST, "Open DevTools")
        p3?.addItem(RefreshEmptyID, "RefreshEmpty")
    }

    override fun onContextMenuCommand(
        p0: CefBrowser?,
        p1: CefFrame?,
        p2: CefContextMenuParams?,
        p3: Int,
        p4: Int
    ): Boolean {
        when(p3){
            CefMenuModel.MenuId.MENU_ID_USER_LAST->{
                wb?.openDevtools()
                return true
            }
            RefreshEmptyID->{
                wb?.loadChatPage()
                return true
            }
        }
      return false
    }

    override fun onContextMenuDismissed(p0: CefBrowser?, p1: CefFrame?) {
    }
    companion object {
        private const val RefreshEmptyID=29101
    }
}