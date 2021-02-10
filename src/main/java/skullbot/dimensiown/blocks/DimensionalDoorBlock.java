package skullbot.dimensiown.blocks;

import com.qouteall.immersive_portals.dimension_sync.DimId;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;
import skullbot.dimensiown.helpers.DimensionalHelper;
import skullbot.dimensiown.helpers.PersonalDimension;
import skullbot.dimensiown.registry.Dimensions;

import java.util.Objects;
import java.util.function.Predicate;

import static skullbot.dimensiown.registry.Blocks.DIM_BLOCK;
import static skullbot.dimensiown.registry.Blocks.DIM_BLOCK_UNBREAKABLE;

public class DimensionalDoorBlock extends Block implements BlockEntityProvider
{
  public static final    DirectionProperty             FACING;
  public static final    BooleanProperty               OPEN;
  public static final    EnumProperty<DoorHinge>       HINGE;
  public static final    EnumProperty<DoubleBlockHalf> HALF;
  protected static final VoxelShape                    NORTH_SHAPE;
  protected static final VoxelShape                    SOUTH_SHAPE;
  protected static final VoxelShape                    EAST_SHAPE;
  protected static final VoxelShape                    WEST_SHAPE;

  //============================================================================
  // Block properties
  //============================================================================

  protected void appendProperties( StateManager.Builder<Block, BlockState> builder )
  {
    builder.add( HALF, FACING, OPEN, HINGE );
  }

  static
  {
    FACING      = HorizontalFacingBlock.FACING;
    OPEN        = Properties.OPEN;
    HINGE       = Properties.DOOR_HINGE;
    HALF        = Properties.DOUBLE_BLOCK_HALF;
    NORTH_SHAPE = Block.createCuboidShape( 0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D );
    SOUTH_SHAPE = Block.createCuboidShape( 0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D );
    EAST_SHAPE  = Block.createCuboidShape( 13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D );
    WEST_SHAPE  = Block.createCuboidShape( 0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D );
  }

  public DimensionalDoorBlock( Settings settings )
  {
    super( settings );
    this.setDefaultState( this.stateManager.getDefaultState().with( FACING, Direction.NORTH ).with( OPEN, false ).with( HINGE, DoorHinge.LEFT ).with( HALF, DoubleBlockHalf.LOWER ) );
  }

  //============================================================================
  // Block placing
  //============================================================================

  public BlockState getPlacementState( ItemPlacementContext ctx )
  {
    BlockPos blockPos = ctx.getBlockPos();

    // Prevent door placing in anything else than overworld
    if( ctx.getWorld().getRegistryKey().getValue() != World.OVERWORLD.getValue() )
    {
      // Prevent double message from both client and server
      if( !ctx.getWorld().isClient() )
        ctx.getPlayer().sendSystemMessage( new LiteralText( "You can only place dimensional doors in the overworld."), Util.NIL_UUID );

      return null;
    }

    if( blockPos.getY() < 255 && ctx.getWorld().getBlockState( blockPos.up() ).canReplace( ctx ) )
    {
      World   world = ctx.getWorld();
      boolean bl    = world.isReceivingRedstonePower( blockPos ) || world.isReceivingRedstonePower( blockPos.up() );
      return this.getDefaultState().with( FACING, ctx.getPlayerFacing() ).with( HINGE, this.getHinge( ctx ) ).with( OPEN, bl ).with( HALF, DoubleBlockHalf.LOWER );
    }
    else
      return null;
  }

