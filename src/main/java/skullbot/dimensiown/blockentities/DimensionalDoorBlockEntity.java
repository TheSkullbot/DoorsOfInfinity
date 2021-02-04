package skullbot.dimensiown.blockentities;

import com.qouteall.immersive_portals.dimension_sync.DimId;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.registry.*;
import skullbot.dimensiown.helpers.DimensionalHelper;
import skullbot.dimensiown.helpers.DimensionalHelper.*;
import skullbot.dimensiown.helpers.PortalCreationHelper;
import skullbot.dimensiown.blocks.DimensionalDoorBlock;
import skullbot.dimensiown.utils.BoxUtils;

public class DimensionalDoorBlockEntity extends BlockEntity implements BlockEntityClientSerializable
{
  public static final int MAX_UPGRADES = 4;

  public DimensionalHelper.PersonalDimension destDimension;
  public BlockPos                            destPosition;
  public World             destWorld;
  public Portal            portal;
  public int               upgrades = 0;

  public DimensionalDoorBlockEntity()
  {
    super( BlockEntities.DIM_DOOR );
  }

  public void syncWith( DimensionalDoorBlockEntity entity )
  {
    entity.destPosition = this.pos;
    entity.destWorld    = this.world;
    this.destPosition   = entity.pos;
    this.destWorld      = entity.world;
    this.upgrades       = entity.upgrades = Math.max( entity.upgrades, this.upgrades );
    this.sync();
    entity.sync();
  }

  public void updateDestination()
  {
    if( isSyncPresent() )
    {
      destWorld.setBlockState( destPosition, getSyncEntity().getWorld().getBlockState( getSyncEntity().getPos() ).with( DimensionalDoorBlock.HINGE, getCachedState().get( DimensionalDoorBlock.HINGE ) ).with( DimensionalDoorBlock.OPEN, getCachedState().get( DimensionalDoorBlock.OPEN ) ), 10 );

      if( world.getRegistryKey() == Dimensions.DIMENSION_WORLD && world.getEntitiesByClass( Portal.class, BoxUtils.getBoxInclusive( pos, pos.up() ), null ).isEmpty() )
      {
        deleteSyncPortal();
        PortalManipulation.completeBiWayPortal( getSyncEntity().portal, Portal.entityType );
      }
    }

  }

  public boolean isSyncPresent()
  {
    return destPosition != null && destWorld != null && !destWorld.getBlockState( destPosition ).isAir();
  }

  public void deleteLocalPortal()
  {
    deletePortals( world, pos );
  }

  public void deleteSyncPortal()
  {
    deletePortals( destWorld, destPosition );
  }

  private void deletePortals( World world, BlockPos pos )
  {
    world.getEntitiesByClass( Portal.class, BoxUtils.getBoxInclusive( pos, pos.up() ), null ).forEach( Portal::remove );
  }

  private void createSyncedPortals()
  {
    Direction  direction      = getCachedState().get( DimensionalDoorBlock.FACING );
    Direction  rightDirection = Direction.fromHorizontal( direction.getHorizontal() + 1 );
    Vec3d      portalPos      = new Vec3d( pos.getX(), pos.getY(), pos.getZ() ).add( 0.5, 1, 0.5 );
    Quaternion rot            = new Quaternion( Vector3f.POSITIVE_Y, direction.getOpposite().getHorizontal() * 90, true );

    PersonalDimension personalDim = getOrCreateLinkedDimension();

    deleteSyncPortal();
    portal = PortalCreationHelper.spawn( world, portalPos, 1, 2, rightDirection, Dimensions.DIMENSION_WORLD, personalDim.getPlayerPosCentered().add( 0, 1, 0 ), true, rot );
    updateDestination();
  }

  public void placeSyncedDoor( World destinationWorld, BlockPos destinationPosition )
  {
    BlockState state = getCachedState();
    destinationWorld.setBlockState( destinationPosition,      Blocks.DIM_DOOR.getDefaultState().with( DimensionalDoorBlock.HINGE, state.get( DimensionalDoorBlock.HINGE ) ).with( DimensionalDoorBlock.FACING, Direction.NORTH ).with( DimensionalDoorBlock.HALF, DoubleBlockHalf.LOWER ) );
    destinationWorld.setBlockState( destinationPosition.up(), Blocks.DIM_DOOR.getDefaultState().with( DimensionalDoorBlock.HINGE, state.get( DimensionalDoorBlock.HINGE ) ).with( DimensionalDoorBlock.FACING, Direction.NORTH ).with( DimensionalDoorBlock.HALF, DoubleBlockHalf.UPPER ) );
    syncWith( (DimensionalDoorBlockEntity) destinationWorld.getBlockEntity( destinationPosition ) );
    createSyncedPortals();
  }

  @Override
  public void fromClientTag( CompoundTag tag )
  {
    upgrades = tag.getInt( "DimensionalOffset" );
  }

  @Override
  public CompoundTag toClientTag( CompoundTag tag )
  {
    tag.putInt( "DimensionalOffset", upgrades );
    return tag;
  }

  @Override
  public void sync()
  {
    markDirty();
    BlockEntityClientSerializable.super.sync();
  }

  public void syncWithDoor()
  {
    syncWith( getSyncEntity() );
  }

  public DimensionalDoorBlockEntity getSyncEntity()
  {
    if( destWorld == null )
      return null;

    return (DimensionalDoorBlockEntity) destWorld.getBlockEntity( destPosition );
  }

  public PersonalDimension getOrCreateLinkedDimension()
  {
    if( destDimension == null )
    {
      destDimension = DimensionalHelper.getEmptyPersonalDimension();
      destDimension.generate();
    }
    return destDimension;
  }

  @Override
  public void fromTag( BlockState state, CompoundTag tag )
  {
    if( tag.contains( "DestinationDimId" ) )
    {
      destWorld    = Dimensiown.SERVER.getWorld( DimId.idToKey( tag.getString( "DestinationDimId" ) ) );
      destPosition = new BlockPos( tag.getInt( "DestinationX" ), tag.getInt( "DestinationY" ), tag.getInt( "DestinationZ" ) );
    }

    super.fromTag( state, tag );

    if( tag.contains( "DimensionalOffset" ) ) upgrades = tag.getInt( "DimensionalOffset" );

    if( tag.contains( "DimensionalOffset" ) ) destDimension = DimensionalHelper.getPersonalDimension( tag.getInt( "DimensionalOffset" ), upgrades );

    super.fromTag( state, tag );
  }

  @Override
  public CompoundTag toTag( CompoundTag tag )
  {
    if( destDimension != null ) tag.putInt( "DimensionalOffset", destDimension.getDimensionOffset() );

    if( destWorld != null )
    {
      tag.putString( "DestinationDimId", destWorld.getRegistryKey().getValue().toString() );
      tag.putInt( "DestinationX", destPosition.getX() );
      tag.putInt( "DestinationY", destPosition.getY() );
      tag.putInt( "DestinationZ", destPosition.getZ() );
    }

    tag.putInt( "DimensionalOffset", upgrades );

    return super.toTag( tag );
  }
}
