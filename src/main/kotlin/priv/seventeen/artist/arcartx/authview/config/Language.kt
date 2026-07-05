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
package priv.seventeen.artist.arcartx.authview.config

import priv.seventeen.artist.blink.bukkitPlugin
import priv.seventeen.artist.blink.config.BlinkConfig
import priv.seventeen.artist.blink.config.Comment
import priv.seventeen.artist.blink.config.ConfigKey

/**
 * 文案配置：plugins/ArcartXAuthView/language.yml
 *
 * 说明：以下发送到界面的文本使用 `&` 颜色代码（ArcartX 客户端可直接渲染），
 * 只有踢出消息等走 Bukkit 的场景才会在代码中转成 `§`。
 */
class Language : BlinkConfig(bukkitPlugin, "language") {

    @Comment("界面主标题（一般写服务器名）")
    @ConfigKey("server-name")
    var serverName: String = "&f&lMinecraft &b&lServer"

    @Comment("登录界面副标题")
    @ConfigKey("login-subtitle")
    var loginSubtitle: String = "&7欢迎回来，请输入密码登录"

    @Comment("注册界面副标题")
    @ConfigKey("register-subtitle")
    var registerSubtitle: String = "&7欢迎加入，请设置你的登录密码"

    @Comment("改密界面副标题")
    @ConfigKey("change-subtitle")
    var changeSubtitle: String = "&7修改你的账户密码"

    @Comment("输入不完整")
    @ConfigKey("input-incomplete")
    var inputIncomplete: String = "&c请填写完整的信息"

    @Comment("两次密码不一致")
    @ConfigKey("password-mismatch")
    var passwordMismatch: String = "&c两次输入的密码不一致"

    @Comment("原密码错误")
    @ConfigKey("old-password-error")
    var oldPasswordError: String = "&c原密码错误"

    @Comment("登录密码错误")
    @ConfigKey("password-error")
    var passwordError: String = "&c密码错误，请重试"

    @Comment("密码过短（%min% 会被替换为配置的最小长度）")
    @ConfigKey("password-too-short")
    var passwordTooShort: String = "&c密码长度不能少于 %min% 位"

    @Comment("密码过长（%max% 会被替换为配置的最大长度）")
    @ConfigKey("password-too-long")
    var passwordTooLong: String = "&c密码长度不能超过 %max% 位"

    @Comment("登录成功")
    @ConfigKey("login-success")
    var loginSuccess: String = "&a登录成功！"

    @Comment("注册成功")
    @ConfigKey("register-success")
    var registerSuccess: String = "&a注册成功，已自动登录！"

    @Comment("改密成功")
    @ConfigKey("change-success")
    var changeSuccess: String = "&a密码修改成功！"

    @Comment("重复注册")
    @ConfigKey("already-registered")
    var alreadyRegistered: String = "&c该账户已注册，无法重复注册"

    @Comment("错误次数过多被踢出时的提示")
    @ConfigKey("too-many-attempts")
    var tooManyAttempts: String = "&c密码错误次数过多，请稍后重新连接"

    companion object {
        lateinit var instance: Language
            private set

        fun load() {
            instance = Language()
            instance.load()
        }

        fun reload() = instance.reload()
    }
}