  public void onPlaced( World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack )
  {
    world.setBlockState( pos.up(), state.with( HALF, DoubleBlockHalf.UPPER ), 3 );

    if( state.get( HALF ) == DoubleBlockHalf.LOWER && !world.isClient )
    {
      if( itemStack != null && itemStack.getSubTag( "BlockEntity" ) != null )
      {
        DimensionalDoorBlockEntity blockEntity = new DimensionalDoorBlockEntity();
        blockEntity.updateDestination();
      }

      DimensionalDoorBlockEntity blockEntity = (DimensionalDoorBlockEntity) world.getBlockEntity( pos );
      PersonalDimension          personalDim = blockEntity.getOrCreateLinkedDimension( placer.getUuid() );
      blockEntity.setOwner( placer.getUuid() );
      blockEntity.placeSyncedDoor( DimensionalHelper.getDimension(), personalDim.getDoorBlockPosition() );
    }
  }

  public void neighborUpdate( BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean moved )
  {
    pos   = state.get( HALF ) == DoubleBlockHalf.LOWER ? pos : pos.down();
    state = world.getBlockState( pos );

    // Prevent door placing in same dimension
    if( world.getRegistryKey().getValue() == Dimensions.DIMENSION_WORLD.getValue() )
      return;

    DimensionalDoorBlockEntity blockEntity = (DimensionalDoorBlockEntity) world.getBlockEntity( pos );

    if( !state.isAir() && !canPlaceAt( state, world, pos ) )
    {
      blockEntity.deleteLocalPortal();
      world.setBlockState( pos.up(), Blocks.AIR.getDefaultState(), 35 );
      world.breakBlock( pos, true );
    }
  }

  public BlockState getStateForNeighborUpdate( BlockState state, Direction facing, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos )
  {
    DoubleBlockHalf doubleBlockHalf = state.get( HALF );
    if( facing.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == ( facing == Direction.UP ) )
      return neighborState.getBlock() == this && neighborState.get( HALF ) != doubleBlockHalf ? state.with( FACING, neighborState.get( FACING ) ).with( OPEN, neighborState.get( OPEN ) ).with( HINGE, neighborState.get( HINGE ) ) : Blocks.AIR.getDefaultState();
    else
      return doubleBlockHalf == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canPlaceAt( world, pos ) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate( state, facing, neighborState, world, pos, neighborPos );
  }

  //============================================================================
  // Block used
  //============================================================================

  @Override
  public ActionResult onUse( BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit )
  {
    BlockPos                   lowerPos    = state.get( HALF ) == DoubleBlockHalf.LOWER ? pos : pos.down();
    DimensionalDoorBlockEntity blockEntity = (DimensionalDoorBlockEntity) world.getBlockEntity( lowerPos );

    // Prevent door opening when trying to upgrade
    if( Objects.equals( player.getItemsHand().iterator().next().getTranslationKey(), "item.dimensiown.dimensional_enhancer" ) )
      return ActionResult.FAIL;

    // Only owner can open the door
    if( !Objects.equals( blockEntity.getOwner(), player.getUuid() ) )
    {
      // Prevent double message from both client and server
//      if( !world.isClient() )
//        player.sendSystemMessage( new LiteralText( "You can only open your own dimensional doors."), Util.NIL_UUID );
      // TODO : Fix double message

      return ActionResult.FAIL;
    }

    state = state.cycle( OPEN );
    world.setBlockState( pos, state, 10 );
    world.syncWorldEvent( player, state.get( OPEN ) ? this.getCloseSoundEventId() : this.getOpenSoundEventId(), pos, 0 );

    if( !world.isClient )
      blockEntity.updateDestination();

    return ActionResult.SUCCESS;
  }

  //============================================================================
  // Block Placing checks
  //============================================================================

  public boolean canPlaceAt( BlockState state, WorldView world, BlockPos pos )
  {
    pos = state.get( HALF ) == DoubleBlockHalf.LOWER ? pos : pos.down();
    return topAndBottomMatch( pos, world ) && ( ( sideMatches( pos, world, Direction.NORTH ) && sideMatches( pos, world, Direction.SOUTH ) ) || ( sideMatches( pos, world, Direction.EAST ) && sideMatches( pos, world, Direction.WEST ) ) );
  }

