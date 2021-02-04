package skullbot.dimensiown.entities;

import com.qouteall.immersive_portals.dimension_sync.DimId;
import com.qouteall.immersive_portals.my_util.IntBox;
import com.qouteall.immersive_portals.portal.Portal;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.registry.Entities;
import skullbot.dimensiown.utils.BoxUtils;

public class DimensionalPortal extends Portal
{

  public IntBox transmitterArea;
  public IntBox glassArea;

  public World transmitterWorld;

  public DimensionalPortal( World world_1 )
  {
    super( Entities.DIM_PORTAL, world_1 );
  }

  @Override
  protected void readCustomDataFromTag( CompoundTag compoundTag )
  {
    super.readCustomDataFromTag( compoundTag );
    transmitterArea = new IntBox( new BlockPos( BoxUtils.vecFromTag( compoundTag.getCompound( "PhotonTransmitterL" ) ) ), new BlockPos( BoxUtils.vecFromTag( compoundTag.getCompound( "PhotonTransmitterH" ) ) ) );
    glassArea = new IntBox( new BlockPos( BoxUtils.vecFromTag( compoundTag.getCompound( "GlassAreaL" ) ) ), new BlockPos( BoxUtils.vecFromTag( compoundTag.getCompound( "GlassAreaH" ) ) ) );
    transmitterWorld = Dimensiown.SERVER.getWorld( DimId.getWorldId( compoundTag, "WorldId", transmitterWorld.isClient ) );
  }

  @Override
  protected void writeCustomDataToTag( CompoundTag compoundTag )
  {
    super.writeCustomDataToTag( compoundTag );
    compoundTag.put( "PhotonTransmitterL", BoxUtils.vecToTag( transmitterArea.l ) );
    compoundTag.put( "PhotonTransmitterH", BoxUtils.vecToTag( transmitterArea.h ) );

    compoundTag.put( "GlassL", BoxUtils.vecToTag( glassArea.l ) );
    compoundTag.put( "GlassH", BoxUtils.vecToTag( glassArea.h ) );

    DimId.putWorldId( compoundTag, "WorldId", transmitterWorld.getRegistryKey() );

  }

  private void checkIntegrity()
  {

    boolean glassValid = glassArea.fastStream().allMatch(
      blockPos -> world.getBlockState( blockPos ).getBlock() instanceof AbstractGlassBlock );
  }

  @Override
  public void tick()
  {
    super.tick();
    if ( !world.isClient )
    {
      if ( world.getTime() % 10 == getEntityId() % 10 )
      {
        checkIntegrity();
      }
    }

  }
}
