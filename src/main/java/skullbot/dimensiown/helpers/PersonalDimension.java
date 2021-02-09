package skullbot.dimensiown.helpers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;

import java.util.function.Function;
import java.util.function.IntPredicate;

import static skullbot.dimensiown.registry.Blocks.DIM_BLOCK_UNBREAKABLE;

public class PersonalDimension
{
  public static final int HEIGHT                  = 64;
  public static final int SPACING                 = 150;
  public static final int INNER_SIZE              = 5;
  public static final int WALL_THICKNESS          = 2;
  public static final int UPGRADE_SIZE_MULTIPLIER = 2;

  private final ServerWorld world;
  private final int         offset;
  private       int         level = 0;

  public PersonalDimension( int offset, ServerWorld world )
  {
    this.offset = offset;
    this.world  = world;
  }

  public Vec3d getDoorPosition()
  {
    return new Vec3d( SPACING * offset, HEIGHT + WALL_THICKNESS, -(WALL_THICKNESS / 2.0f) );
  }

  public BlockPos getDoorBlockPosition()
  {
    return new BlockPos( getDoorPosition() );
  }

  public Vec3d getCubeOrigin()
  {
    return new Vec3d( Math.floor( ( SPACING * offset ) - ( getInnerSize() / 2.0f ) - Math.floor( WALL_THICKNESS / 2.0f ) ), HEIGHT, 0 );
  }

  public int getDimensionOffset()
  {
    return offset;
  }

  public Vec3d getPortalPosition()
  {
    return new Vec3d( getDoorPosition().getX(), getDoorPosition().getY(), getDoorPosition().getZ() ).add( 0.5, 1, 0.5 );
  }

  public int getInnerSize()
  {
    return INNER_SIZE + ( getUpgrades() * UPGRADE_SIZE_MULTIPLIER * INNER_SIZE );
  }

  public int getUpgrades()
  {
    return level;
  }

  public boolean canUpgrade()
  {
    return this.level < UPGRADE_SIZE_MULTIPLIER;
  }

  public boolean upgrade()
  {
    if( !canUpgrade() )
      return false;

    // TODO : Check if needed because door shouldn't change position
    // Remove portals
    DimensionalDoorBlockEntity linkedBlockEntity = getBlockEntity().getSyncEntity();
    linkedBlockEntity.deleteLocalPortal();
    linkedBlockEntity.deleteSyncPortal();

    // Replace previous cube with air
    generateCube( getCubeOrigin(), getInnerSize(), WALL_THICKNESS, vec -> vec.getY() >= WALL_THICKNESS ? Blocks.AIR.getDefaultState() : DIM_BLOCK_UNBREAKABLE.getDefaultState() );

    // Upgrade dimension
    linkedBlockEntity.upgrades++;
    level = linkedBlockEntity.upgrades;

    // Replace new cube with dimensional block
    generateCube( getCubeOrigin(), getInnerSize(), WALL_THICKNESS, vec -> DIM_BLOCK_UNBREAKABLE.getDefaultState() );

    // Place the door
    linkedBlockEntity.placeSyncedDoor( world, getDoorBlockPosition() );
    return true;
  }

  private DimensionalDoorBlockEntity getBlockEntity()
  {
    return (DimensionalDoorBlockEntity) world.getBlockEntity( getDoorBlockPosition() );
  }

  public void generate()
  {
    generateCube( getCubeOrigin(), getInnerSize(), WALL_THICKNESS, vec -> DIM_BLOCK_UNBREAKABLE.getDefaultState() );
    resetDoor();
  }

  private void resetDoor()
  {
    world.setBlockState( getDoorBlockPosition(),      Blocks.AIR.getDefaultState() );
    world.setBlockState( getDoorBlockPosition().up(), Blocks.AIR.getDefaultState() );
  }

  public void generateCube( Vec3d basePosition, int innerSize, int wallThickness, Function<Vec3i, BlockState> stateFunction )
  {
    for( int i = 0; i < innerSize + wallThickness * 2; i++ )
    {
      for( int j = 0; j < innerSize + wallThickness * 2; j++ )
      {
        for( int k = 0; k < innerSize + wallThickness * 2; k++ )
        {
          BlockPos     pos     = new BlockPos( basePosition.x + i, basePosition.y + j, -(basePosition.z + k) );
          IntPredicate allowed = ( a ) -> a <= wallThickness - 1 || a >= innerSize + wallThickness;
          if( allowed.test( i ) || allowed.test( j ) || allowed.test( k ) )
          {
            BlockState state = stateFunction.apply( new Vec3i( i, j, k ) );

            if( state != null )
              world.setBlockState( pos, state );
          }
        }
      }
    }
  }
}