  private boolean topAndBottomMatch( BlockPos pos, WorldView world )
  {
    Predicate<Block> matchingBlocks = ( block ) -> block == DIM_BLOCK || block == DIM_BLOCK_UNBREAKABLE;
    return matchingBlocks.test( world.getBlockState( pos.down() ).getBlock() ) && matchingBlocks.test( world.getBlockState( pos.up( 2 ) ).getBlock() );
  }

  private boolean sideMatches( BlockPos pos, WorldView world, Direction d )
  {
    Predicate<Block> matchingBlocks = ( block ) -> block == DIM_BLOCK || block == DIM_BLOCK_UNBREAKABLE;
    BlockPos         sidePos        = pos.add( d.getVector() );
    return matchingBlocks.test( world.getBlockState( sidePos ).getBlock() ) && matchingBlocks.test( world.getBlockState( sidePos.up() ).getBlock() );
  }

  //============================================================================
  // Block Breaking
  //============================================================================

  public void onBreak( World world, BlockPos pos, BlockState state, PlayerEntity player )
  {
    if( world.getRegistryKey() == Dimensions.DIMENSION_WORLD )
      return;

    BlockPos                   lowerPos         = state.get( HALF ) == DoubleBlockHalf.LOWER ? pos : pos.down();
    BlockState                 lowerBlockState  = world.getBlockState( lowerPos );
    DoubleBlockHalf            doubleBlockHalf  = state.get( HALF );
    BlockPos                   otherPos         = doubleBlockHalf == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
    BlockState                 otherblockState  = world.getBlockState( otherPos );
    DimensionalDoorBlockEntity lowerBlockEntity = (DimensionalDoorBlockEntity) world.getBlockEntity( lowerPos );

    if( otherblockState.getBlock() == this && otherblockState.get( HALF ) != doubleBlockHalf )
    {
      world.syncWorldEvent( player, 2001, otherPos, Block.getRawIdFromState( otherblockState ) );

      if( !world.isClient && !player.isCreative() && player.isUsingEffectiveTool( otherblockState ) )
        Block.dropStacks( lowerBlockState, world, lowerPos, world.getBlockEntity( lowerPos ) );

      world.setBlockState( otherPos, Blocks.AIR.getDefaultState(), 35 );
    }

    if( !world.isClient )
      lowerBlockEntity.deleteLocalPortal();

    super.onBreak( world, pos, state, player );
  }

  public void afterBreak( World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack )
  {
    super.afterBreak( world, player, pos, Blocks.AIR.getDefaultState(), blockEntity, stack );
  }

  @Override
  public float calcBlockBreakingDelta( BlockState state, PlayerEntity player, BlockView world, BlockPos pos )
  {
    if( world instanceof World )
      if( ( (World) world ).getRegistryKey() == Dimensions.DIMENSION_WORLD )
        return 0;

    return super.calcBlockBreakingDelta( state, player, world, pos );
  }

  //============================================================================
  // Block sounds
  //============================================================================

  private int getOpenSoundEventId()
  {
    return this.material == Material.METAL ? 1011 : 1012;
  }

  private int getCloseSoundEventId()
  {
    return this.material == Material.METAL ? 1005 : 1006;
  }

  //============================================================================
  // Block rendering
  //============================================================================

  @Override
  public boolean canPathfindThrough( BlockState world, BlockView view, BlockPos pos, NavigationType env )
  {
    return true;
  }

  public PistonBehavior getPistonBehavior( BlockState state )
  {
    return PistonBehavior.BLOCK;
  }

  public BlockRenderType getRenderType( BlockState state )
  {
    return BlockRenderType.MODEL;
  }

  public BlockState rotate( BlockState state, BlockRotation rotation )
  {
    return state.with( FACING, rotation.rotate( state.get( FACING ) ) );
  }

  public BlockState mirror( BlockState state, BlockMirror mirror )
  {
    return mirror == BlockMirror.NONE ? state : state.rotate( mirror.getRotation( state.get( FACING ) ) ).cycle( HINGE );
  }

