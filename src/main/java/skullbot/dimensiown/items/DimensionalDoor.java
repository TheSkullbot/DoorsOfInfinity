package skullbot.dimensiown.items;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TallBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import skullbot.dimensiown.Dimensiown;

import java.util.List;

public class DimensionalDoor extends TallBlockItem
{
  public DimensionalDoor( Block block, Settings settings )
  {
    super( block, settings );
  }

  @Override
  public void appendTooltip( ItemStack stack, World world, List<Text> tooltip, TooltipContext context )
  {
    CompoundTag blockEntityTag = stack.getSubTag( "BlockEntityTag" );
    if( ( blockEntityTag != null ) && !blockEntityTag.isEmpty() )
    {
      tooltip.add( new TranslatableText( "text." + Dimensiown.MOD_ID + ".dimensional_offset",   blockEntityTag.getInt( "DimensionalOffset"   ) ).formatted( Formatting.AQUA ) );
      tooltip.add( new TranslatableText( "text." + Dimensiown.MOD_ID + ".dimensional_upgrades", blockEntityTag.getInt( "DimensionalUpgrades" ) ).formatted( Formatting.GRAY ) );
    }
    super.appendTooltip( stack, world, tooltip, context );
  }
}
