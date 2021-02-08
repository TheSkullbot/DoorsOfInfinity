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
import skullbot.dimensiown.helpers.PersonalDimension;
import skullbot.dimensiown.registry.*;
import skullbot.dimensiown.helpers.DimensionalHelper;
import skullbot.dimensiown.helpers.PortalCreationHelper;
import skullbot.dimensiown.blocks.DimensionalDoorBlock;
import skullbot.dimensiown.utils.BoxUtils;

import java.util.UUID;

public class DimensionalDoorBlockEntity extends BlockEntity implements BlockEntityClientSerializable
{
  protected UUID   ownerID;
  protected String ownerName;

  public PersonalDimension destDimension;
  public BlockPos          destPosition;
  public World             destWorld;
  public Portal            portal;
  public int               upgrades = 0;

  public DimensionalDoorBlockEntity()
  {
    super( BlockEntities.DIM_DOOR );
  }

  public void setOwner( UUID p_owner )
  {
    this.ownerID   = p_owner;
    this.ownerName = Dimensiown.SERVER.getPlayerManager().getPlayer( p_owner ).getName().asString();
    this.sync();
  }

  public UUID getOwner()
  {
    return this.ownerID;
  }

  public String getOwnerName()
  {
    return this.ownerName;
  }

  public void syncWith( DimensionalDoorBlockEntity entity )
  {
    entity.setOwner( this.ownerID );
    this.setOwner( entity.ownerID );

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

    PersonalDimension personalDim = getOrCreateLinkedDimension( ownerID );

    deleteSyncPortal();
    portal = PortalCreationHelper.spawn( world, portalPos, 1, 2, rightDirection, Dimensions.DIMENSION_WORLD, personalDim.getPlayerPosCentered().add( 0, 1, 0 ), true, rot );
    updateDestination();
  }

  public void placeSyncedDoor( World destinationWorld, BlockPos destinationPosition )
  {
    BlockState state = getCachedState();
    destinationWorld.setBlockState( destinationPosition, Blocks.DIM_DOOR.getDefaultState().with( DimensionalDoorBlock.HINGE, state.get( DimensionalDoorBlock.HINGE ) ).with( DimensionalDoorBlock.FACING, Direction.NORTH ).with( DimensionalDoorBlock.HALF, DoubleBlockHalf.LOWER ) );
    destinationWorld.setBlockState( destinationPosition.up(), Blocks.DIM_DOOR.getDefaultState().with( DimensionalDoorBlock.HINGE, state.get( DimensionalDoorBlock.HINGE ) ).with( DimensionalDoorBlock.FACING, Direction.NORTH ).with( DimensionalDoorBlock.HALF, DoubleBlockHalf.UPPER ) );
    syncWith( (DimensionalDoorBlockEntity) destinationWorld.getBlockEntity( destinationPosition ) );
    // TODO : Sync owner id with distant door
    createSyncedPortals();
  }

  public DimensionalDoorBlockEntity getSyncEntity()
  {
    if( destWorld == null )
      return null;

    return (DimensionalDoorBlockEntity) destWorld.getBlockEntity( destPosition );
  }

  public PersonalDimension getOrCreateLinkedDimension( UUID ownerID )
  {
    if( destDimension == null )
    {
      destDimension = DimensionalHelper.getEmptyPersonalDimension( ownerID );
      destDimension.generate();
    }
    return destDimension;
  }

  //============================================================================
  // CLIENT TAGS
  //============================================================================

  @Override
  public void fromClientTag( CompoundTag tag )
  {
    if( tag.contains( "DimensionalUpgrades" ) )
      upgrades = tag.getInt( "DimensionalUpgrades" );

    if( tag.contains( "DimensionOwner" ) )
    {
      ownerID   = tag.getUuid( "DimensionOwner" );
      ownerName = tag.getString( "DimensionOwnerName" );
    }
  }

  @Override
  public CompoundTag toClientTag( CompoundTag tag )
  {
    tag.putInt( "DimensionalUpgrades", upgrades );

    if( ownerID != null )
    {
      tag.putUuid( "DimensionOwner", ownerID );
      tag.putString( "DimensionOwnerName", ownerName );
    }

    return tag;
  }

  @Override
  public void sync()
  {
    markDirty();
    BlockEntityClientSerializable.super.sync();
  }

  //============================================================================
  // SERVER TAGS
  //============================================================================

  @Override
  public void fromTag( BlockState state, CompoundTag tag )
  {
    super.fromTag( state, tag );

    if( tag.contains( "DimensionOwner" ) )
    {
      ownerID   = tag.getUuid( "DimensionOwner" );
      ownerName = tag.getString( "DimensionOwnerName" );
    }

    if( tag.contains( "DestinationDimId" ) && Dimensiown.SERVER != null )
    {
      destWorld    = Dimensiown.SERVER.getWorld( DimId.idToKey( tag.getString( "DestinationDimId" ) ) );
      destPosition = new BlockPos( tag.getInt( "DestinationX" ), tag.getInt( "DestinationY" ), tag.getInt( "DestinationZ" ) );
    }

    if( tag.contains( "DimensionalUpgrades" ) )
      upgrades = tag.getInt( "DimensionalUpgrades" );

    if( tag.contains( "DimensionalOffset" ) )
      destDimension = DimensionalHelper.getPersonalDimension( tag.getInt( "DimensionalOffset" ), ownerID, upgrades );
  }

  @Override
  public CompoundTag toTag( CompoundTag tag )
  {
    super.toTag( tag );

    if( ownerID != null )
    {
      tag.putUuid( "DimensionOwner", ownerID );
      tag.putString( "DimensionOwnerName", ownerName );
    }

    if( destDimension != null )
      tag.putInt( "DimensionalOffset", destDimension.getDimensionOffset() );

    if( destWorld != null )
    {
      tag.putString( "DestinationDimId", destWorld.getRegistryKey().getValue().toString() );
      tag.putInt( "DestinationX", destPosition.getX() );
      tag.putInt( "DestinationY", destPosition.getY() );
      tag.putInt( "DestinationZ", destPosition.getZ() );
    }

    tag.putInt( "DimensionalUpgrades", upgrades );
    return tag;
  }
}
