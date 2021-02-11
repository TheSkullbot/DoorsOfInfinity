package skullbot.dimensiown.helpers;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.registry.Dimensions;
import skullbot.dimensiown.stores.DimensionInfo;
import skullbot.dimensiown.stores.PlayerDimensionManager;

import java.util.UUID;

public class DimensionalHelper
{
  public static PersonalDimension getEmptyPersonalDimension( UUID placer )
  {
    ServerWorld   world  = Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
    BlockPos      check  = new BlockPos( 0, PersonalDimension.HEIGHT, -( PersonalDimension.WALL_THICKNESS / 2.0f ) );
    DimensionInfo info   = PlayerDimensionManager.get().getInfo( Dimensiown.SERVER, placer );
    int           offset = info.getOffset();

    if( offset >= 0 )
    {
      Dimensiown.log( "Dimension #" + offset + " found for player " + placer );
      return new PersonalDimension( offset, world );
    }
    else
    {
      Dimensiown.log( "No dimension found for player " + placer + ", searching available space..." );

      for( offset = 0; ; offset++ )
      {
        // Create new dimension if not block is found at origin (Y : doorPosition - WALL_THICKNESS)
        if( world.getBlockState( check.add( offset * PersonalDimension.SPACING, 0, 0 ) ).isAir() )
        {
          Dimensiown.log( "Available dimension found at offset : " + offset );
          PersonalDimension dim = new PersonalDimension( offset, world );
          dim.generate();
          info.create( offset );
          return dim;
        }
        Dimensiown.log( "No available dimension at offset : " + offset );
      }
    }
  }

  public static PersonalDimension getPersonalDimension( int id )
  {
    return new PersonalDimension( id, Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD ) );
  }

  public static ServerWorld getDimension()
  {
    return Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
  }
}
