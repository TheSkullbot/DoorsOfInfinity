package skullbot.dimensiown.helpers;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;
import skullbot.dimensiown.registry.Dimensions;

import java.util.function.Function;
import java.util.function.IntPredicate;

import static skullbot.dimensiown.registry.Blocks.DIM_BLOCK_UNBREAKABLE;

public class DimensionalHelper
{
  private static final int START_HEIGHT            = 2;
  private static final int INNER_SIZE              = 5;
  private static final int WALL_THICKNESS          = 2;
  private static final int UPGRADE_SIZE_MULTIPLIER = 2;

  public static PersonalDimension getEmptyPersonalDimension()
  {
    ServerWorld infinityDim = Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
    BlockPos    basePos     = new BlockPos( 0, START_HEIGHT, 0 );
    int         offset      = 0;

    while( !infinityDim.getBlockState( basePos.add( offset * 200, 200, 0 ) ).isAir() )
    {
      offset++;
    }
    return new PersonalDimension( offset, infinityDim );
  }

  public static PersonalDimension getPersonalDimension( int id, int upgrades )
  {
    ServerWorld infinityDim = Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
    return new PersonalDimension( id, upgrades, infinityDim );
  }

  public PersonalDimension getPersonalDimensionAt( BlockPos pos )
  {
    return new PersonalDimension( pos.getX() / 200, Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD ) );
  }

  public static ServerWorld getDimension()
  {
    return Dimensiown.SERVER.getWorld( Dimensions.DIMENSION_WORLD );
  }

  public static class PersonalDimension
  {

    private int         dimId;
    private ServerWorld world;
    private int         upgrades = 0;

    public PersonalDimension( int dimOffset, int upgrades, ServerWorld world )
    {
      this( dimOffset, world );
      this.upgrades = upgrades;
    }

    public PersonalDimension( int dimOffset, ServerWorld world )
    {
      this.dimId = dimOffset;
      this.world = world;
    }

    public Vec3d getBasePosition()
    {
      return new Vec3d( 200 * dimId, START_HEIGHT, 0 );
    }

    public int getDimensionOffset()
    {
      return dimId;
    }

    public Vec3d getPlayerPosCentered()
    {
      return new Vec3d( getPlayerPos().getX(), getPlayerPos().getY(), getPlayerPos().getZ() ).add( 0.5, 0, 0.5 );
    }

    public int getInnerSize()
    {
      return INNER_SIZE + getUpgrades() * UPGRADE_SIZE_MULTIPLIER;
    }

    public int getUpgrades()
    {
      return upgrades;
    }

    public boolean upgrade()
    {
      if( this.upgrades >= UPGRADE_SIZE_MULTIPLIER )
      {
        return false;
      }
      int prevInnerSize = getInnerSize();

      DimensionalDoorBlockEntity linkedBlockEntity = getBlockEntity().getSyncEntity();

      linkedBlockEntity.deleteLocalPortal();
      linkedBlockEntity.deleteSyncPortal();


      linkedBlockEntity.upgrades++;
      upgrades = linkedBlockEntity.upgrades;

      generateCube( getBasePosition(), prevInnerSize, WALL_THICKNESS, vec ->
      {
        if( vec.getY() >= WALL_THICKNESS )
          return Blocks.AIR.getDefaultState();
        else
          return DIM_BLOCK_UNBREAKABLE.getDefaultState();
      } );


      generateCube( getBasePosition(), getInnerSize(), WALL_THICKNESS, vec ->
      {
        if( vec.getY() >= WALL_THICKNESS )
          return DIM_BLOCK_UNBREAKABLE.getDefaultState();
        else
          return DIM_BLOCK_UNBREAKABLE.getDefaultState();
      } );

      linkedBlockEntity.placeSyncedDoor( world, getPlayerPos() );
      return true;
    }

    private DimensionalDoorBlockEntity getBlockEntity()
    {
      return (DimensionalDoorBlockEntity) world.getBlockEntity( getPlayerPos() );
    }

    public BlockPos getPlayerPos()
    {
      return new BlockPos( getBasePosition().add( Math.floor( getInnerSize() / 2.0f ) + WALL_THICKNESS, START_HEIGHT, getInnerSize() + WALL_THICKNESS ) );
    }

    public void generate()
    {
      generateCube( getBasePosition(), getInnerSize(), WALL_THICKNESS, vec -> DIM_BLOCK_UNBREAKABLE.getDefaultState() );

      resetDoor();
    }

    private void resetDoor()
    {
      BlockPos spawnPos = getPlayerPos();
      world.setBlockState( spawnPos, Blocks.AIR.getDefaultState() );
      world.setBlockState( spawnPos.up(), Blocks.AIR.getDefaultState() );
    }

    public void generateCube( Vec3d basePosition, int innerSize, int wallThickness, Function<Vec3i, BlockState> stateFunction )
    {

      for( int i = 0; i < innerSize + wallThickness * 2; i++ )
      {
        for( int j = 0; j < innerSize + wallThickness * 2; j++ )
        {
          for( int k = 0; k < innerSize + wallThickness * 2; k++ )
          {
            BlockPos     pos     = new BlockPos( basePosition.x + i, basePosition.y + j, basePosition.z + k );
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

}
