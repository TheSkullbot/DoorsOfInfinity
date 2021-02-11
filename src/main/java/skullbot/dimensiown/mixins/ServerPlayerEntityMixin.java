package skullbot.dimensiown.mixins;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skullbot.dimensiown.stores.DimensionInfo;
import skullbot.dimensiown.stores.DimensionOwner;
import skullbot.dimensiown.stores.PlayerDimensionManager;

@Mixin( ServerPlayerEntity.class )
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements DimensionOwner
{
  @Unique
  private final DimensionInfo dimensionInfo = new DimensionInfo( this.getUuid() );

  private ServerPlayerEntityMixin( World world, BlockPos pos, float yaw, GameProfile profile )
  {
    super( world, pos, yaw, profile );
  }

  @Override
  public DimensionInfo getDimensionInfo()
  {
    return this.dimensionInfo;
  }

  @Inject( method = "readCustomDataFromTag", at = @At( "RETURN" ) )
  private void readCustomDataFromTag( CompoundTag tag, CallbackInfo ci )
  {
    if( tag.contains( "dimensions", NbtType.LIST ) )
    {
      PlayerDimensionManager.get().loadFromPlayer( this, tag.getList( "dimensions", NbtType.STRING ) );
    }
  }
}
