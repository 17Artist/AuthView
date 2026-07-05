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
package priv.seventeen.artist.arcartx.authview.auth

import fr.xephi.authme.api.v3.AuthMeApi
import org.bukkit.entity.Player
import priv.seventeen.artist.arcartx.authview.config.Setting
import priv.seventeen.artist.arcartx.authview.ui.AuthUI
import priv.seventeen.artist.arcartx.nms.AsteroidScheduler
import priv.seventeen.artist.blink.bukkitPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


object AuthService {

    /** 懒获取，AuthMe 完全初始化前可能为 null；不缓存，避免热重载后拿到失效引用。 */
    private val api: AuthMeApi? get() = AuthMeApi.getInstance()

    /** uuid -> 待执行的"宽限后弹界面"任务句柄 */
    private val pendingChecks = ConcurrentHashMap<UUID, Any>()

    /** uuid -> 界面登录连续失败次数 */
    private val failedAttempts = ConcurrentHashMap<UUID, Int>()

    fun isReady(): Boolean = api != null

    fun isAuthenticated(player: Player): Boolean = api?.isAuthenticated(player) ?: false

    fun isRegistered(name: String): Boolean = api?.isRegistered(name) ?: false

    fun checkPassword(name: String, password: String): Boolean =
        api?.checkPassword(name, password) ?: false

    /** 是否应跳过登录界面：AuthMe 未就绪 / 已登录 / 是 NPC / 属于免验证名单 */
    fun shouldSkip(player: Player): Boolean {
        val a = api ?: return true
        return a.isAuthenticated(player) || a.isNpc(player) || a.isUnrestricted(player)
    }

    /** 客户端资源就绪后调用：等宽限时间再决定是否弹界面（等待跨服自动登录）。 */
    fun scheduleAuthCheck(player: Player) {
        cancelPending(player)
        val delay = Setting.instance.bungeeGraceTicks.toLong().coerceAtLeast(0L)
        val handle = AsteroidScheduler.runTaskLater(bukkitPlugin, Runnable {
            pendingChecks.remove(player.uniqueId)
            if (!player.isOnline) return@Runnable
            if (shouldSkip(player)) return@Runnable
            AuthUI.openAuto(player)
        }, delay)
        if (handle != null) pendingChecks[player.uniqueId] = handle
    }

    /**
     * AuthMe 侧任意登录路径完成时调用（/login、forceLogin、会话恢复、跨服自动登录）。
     * 取消待弹任务、清理失败计数，并让界面撤下。
     */
    fun onAuthenticated(player: Player) {
        cancelPending(player)
        failedAttempts.remove(player.uniqueId)
        AuthUI.onExternalAuthenticated(player)
    }

    /** 记录一次界面登录失败并返回累计次数。 */
    fun registerFailure(player: Player): Int {
        val count = (failedAttempts[player.uniqueId] ?: 0) + 1
        failedAttempts[player.uniqueId] = count
        return count
    }

    fun resetFailures(player: Player) {
        failedAttempts.remove(player.uniqueId)
    }

    /** 玩家退出时清理其残留状态与待执行任务。 */
    fun cleanup(player: Player) {
        cancelPending(player)
        failedAttempts.remove(player.uniqueId)
    }

    /** 插件卸载时取消所有待执行任务。 */
    fun shutdown() {
        pendingChecks.values.forEach { AsteroidScheduler.cancelTask(it) }
        pendingChecks.clear()
        failedAttempts.clear()
    }

    private fun cancelPending(player: Player) {
        pendingChecks.remove(player.uniqueId)?.let { AsteroidScheduler.cancelTask(it) }
    }
}
