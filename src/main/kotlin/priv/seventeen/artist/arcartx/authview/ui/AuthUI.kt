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
package priv.seventeen.artist.arcartx.authview.ui

import fr.xephi.authme.api.v3.AuthMeApi
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import priv.seventeen.artist.arcartx.api.ArcartXAPI
import priv.seventeen.artist.arcartx.authview.Bootstrap
import priv.seventeen.artist.arcartx.authview.auth.AuthService
import priv.seventeen.artist.arcartx.authview.config.Language
import priv.seventeen.artist.arcartx.authview.config.Setting
import priv.seventeen.artist.arcartx.core.ui.UIHandler
import priv.seventeen.artist.arcartx.nms.AsteroidScheduler
import priv.seventeen.artist.arcartx.util.collections.CallBack
import priv.seventeen.artist.blink.BlinkLog
import priv.seventeen.artist.blink.bukkitPlugin
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 登录 / 注册 / 改密界面处理器。
 *
 * 继承 [UIHandler]：对象初始化即向 ArcartX 注册界面并绑定 OPEN/CLOSE/PACKET 回调。
 * 因此必须在 [bukkitPlugin] 就绪、且界面文件已释放到磁盘之后才首次访问本对象
 * （见 [Bootstrap.onEnable] 的顺序：先 saveResource 再 [bootstrap]）。
 */
object AuthUI : UIHandler(Bootstrap.VIEW_ID, File(bukkitPlugin.dataFolder, Bootstrap.VIEW_RESOURCE)) {

    override val plugin: JavaPlugin
        get() = bukkitPlugin

    /** 记录"界面自身发起并已成功"的玩家：由客户端 success 处理器关闭，避免服务端二次关闭打断动画。 */
    private val gracefulClose = ConcurrentHashMap.newKeySet<UUID>()

    enum class Mode(val id: String) {
        LOGIN("login"),
        REGISTER("register"),
        CHANGE("change");

        fun subtitle(): String = when (this) {
            LOGIN -> Language.instance.loginSubtitle
            REGISTER -> Language.instance.registerSubtitle
            CHANGE -> Language.instance.changeSubtitle
        }
    }

    /** 空操作，仅用于在 ENABLE 阶段强制触发对象初始化（从而完成界面注册）。 */
    fun bootstrap() {
        BlinkLog.detail("已注册登录界面 §7${Bootstrap.VIEW_ID}")
    }

    /** 从磁盘热重载界面文件（保留已注册回调，并同步给在线玩家）。 */
    fun reload() {
        ArcartXAPI.getUIRegistry().reload(
            Bootstrap.VIEW_ID,
            File(bukkitPlugin.dataFolder, Bootstrap.VIEW_RESOURCE)
        )
    }

    /** 依据注册状态自动选择登录 / 注册界面。 */
    fun openAuto(player: Player) {
        val mode = if (AuthService.isRegistered(player.name)) Mode.LOGIN else Mode.REGISTER
        openView(player, mode)
    }

    /** 以指定模式打开界面，并在客户端确认打开后下发初始化数据。 */
    fun openView(player: Player, mode: Mode) {
        ui.open(player) {
            ui.sendPacket(
                player, "init", mapOf(
                    "mode" to mode.id,
                    "serverName" to Language.instance.serverName,
                    "subtitle" to mode.subtitle()
                )
            )
        }
    }

    /**
     * 由 [AuthService.onAuthenticated]（AuthMe LoginEvent）回调。
     * 若是界面自身发起的成功登录 → 交给客户端关闭；否则（外部登录 / 跨服自动登录 / 会话恢复）服务端关闭。
     */
    fun onExternalAuthenticated(player: Player) {
        if (gracefulClose.remove(player.uniqueId)) return
        close(player)
    }

    // ---------------------------------------------------------------------
    // UIHandler 回调
    // ---------------------------------------------------------------------

