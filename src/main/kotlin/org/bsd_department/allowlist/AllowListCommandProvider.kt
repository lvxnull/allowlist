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
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

typealias MCArgumentBuilder<T> = ArgumentBuilder<ServerCommandSource, T>
typealias MCCommandContext = CommandContext<ServerCommandSource>
typealias MCCommandDispatcher = CommandDispatcher<ServerCommandSource>

inline fun <T: MCArgumentBuilder<T>> T.executesCommand(crossinline command: (MCCommandContext) -> Unit): T {
    return this.executes {
        command(it)
        1
    }
}

class AllowListCommandProvider(private val meta: ModMetadata,
                               private val storage: AllowListStorage) {
    private var registered = false

    fun register(dispatcher: MCCommandDispatcher) {
        if(registered) return
        dispatcher.register(
            literal("al")
                .requires { s -> s.hasPermissionLevel(3) }
                .then(literal("list").executesCommand(::list))
                .then(
                    literal("remove").then(
                        argument("player", string())
                            .suggests { _, builder -> CommandSource.suggestMatching(storage, builder) }
                            .executesCommand { remove(it, getString(it, "player")) }
                    )
                ).then(
                    literal("add").then(
                        argument("player", string())
                            .suggests { c, builder ->
                                CommandSource.suggestMatching(c.source.server.playerNames.filterNot(storage::isAllowed), builder)
                            }.executesCommand { add(it, getString(it, "player")) }
                    )
                ).then(literal("version").executesCommand { version(it) })
        )
        registered = true
    }

    private fun list(context: MCCommandContext) {
        if(storage.size == 0) {
            context.source.sendFeedback(Text.of("There are currently no users on the allowlist"), false)
            return
        }
        val text = LiteralText.EMPTY.copy()
        text.append(LiteralText("Players currently allowed:\n").formatted(Formatting.YELLOW))
        for(p in storage) {
            text.append(" - ")
                .append(LiteralText(p).formatted(Formatting.GREEN))
                .append("\n")
        }
        context.source.sendFeedback(text, false)
    }

    private fun remove(context: MCCommandContext, player: String) {
        if(!storage.remove(player)) {
            throw SimpleCommandExceptionType(Text.of("Player not on allowlist")).create()
        }

        context.source.sendFeedback(Text.of("Player $player has been removed from allowlist"), true)
    }

    private fun add(context: MCCommandContext, player: String) {
        try {
            if(!storage.add(player)) {
                throw SimpleCommandExceptionType(Text.of("Player is already on allowlist")).create()
            }
            context.source.sendFeedback(Text.of("Player $player has been added to allowlist"), true)
        } catch(e: IllegalArgumentException) {
            throw SimpleCommandExceptionType(Text.of("Invalid player name")).create()
        }
    }

    private fun version(context: MCCommandContext) {
        context.source.sendFeedback(Text.of("AllowList version ${meta.version}"), false)
    }
}

