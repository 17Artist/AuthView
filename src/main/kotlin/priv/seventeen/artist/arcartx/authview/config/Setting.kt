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
 * 插件行为配置
 */
class Setting : BlinkConfig(bukkitPlugin, "config") {

    @Comment("客户端资源就绪后，若玩家未登录，是否自动弹出登录 / 注册界面")
    @ConfigKey("enable-auto-open")
    var enableAutoOpen: Boolean = true

    @Comment("等待跨服(BungeeCord / Velocity)自动登录的宽限时间，单位 tick（20 tick = 1 秒）。")
    @Comment("玩家从其它子服切换过来时，AuthMe 的跨服自动登录消息可能晚于客户端就绪到达，")
    @Comment("在此宽限期内暂不弹界面，避免登录界面闪一下又被关掉。建议 20 ~ 60。")
    @ConfigKey("bungee-grace-ticks")
    var bungeeGraceTicks: Int = 40

    @Comment("界面登录允许的最大连续密码错误次数，超过后踢出玩家用于防爆破。0 表示不限制。")
    @Comment("注意：界面登录走 AuthMe 的 checkPassword API，不计入 AuthMe 自身的错误次数，")
    @Comment("因此这里由本插件独立做次数限制。")
    @ConfigKey("max-login-attempts")
    var maxLoginAttempts: Int = 5

    @Comment("注册 / 改密时界面侧校验的最小密码长度，建议与 AuthMe 的 settings.security.minPasswordLength 保持一致")
    @ConfigKey("min-password-length")
    var minPasswordLength: Int = 4

    @Comment("注册 / 改密时界面侧校验的最大密码长度，0 表示不限制")
    @ConfigKey("max-password-length")
    var maxPasswordLength: Int = 30

    @Comment("未登录时，是否禁止玩家点击 ArcartX 额外槽位（Extra Slot）")
    @ConfigKey("lock-extra-slot")
    var lockExtraSlot: Boolean = true

    companion object {
        lateinit var instance: Setting
            private set

        fun load() {
            instance = Setting()
            instance.load()
        }

        fun reload() = instance.reload()
    }
}