    override fun onPacket(player: Player, identifier: String, data: List<String>) {
        // onPacket 运行在 Netty 线程，操作 Bukkit / AuthMe 前切回主线程
        AsteroidScheduler.ensureMainThread(bukkitPlugin) {
            when (identifier.lowercase()) {
                "login" -> handleLogin(player, data)
                "register" -> handleRegister(player, data)
                "change" -> handleChange(player, data)
            }
        }
    }

    override fun onClose(player: Player) {
        gracefulClose.remove(player.uniqueId)
    }

    // ---------------------------------------------------------------------
    // 业务处理
    // ---------------------------------------------------------------------

    private fun handleLogin(player: Player, data: List<String>) {
        val lang = Language.instance
        if (isBlank(data, 1)) {
            feedback(player, lang.inputIncomplete)
            return
        }
        val password = data[0]
        if (AuthService.checkPassword(player.name, password)) {
            AuthService.resetFailures(player)
            gracefulClose.add(player.uniqueId)
            success(player, lang.loginSuccess)
            AuthMeApi.getInstance()?.forceLogin(player)
        } else {
            val attempts = AuthService.registerFailure(player)
            val max = Setting.instance.maxLoginAttempts
            if (max in 1..attempts) {
                player.kickPlayer(legacy(lang.tooManyAttempts))
            } else {
                feedback(player, lang.passwordError)
            }
        }
    }

    private fun handleRegister(player: Player, data: List<String>) {
        val lang = Language.instance
        if (AuthService.isRegistered(player.name)) {
            feedback(player, lang.alreadyRegistered)
            return
        }
        if (isBlank(data, 2)) {
            feedback(player, lang.inputIncomplete)
            return
        }
        val password = data[0]
        val confirm = data[1]
        if (password != confirm) {
            feedback(player, lang.passwordMismatch)
            return
        }
        val lengthError = passwordLengthError(password)
        if (lengthError != null) {
            feedback(player, lengthError)
            return
        }
        val api = AuthMeApi.getInstance() ?: return
        api.registerPlayer(player.name, password)
        gracefulClose.add(player.uniqueId)
        success(player, lang.registerSuccess)
        api.forceLogin(player)
    }

    private fun handleChange(player: Player, data: List<String>) {
        val lang = Language.instance
        if (isBlank(data, 3)) {
            feedback(player, lang.inputIncomplete)
            return
        }
        val oldPassword = data[0]
        val newPassword = data[1]
        val confirm = data[2]
        if (!AuthService.checkPassword(player.name, oldPassword)) {
            feedback(player, lang.oldPasswordError)
            return
        }
        if (newPassword != confirm) {
            feedback(player, lang.passwordMismatch)
            return
        }
        val lengthError = passwordLengthError(newPassword)
        if (lengthError != null) {
            feedback(player, lengthError)
            return
        }
        // 改密不产生 LoginEvent，界面由客户端 success 处理器自行关闭
        AuthMeApi.getInstance()?.changePassword(player.name, newPassword)
        success(player, lang.changeSuccess)
    }

    // ---------------------------------------------------------------------
    // 工具
    // ---------------------------------------------------------------------

    /** 失败反馈：显示消息并让面板抖动。 */
    private fun feedback(player: Player, message: String) {
        ui.sendPacket(player, "feedback", mapOf("message" to message, "ok" to false))
    }

    /** 成功反馈：显示消息，客户端稍后关闭界面。 */
    private fun success(player: Player, message: String) {
        ui.sendPacket(player, "success", mapOf("message" to message))
    }

    private fun passwordLengthError(password: String): String? {
        val setting = Setting.instance
        if (password.length < setting.minPasswordLength) {
            return Language.instance.passwordTooShort.replace("%min%", setting.minPasswordLength.toString())
        }
        if (setting.maxPasswordLength > 0 && password.length > setting.maxPasswordLength) {
            return Language.instance.passwordTooLong.replace("%max%", setting.maxPasswordLength.toString())
        }
        return null
    }

    private fun isBlank(data: List<String>, need: Int): Boolean =
        data.size < need || (0 until need).any { data[it].isBlank() }

    /** 界面文本走 `&`，Bukkit 踢出等场景需转成 `§`。 */
    private fun legacy(text: String): String = text.replace('&', '§')
}
