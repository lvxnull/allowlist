package org.lvxnull.allowlist.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.lvxnull.allowlist.AllowList;
import org.lvxnull.allowlist.AllowListConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public final class PlayerJoinMixin {
    @Inject(at = @At("HEAD"),
            method = "checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;",
            cancellable = true)
    private void onJoin(SocketAddress ignored, GameProfile profile, CallbackInfoReturnable<Text> info) {
        if(!AllowList.getStorage().contains(profile.getName())) {
            info.setReturnValue(AllowListConfig.INSTANCE.messageText);
        }
    }
}
