package skullbot.dimensiown.helpers;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.registry.Dimensions;

import java.util.UUID;

public class DimensionalHelper
{
  public static PersonalDimension getEmptyPersonalDimension( UUID playerID )
  {
    ServerWorld world   = Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
    BlockPos    basePos = new BlockPos( 0, PersonalDimension.WALL_THICKNESS, 0 );
    int         offset  = 0;

    while( !world.getBlockState( basePos.add( offset * PersonalDimension.SPACING, PersonalDimension.WALL_THICKNESS, 0 ) ).isAir() )
      offset++;

    return new PersonalDimension( offset, playerID, world );
  }

  public static PersonalDimension getPersonalDimension( int id, UUID playerID, int upgrades )
  {
    ServerWorld world = Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
    return new PersonalDimension( id, playerID, upgrades, world );
  }

  public PersonalDimension getPersonalDimensionAt( BlockPos pos, UUID playerID )
  {
    return new PersonalDimension( pos.getX() / PersonalDimension.SPACING, playerID, Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD ) );
  }

  public static ServerWorld getDimension()
  {
    return Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
  }
}
