package com.github.danbai225.pwlchat.handler

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandler


class PwlContextMenuHandler : CefContextMenuHandler {
    override fun onBeforeContextMenu(p0: CefBrowser?, p1: CefFrame?, p2: CefContextMenuParams?, p3: CefMenuModel?) {
        p3?.clear();//清除默认的菜单项
        p3?.addItem(CefMenuModel.MenuId.MENU_ID_USER_LAST, "Open DevTools")
    }

    override fun onContextMenuCommand(
        p0: CefBrowser?,
        p1: CefFrame?,
        p2: CefContextMenuParams?,
        p3: Int,
        p4: Int
    ): Boolean {
        if (p3 == CefMenuModel.MenuId.MENU_ID_USER_LAST) {

            p0?.devTools?.setWindowVisibility(true)
            return true
        }
      return false
    }

    override fun onContextMenuDismissed(p0: CefBrowser?, p1: CefFrame?) {
    }
}