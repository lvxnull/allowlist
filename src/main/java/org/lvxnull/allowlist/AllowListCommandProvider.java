package org.lvxnull.allowlist;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.quiltmc.loader.api.ModMetadata;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class AllowListCommandProvider {
    private static final SimpleCommandExceptionType NOT_LISTED_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("Player not on allowlist"));
    private static final SimpleCommandExceptionType ALREADY_LISTED_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("Player alredy on allowlist"));
    private static final SimpleCommandExceptionType INVALID_PLAYER_NAME_EXCEPTION =
            new SimpleCommandExceptionType(Text.of("Invalid player name"));
    private boolean registered = false;
    private final ModMetadata meta;
    private final AllowListStorage storage;

    private CompletableFuture<Suggestions> suggestUnlistedPlayers(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(ctx.getSource().getServer().getPlayerNames()).filter(p -> !storage.contains(p)), builder);
    }

    public AllowListCommandProvider(ModMetadata meta, AllowListStorage storage) {
        this.meta = meta;
        this.storage = storage;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        if(registered) return;
        dispatcher.register(
                literal("al")
                              .requires(s -> s.hasPermissionLevel(3))
                              .then(literal("list").executes(this::list))
                              .then(
                                literal("remove").then(
                                        argument("player", string())
                                                .suggests((ctx, builder) -> CommandSource.suggestMatching(storage, builder))
                                                .executes(ctx -> remove(ctx, getString(ctx, "player")))
                                )
                        ).then(
                                literal("add").then(
                                        argument("player", string())
                                                .suggests(this::suggestUnlistedPlayers)
                                                .executes(ctx -> add(ctx, getString(ctx, "player")))
                                )
                        ).then(literal("version").executes(this::version))


        );
        registered = true;
    }


    private int list(CommandContext<ServerCommandSource> ctx) {
        if(storage.size() == 0) {
            ctx.getSource().sendFeedback(() -> Text.of("There are currently no users on the allowlist"), false);
            return 2;
        }
        var text = Text.literal("Players currently on the allowlist:\n")
                       .formatted(Formatting.YELLOW);
        for(var p: storage) {
            text.append(" - ")
                .append(Text.literal(p).formatted(Formatting.GREEN))
                .append("\n");

        }
        ctx.getSource().sendFeedback(() -> text, false);

        return 1;
    }

    private int remove(CommandContext<ServerCommandSource> ctx, String player) throws CommandSyntaxException {
        if(!storage.remove(player)) {
            throw NOT_LISTED_EXCEPTION.create();
        }

        ctx.getSource().sendFeedback(() -> Text.of(String.format("Player %s has been removed from the allowlist", player)), false);
        return 1;
    }

    private int add(CommandContext<ServerCommandSource> ctx, String player) throws CommandSyntaxException {
        try {
            if(!storage.add(player)) {
                throw ALREADY_LISTED_EXCEPTION.create();
            }
            ctx.getSource().sendFeedback(() -> Text.of(String.format("Player %s has been added to the allowlist", player)), false);
        } catch(IllegalArgumentException e) {
            throw INVALID_PLAYER_NAME_EXCEPTION.create();
        }
        return 1;
    }

    private int version(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.of(String.format("AllowList version %s", meta.version())), false);
        return 1;
    }
}
