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

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("UNUSED")
object AllowList: ModInitializer {
    val meta: ModMetadata = FabricLoader.getInstance().getModContainer("allowlist").get().metadata
    val logger: Logger = LogManager.getLogger("AllowList")
    val storage = AllowListStorage(FabricLoader.getInstance().configDir)

    override fun onInitialize() {
        logger.info("Starting allowlist {}", meta.version)

        ServerLifecycleEvents.SERVER_STARTING.register {
            storage.load()
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("al").requires { s -> s.hasPermissionLevel(3) }.then(
                    literal("list").executes {
                        val text: LiteralText = LiteralText.EMPTY as LiteralText
                        text.append(LiteralText("Players currently allowed:\n").formatted(Formatting.YELLOW))
                        for(p in storage) {
                            text.append(" - ")
                                .append(LiteralText(p).formatted(Formatting.GREEN))
                                .append("\n")
                        }
                        it.source.sendFeedback(text, false)
                        1
                    }
                ).then(
                    literal("remove").then(
                        argument("player", string())
                            .suggests { _, builder -> CommandSource.suggestMatching(storage, builder) }
                            .executes {
                                val player = getString(it, "player")
                                if(!storage.remove(player)) {
                                    throw SimpleCommandExceptionType(Text.of("Player not on allowlist")).create()
                                }

                                it.source.sendFeedback(Text.of("Player $player has been removed from allowlist"), true)
                                1
                            }
                    )
                ).then(
                    literal("add").then(
                        argument("player", string())
                            .suggests { c, builder ->
                                CommandSource.suggestMatching(c.source.server.playerNames.filterNot(storage::isAllowed), builder)
                            }
                            .executes {
                                val player = getString(it, "player")
                                try {
                                    if(!storage.add(player)) {
                                        throw SimpleCommandExceptionType(Text.of("Player is already on allowlist")).create()
                                    }
                                    it.source.sendFeedback(Text.of("Player $player has been added to allowlist"), true)
                                    1
                                } catch(e: IllegalArgumentException) {
                                    throw SimpleCommandExceptionType(Text.of("Invalid player name")).create()
                                }
                            }
                    )
                ).then(
                    literal("version").executes {
                        it.source.sendFeedback(Text.of("AllowList version ${meta.version}"), false)
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
