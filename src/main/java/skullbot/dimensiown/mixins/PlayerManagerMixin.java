package skullbot.dimensiown.mixins;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skullbot.dimensiown.stores.PlayerDimensionManager;

@Mixin( PlayerManager.class )
public class PlayerManagerMixin
{
  @Inject( method = "onPlayerConnect", at = @At( "HEAD" ) )
  private void onPlayerConnect( ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci )
  {
    PlayerDimensionManager manager = PlayerDimensionManager.get();
    manager.onPlayerJoin( player );
  }

  @Inject( method = "remove", at = @At( "HEAD" ) )
  private void onPlayerDisconnect( ServerPlayerEntity player, CallbackInfo ci )
  {
    PlayerDimensionManager manager = PlayerDimensionManager.get();
    manager.onPlayerLeave( player );
  }
}
