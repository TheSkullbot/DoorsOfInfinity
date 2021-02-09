package skullbot.dimensiown.helpers;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;
import skullbot.dimensiown.registry.Dimensions;

import java.util.UUID;

public class DimensionalHelper
{
  public static PersonalDimension getEmptyPersonalDimension( UUID placer )
  {
    ServerWorld world        = Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
    BlockPos    doorPosition = new BlockPos( 0, PersonalDimension.WALL_THICKNESS + PersonalDimension.HEIGHT, -( PersonalDimension.WALL_THICKNESS / 2.0f ) );

    for( int offset = 0 ; ; offset++ )
    {
      DimensionalDoorBlockEntity doorEntity = (DimensionalDoorBlockEntity) world.getBlockEntity( doorPosition.add( offset * PersonalDimension.SPACING, 0, 0 ) );

      // Create new dimension
      if( world.getBlockState( doorPosition.add( offset * PersonalDimension.SPACING, 0, 0 ) ).isAir() )
      {
        PersonalDimension dim = new PersonalDimension( offset, world );
        dim.generate();
        return dim;
      }
      // Search for existing dimension owner
      else if( doorEntity != null && doorEntity.getOwner() == placer )
      {
        return new PersonalDimension( offset, world );
      }

      Dimensiown.LOGGER.info( "No available dimension found at offset : " + offset );
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
