# pwl-chat

![Build](https://github.com/danbai225/pwl-chat/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.danbai225.pwlchat.svg)](https://plugins.jetbrains.com/plugin/com.github.danbai225.pwlchat)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.danbai225.pwlchat.svg)](https://plugins.jetbrains.com/plugin/com.github.danbai225.pwlchat)

## Template ToDo list

<!-- Plugin description -->

This is a [pwl](https://pwl.icu) chat plugin，We are a group of people who love work and the boss.

Enter`#Help`ForHelp

HelpCommand：Command starts with `#` Parameters are separated by spaces

`#help` - Output this help command

`#packet` - Send red envelope, parameter 1 (number) parameter 2 (total) parameter 3 (message)

`#revoke` - Withdraw the last message sent

`#exit` - sign out

`#eventLog` - Output chat parameter 1 in the event (whether 1 or 0 is turned on)

`#web` - Switch output mode parameter 1 (open) 0 or 1


这是一个[pwl](https://pwl.icu)聊天插件，我们是一群热爱工作和老板的人。

输入`#help`获取帮助

帮助命令：命令都是以`#`开头 参数用空格分割

`#help` - 输出本帮助命令

`#packet` - 发送红包，参数1(个数) 参数2(总额) 参数3(消息)

`#revoke` - 撤回最后一条发送的消息

`#exit` - 退出登陆

`#eventLog` - 在事件中输出聊天 参数1(是否开启1 or 0)

`#web` - 切换输出模式 参数1(开启) 0 or 1

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "pwl-chat"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/danbai225/pwl-chat/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## 已知BUG

- 输入框输入参数空时报错 `java.lang.NumberFormatException: For input string: ""`

## TODO

- 引用
- web页撤回
- draw接口

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