  private DoorHinge getHinge( ItemPlacementContext ctx )
  {
    BlockView  blockView   = ctx.getWorld();
    BlockPos   blockPos    = ctx.getBlockPos();
    Direction  direction   = ctx.getPlayerFacing();
    BlockPos   blockPos2   = blockPos.up();
    Direction  direction2  = direction.rotateYCounterclockwise();
    BlockPos   blockPos3   = blockPos.offset( direction2 );
    BlockState blockState  = blockView.getBlockState( blockPos3 );
    BlockPos   blockPos4   = blockPos2.offset( direction2 );
    BlockState blockState2 = blockView.getBlockState( blockPos4 );
    Direction  direction3  = direction.rotateYClockwise();
    BlockPos   blockPos5   = blockPos.offset( direction3 );
    BlockState blockState3 = blockView.getBlockState( blockPos5 );
    BlockPos   blockPos6   = blockPos2.offset( direction3 );
    BlockState blockState4 = blockView.getBlockState( blockPos6 );
    int        i           = ( blockState.isFullCube( blockView, blockPos3 ) ? -1 : 0 ) + ( blockState2.isFullCube( blockView, blockPos4 ) ? -1 : 0 ) + ( blockState3.isFullCube( blockView, blockPos5 ) ? 1 : 0 ) + ( blockState4.isFullCube( blockView, blockPos6 ) ? 1 : 0 );
    boolean    bl          = blockState.getBlock() == this && blockState.get( HALF ) == DoubleBlockHalf.LOWER;
    boolean    bl2         = blockState3.getBlock() == this && blockState3.get( HALF ) == DoubleBlockHalf.LOWER;

    if( ( !bl || bl2 ) && i <= 0 )
    {
      if( ( !bl2 || bl ) && i >= 0 )
      {
        int    j     = direction.getOffsetX();
        int    k     = direction.getOffsetZ();
        Vec3d  vec3d = ctx.getHitPos();
        double d     = vec3d.x - (double) blockPos.getX();
        double e     = vec3d.z - (double) blockPos.getZ();
        return ( j >= 0 || e >= 0.5D ) && ( j <= 0 || e <= 0.5D ) && ( k >= 0 || d <= 0.5D ) && ( k <= 0 || d >= 0.5D ) ? DoorHinge.LEFT : DoorHinge.RIGHT;
      }
      else
        return DoorHinge.LEFT;
    }
    else
      return DoorHinge.RIGHT;
  }

  public VoxelShape getOutlineShape( BlockState state, BlockView view, BlockPos pos, ShapeContext context )
  {
    Direction direction = state.get( FACING );
    boolean   bl        = !( state.get( OPEN ) );
    boolean   bl2       = state.get( HINGE ) == DoorHinge.RIGHT;
    switch( direction )
    {
      case EAST:
      default:
        return bl ? WEST_SHAPE : ( bl2 ? SOUTH_SHAPE : NORTH_SHAPE );
      case SOUTH:
        return bl ? NORTH_SHAPE : ( bl2 ? WEST_SHAPE : EAST_SHAPE );
      case WEST:
        return bl ? EAST_SHAPE : ( bl2 ? NORTH_SHAPE : SOUTH_SHAPE );
      case NORTH:
        return bl ? SOUTH_SHAPE : ( bl2 ? EAST_SHAPE : WEST_SHAPE );
    }
  }

  @Environment( EnvType.CLIENT )
  public long getRenderingSeed( BlockState state, BlockPos pos )
  {
    return MathHelper.hashCode( pos.getX(), pos.down( state.get( HALF ) == DoubleBlockHalf.LOWER ? 0 : 1 ).getY(), pos.getZ() );
  }

  //============================================================================
  // Block entity
  //============================================================================

  @Override
  public BlockEntity createBlockEntity( BlockView view )
  {
    return new DimensionalDoorBlockEntity();
  }
}
