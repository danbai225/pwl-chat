# pwl-chat

![Build](https://github.com/danbai225/pwl-chat/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.danbai225.pwlchat.svg)](https://plugins.jetbrains.com/plugin/18091-pwl-chat)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.danbai225.pwlchat.svg)](https://plugins.jetbrains.com/plugin/18091-pwl-chat)


![image-56c2aad9](https://danbai.oss-accelerate.aliyuncs.com/bk/image-56c2aad9.png)
<!-- Plugin description -->

This is a connection [pwl](https://fishpi.cn) chat plugin.

Enter`#Help`ForHelp

HelpCommand：Command starts with `#` Parameters are separated by spaces

`#help` - Output this help command

`#packet` - Send red envelope, parameter 1 (number) parameter 2 (total) parameter 3 (message)

`#revoke` - Withdraw the last message sent

`#exit` - sign out

`#eventLog` - Output chat parameter 1 in the event (whether 1 or 0 is turned on)

`#web` - Switch output mode parameter 1 (open) 0 or 1

`#clear` - ClearChatHistory

这是一个连接[pwl](https://fishpi.cn/)聊天插件。

输入`#help`获取帮助

帮助命令：命令都是以`#`开头 参数用空格分割

`#help` - 输出本帮助命令

`#packet` - 发送红包，参数1(个数) 参数2(总额) 参数3(消息)

`#revoke` - 撤回最后一条发送的消息

`#exit` - 退出登陆

`#eventLog` - 在事件中输出聊天 参数1(是否开启1 or 0)

`#web` - 切换输出模式 参数1(开启) 0 or 1

`#clear` - 清空聊天记录

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "pwl-chat"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/danbai225/pwl-chat/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## 已知BUG

## TODO

## 版本兼容问题

主要兼容2020.3以后的版本 v0.1.3
2021.1以后的版本 v0.1.4

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
