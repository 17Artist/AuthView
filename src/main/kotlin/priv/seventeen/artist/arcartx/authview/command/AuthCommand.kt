/*
    Copyright (C) 2024-2026 17Artist

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package priv.seventeen.artist.arcartx.authview.command

import priv.seventeen.artist.arcartx.authview.config.Language
import priv.seventeen.artist.arcartx.authview.config.Setting
import priv.seventeen.artist.arcartx.authview.ui.AuthUI
import priv.seventeen.artist.blink.bukkitPlugin
import priv.seventeen.artist.blink.command.BlinkCommand
import priv.seventeen.artist.blink.command.BlinkCommandRegistrar
import priv.seventeen.artist.blink.command.SenderType

object AuthCommand {

    fun register() {
        val command = BlinkCommand("arcartxauthview", "aav", "authview")
            .command(
                "reload", "重载配置、语言与界面",
                permission = "authview.reload", sender = SenderType.OP
            ) { ctx ->
                Setting.reload()
                Language.reload()
                AuthUI.reload()
                ctx.reply("§a[AuthView] §f已重载配置、语言与界面")
            }
            .command("login", "打开登录界面", sender = SenderType.PLAYER) { ctx ->
                val player = ctx.player ?: return@command ctx.reply("§c仅玩家可用")
                AuthUI.openView(player, AuthUI.Mode.LOGIN)
            }
            .command("register", "打开注册界面", sender = SenderType.PLAYER) { ctx ->
                val player = ctx.player ?: return@command ctx.reply("§c仅玩家可用")
                AuthUI.openView(player, AuthUI.Mode.REGISTER)
            }
            .command("change", "打开修改密码界面", sender = SenderType.PLAYER) { ctx ->
                val player = ctx.player ?: return@command ctx.reply("§c仅玩家可用")
                AuthUI.openView(player, AuthUI.Mode.CHANGE)
            }

        BlinkCommandRegistrar.register(bukkitPlugin, command)
    }
}
