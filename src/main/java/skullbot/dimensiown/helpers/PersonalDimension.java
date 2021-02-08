package skullbot.dimensiown.helpers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntPredicate;

import static skullbot.dimensiown.registry.Blocks.DIM_BLOCK_UNBREAKABLE;

public class PersonalDimension
{
  public static final int SPACING                 = 150;
  public static final int INNER_SIZE              = 5;
  public static final int WALL_THICKNESS          = 1;
  public static final int UPGRADE_SIZE_MULTIPLIER = 2;

  private UUID        owner;
  private ServerWorld world;
  private int         dimOffset;
  private int         upgrades = 0;

  public PersonalDimension( int dimOffset, UUID owner, int upgrades, ServerWorld world )
  {
    this( dimOffset, owner, world );
    this.upgrades = upgrades;
  }

  public PersonalDimension( int dimOffset, UUID owner, ServerWorld world )
  {
    this.dimOffset = dimOffset;
    this.world     = world;
    this.owner     = owner;
  }

  public Vec3d getBasePosition()
  {
    return new Vec3d( SPACING * dimOffset, WALL_THICKNESS, 0 );
  }

  public int getDimensionOffset()
  {
    return dimOffset;
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
      return false;

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
    return new BlockPos( getBasePosition().add( Math.floor( getInnerSize() / 2.0f ) + WALL_THICKNESS, WALL_THICKNESS, getInnerSize() + WALL_THICKNESS ) );
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
