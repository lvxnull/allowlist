package org.lvxnull.allowlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class AllowList implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Allowlist");
    public static final String MOD_ID = "allowlist";
    private static AllowListStorage storage;

    public static AllowListStorage getStorage() {
        return storage;
    }

    @Override
    public void onInitializeServer(ModContainer mod) {
        var meta = mod.metadata();
        var config = AllowListConfig.INSTANCE;

        try {
            storage = new AllowListStorage(QuiltLoader.getConfigDir().resolve(config.savePath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var commandProvider = new AllowListCommandProvider(meta, storage);

        ServerLifecycleEvents.STARTING.register(s -> {
            LOGGER.info("Starting allowlist {}", meta.version());
            try {
                storage.load();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, env) -> commandProvider.register(dispatcher));

        ServerLifecycleEvents.STOPPING.register(s -> {
            LOGGER.info("Stopping allowlist {}", meta.version());
            try {
                storage.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
