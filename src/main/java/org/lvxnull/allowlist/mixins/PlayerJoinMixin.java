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

package org.lvxnull.allowlist.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.lvxnull.allowlist.AllowList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {
    @Inject(at = @At("HEAD"),
            method = "checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;",
            cancellable = true)
    private void onJoin(SocketAddress ignored, GameProfile profile, CallbackInfoReturnable<Text> info) {
        if(!AllowList.INSTANCE.getStorage().isAllowed(profile.getName())) {
            info.setReturnValue(Text.of("You have not been allowed on this server"));
        }
    }
}
