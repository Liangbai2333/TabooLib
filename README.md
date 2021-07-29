![](https://wiki.ptms.ink/images/7/79/禁忌书库LOGO_SMAIL.png)

## TabooLib framework

[![](https://app.codacy.com/project/badge/Grade/3e9c747cd4aa484ab7cd74b7666c4c43)](https://www.codacy.com/gh/TabooLib/TabooLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TabooLib/TabooLib&amp;utm_campaign=Badge_Grade)
[![](https://www.codefactor.io/repository/github/taboolib/taboolib/badge)](https://www.codefactor.io/repository/github/taboolib/taboolib)
![](https://img.shields.io/github/contributors/taboolib/taboolib)
![](https://img.shields.io/github/languages/code-size/taboolib/taboolib)
![](https://img.shields.io/github/release/taboolib/taboolib)

TabooLib 是为 Minecraft（Java 版）提供一个跨平台的插件开发框架。但是 TabooLib 不是一个平台，也不提供插件的运行环境，而是帮助开发者在各个平台上快速开发，代替一些频繁使用或是相对复杂的操作，以及解决一些令人头疼的问题。

* TabooLib 起初是针对 Bukkit 的解决方案，不过现在正在横向发展。
  * 我们使用 MIT 协议，这个协议非常的宽松。
  * 开发速度至上。

随着 6.0 版本的更新，我们更加注重安全性和稳定性。抛弃了上个版本问题频发的热加载机制，虽然那样能够显著的减少插件体积，以及带来一个中心化的插件管理器。但是随着 Minecraft 的版本更新，以及大量的第三方 Spigot 分支出现，这样的设计出现了不少问题。所以迎来 6.0 版本的巨大更新是铁板钉钉的事情，也是在这个版本我们重新精心设计了 TabooLib 的每一个工具。

大多数基于 TabooLib 的插件应能跨多个 Minecraft 版本使用而不用特别更新。即在大部分情况下，服主不需要担心插件不兼容的问题。甚至是大面积使用 nms 代码也不例外，TabooLib 提供了数个堪比魔法的工具。

**简单一点的，例如你可以按照 TabooLib 提供的方法快速注册命令。**

```kotlin
command("tpuuid") {
    literal("random") {
        execute<ProxyPlayer> { player, _, _ ->
            player.teleport(player.entities().randomOrNull() ?: return@execute)
        }
    }
    dynamic(optional = true) {
        suggestion<ProxyPlayer> { player, _ ->
            player.entities().map { it.toString() }
        }
        execute<ProxyPlayer> { player, _, argument ->
            player.teleport(UUID.fromString(argument))
        }
    }
    execute<ProxyPlayer> { player, _, _ ->
        player.teleport(player.entityNearly() ?: return@execute)
    }
}
```

复杂一点的就像下面这样自行搭建多平台实现，TabooLib 会根据当前运行平台选择相应的实现类。

```kotlin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.*
import taboolib.platform.util.toBukkitLocation
import java.util.*

interface PlatformEntityHandler {

    fun entities(player: ProxyPlayer): List<UUID>

    fun entityNearly(player: ProxyPlayer): UUID?

    fun teleport(player: ProxyPlayer, uuid: UUID)

    @PlatformImplementation(Platform.BUKKIT)
    class BukkitSide : PlatformEntityHandler {

        override fun entities(player: ProxyPlayer): List<UUID> {
            return player.cast<Player>().world.entities.map { it.uniqueId }
        }

        override fun entityNearly(player: ProxyPlayer): UUID? {
            return player.cast<Player>().world.entities
                .filter { it != player.origin }
                .minByOrNull { it.location.distance(player.location.toBukkitLocation()) }?.uniqueId
        }

        override fun teleport(player: ProxyPlayer, uuid: UUID) {
            player.cast<Player>().teleport(Bukkit.getEntity(uuid) ?: return)
        }
    }
}

fun ProxyPlayer.entities(): List<UUID> {
    return implementations<PlatformEntityHandler>().entities(this)
}

fun ProxyPlayer.entityNearly(): UUID? {
    return implementations<PlatformEntityHandler>().entityNearly(this)
}

fun ProxyPlayer.teleport(uuid: UUID) {
    implementations<PlatformEntityHandler>().teleport(this, uuid)
}
```

如果你的插件仅在 Bukkit 平台上工作，那么大可不必这么做。因为 TabooLib 的职责是帮助开发者们尽可能快速的完成开发工作，而不是制造一些无意义的方法来增加仓库体积。

## 模块

  * **common**: TabooLib 的核心部分，环境部署以及跨平台接口
  * **common-5**: TabooLib 5.0 版本保留下来的一些工具
  * **module-ai**: 管理与注册自定义实体 AI（Pathfinder）
  * **module-chat**: Component（Json）信息构建工具与 1.16 RGB 颜色转换
  * **module-configuration**: Yaml 封装接口与配置文件管理工具
  * **module-database**: 数据库管理工具
  * **module-effect**: 粒子生成工具
  * **module-kether**: 内建脚本（动作语句）解决方案
  * **module-lang**: 语言文件工具
  * **module-metrics**: bStats 整合
  * **module-navigation**: 无实体寻路工具
  * **module-nms**: 跨版本 nms 解决方案与数据包管理工具
  * **module-nms-util**: 常用 nms 工具集合
  * **module-porticus**: BungeeCord 通讯工具
  * **module-ui**: 箱子菜单构建工具
  * **module-ui-receptacle**: 箱子菜单构建工具（发包实现）
  * **platform-bukkit**: Bukkit 实现
  * **platform-bungee**: BungeeCord 实现
  * **platform-nukkit**: Nukkit 实现
  * **platform-sponge-api7**: Sponge (api7) 实现
  * **platform-sponge-api8**: Sponge (api8) 实现
  * **platform-velocity**: Velocity 实现

# 相关链接

  * [TabooLib 文档](https://docs.tabooproject.org)
  * [TabooLib SDK](https://github.com/taboolib/taboolib-sdk)
