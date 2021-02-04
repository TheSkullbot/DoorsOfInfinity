package skullbot.dimensiown.items;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;
import skullbot.dimensiown.blocks.DimensionalDoorBlock;
import skullbot.dimensiown.registry.Blocks;

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
      if( blockEntity.getOrCreateLinkedDimension().upgrade() )
      {
        context.getPlayer().getMainHandStack().decrement( 1 );
        return ActionResult.SUCCESS;
      }
    }
    return ActionResult.PASS;
  }

}
