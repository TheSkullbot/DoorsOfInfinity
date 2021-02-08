package skullbot.dimensiown.items;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;
import skullbot.dimensiown.blocks.DimensionalDoorBlock;
import skullbot.dimensiown.registry.Blocks;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class DimensionalEnhancer extends Item
{
  public DimensionalEnhancer( Settings settings )
  {
    super( settings );
  }

  @Override
  public ActionResult useOnBlock( ItemUsageContext context )
  {
    BlockState state = context.getWorld().getBlockState( context.getBlockPos() );

    if( !context.getWorld().isClient && state.getBlock() == Blocks.DIM_DOOR && context.getHand() == Hand.MAIN_HAND )
    {
      BlockPos                   blockEntityPos = state.get( DimensionalDoorBlock.HALF ) == DoubleBlockHalf.LOWER ? context.getBlockPos() : context.getBlockPos().down();
      DimensionalDoorBlockEntity blockEntity    = (DimensionalDoorBlockEntity) context.getWorld().getBlockEntity( blockEntityPos );

      if( blockEntity.getOwner() != context.getPlayer().getUuid() )
      {
        String             userName  = context.getPlayer().getName().toString();
        String             ownerName = blockEntity.getOwnerName().toString();

        Dimensiown.LOGGER.info( Dimensiown.MARKER, userName + " tried to upgrade " + ownerName + "'s dimension." );
        return ActionResult.FAIL;
      }
      else if( blockEntity.getOrCreateLinkedDimension( blockEntity.getOwner() ).upgrade() )
      {
        context.getPlayer().getMainHandStack().decrement( 1 );
        return ActionResult.SUCCESS;
      }
    }
    return ActionResult.PASS;
  }

}
