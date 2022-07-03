/*This file is part of allowlist.

  Allowlist is free software: you can redistribute it and/or modify it under the
  terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  Allowlist is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  more details.

  You should have received a copy of the GNU General Public License along with
  allowlist. If not, see <https://www.gnu.org/licenses/>. */

package org.lvxnull.allowlist

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("UNUSED")
object AllowList: ModInitializer {
    val meta: ModMetadata = FabricLoader.getInstance().getModContainer("allowlist").get().metadata
    val logger: Logger = LogManager.getLogger("AllowList")

    val storage = AllowListStorage(FabricLoader.getInstance().configDir)

    override fun onInitialize() {
        logger.info("Starting allowlist {}", meta.version)
        storage.load()

        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("al").requires { s -> s.hasPermissionLevel(3) }.then(
                    literal("list").executes {
                        val sb = StringBuilder("§ePlayers currently allowed:§r\n")
                        for(p in storage) {
                            sb.appendLine(" - §a$p§r")
                        }
                        it.source.sendFeedback(Text.of(sb.toString()), false)
                        1
                    }
                )
            )
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            onClose()
        }
    }

    private fun onClose() {
        logger.info("Stopping allowlist {}", meta.version)
        storage.close()
    }
}
