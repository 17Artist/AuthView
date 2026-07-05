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
package priv.seventeen.artist.arcartx.authview

import priv.seventeen.artist.arcartx.authview.auth.AuthService
import priv.seventeen.artist.arcartx.authview.command.AuthCommand
import priv.seventeen.artist.arcartx.authview.config.Language
import priv.seventeen.artist.arcartx.authview.config.Setting
import priv.seventeen.artist.arcartx.authview.ui.AuthUI
import priv.seventeen.artist.blink.BlinkLog
import priv.seventeen.artist.blink.bukkitPlugin
import priv.seventeen.artist.blink.lifecycle.Awake
import priv.seventeen.artist.blink.lifecycle.LifeCycle

object Bootstrap {

    /** ArcartX 界面唯一标识 */
    const val VIEW_ID = "ArcartXAuthView:Main"

    const val VIEW_RESOURCE = "view/auth_view.yml"

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        // 1. 加载配置与文案
        Setting.load()
        Language.load()
        // 2. 先把默认界面文件释放到数据目录（首次），再注册界面
        bukkitPlugin.saveResource(VIEW_RESOURCE, false)
        AuthUI.bootstrap()
        // 3. 注册命令
        AuthCommand.register()

        if (!AuthService.isReady()) {
            BlinkLog.warn("未获取到 AuthMe API，联动功能暂不可用（请确认已安装并启用 AuthMe）")
        }
        val autoOpen = if (Setting.instance.enableAutoOpen) "§a开" else "§c关"
        BlinkLog.success(
            "登录界面已就绪 · 联动 §bAuthMe§f · 自动弹出 $autoOpen§f · 跨服宽限 §e${Setting.instance.bungeeGraceTicks}§f tick"
        )
    }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        AuthService.shutdown()
    }
}
