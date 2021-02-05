package skullbot.dimensiown.entities;

import com.qouteall.immersive_portals.dimension_sync.DimId;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.registry.Entities;

public class DimensionalPortal extends Portal
{
  public World transmitterWorld;

  public DimensionalPortal( World world_1 )
  {
    super( Entities.DIM_PORTAL, world_1 );
  }

  @Override
  protected void readCustomDataFromTag( CompoundTag compoundTag )
  {
    super.readCustomDataFromTag( compoundTag );
    transmitterWorld = Dimensiown.SERVER.getWorld( DimId.getWorldId( compoundTag, "WorldId", transmitterWorld.isClient ) );
  }

  @Override
  protected void writeCustomDataToTag( CompoundTag compoundTag )
  {
    super.writeCustomDataToTag( compoundTag );
    DimId.putWorldId( compoundTag, "WorldId", transmitterWorld.getRegistryKey() );
  }

  @Override
  public void tick()
  {
    super.tick();
  }
}
