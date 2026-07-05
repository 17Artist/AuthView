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
package priv.seventeen.artist.arcartx.authview.listener

import fr.xephi.authme.events.LoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import priv.seventeen.artist.arcartx.authview.auth.AuthService
import priv.seventeen.artist.arcartx.authview.config.Setting
import priv.seventeen.artist.arcartx.event.client.ClientExtraSlotClickEvent
import priv.seventeen.artist.arcartx.event.client.ClientInitializedEvent
import priv.seventeen.artist.blink.event.AutoListener

object AuthListener {


    @AutoListener
    fun onClientReady(event: ClientInitializedEvent.ResourceLoaded) {
        if (!Setting.instance.enableAutoOpen) return
        val player = event.player
        if (AuthService.shouldSkip(player)) return
        AuthService.scheduleAuthCheck(player)
    }


    @AutoListener
    fun onAuthMeLogin(event: LoginEvent) {
        AuthService.onAuthenticated(event.player)
    }


    @AutoListener
    fun onExtraSlotClick(event: ClientExtraSlotClickEvent) {
        if (!Setting.instance.lockExtraSlot) return
        if (!AuthService.isAuthenticated(event.player)) {
            event.isCancelled = true
        }
    }


    @AutoListener
    fun onQuit(event: PlayerQuitEvent) {
        AuthService.cleanup(event.player)
    }
}
