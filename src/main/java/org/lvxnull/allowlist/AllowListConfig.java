package org.lvxnull.allowlist;

import net.minecraft.text.Text;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Processor;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.loader.api.config.v2.QuiltConfig;

import static org.lvxnull.allowlist.AllowListUtil.colorize;

public class AllowListConfig extends ReflectiveConfig {
    public static final AllowListConfig INSTANCE = QuiltConfig.create(AllowList.MOD_ID, AllowList.MOD_ID, AllowListConfig.class);

    @Processor("processMessage")
    public final TrackedValue<String> message = this.value("&cYou're not allowed to enter");
    public transient Text messageText = colorize(message.value());

    @SuppressWarnings("unused")
    public void processMessage(TrackedValue.Builder<String> builder) {
        builder.callback(value -> messageText = colorize(value.value()));
    }
}
