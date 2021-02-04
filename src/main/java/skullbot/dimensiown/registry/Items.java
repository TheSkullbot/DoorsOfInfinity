package skullbot.dimensiown.registry;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.items.DimensionalDoor;
import skullbot.dimensiown.items.DimensionalEnhancer;

import java.util.function.BiFunction;

public class Items
{
  public static Item DIM_BLOCK_UNBREAKABLE;
  public static Item DIM_BLOCK;
  public static Item DIM_DOOR_UNBREAKABLE;
  public static Item DIM_DOOR;
  public static Item DIM_ENHANCER;

  public static final ItemGroup ITEMGROUP = FabricItemGroupBuilder.build( new Identifier( Dimensiown.MOD_ID, "items" ), () -> new ItemStack( DIM_DOOR ) );

  public static void init()
  {
    DIM_DOOR_UNBREAKABLE  = registerBlockItem( Blocks.DIM_DOOR_UNBREAKABLE,  new Item.Settings(), DimensionalDoor::new );
    DIM_BLOCK_UNBREAKABLE = registerBlockItem( Blocks.DIM_BLOCK_UNBREAKABLE, new Item.Settings(), BlockItem::new );

    DIM_DOOR  = registerBlockItem( Blocks.DIM_DOOR,  new Item.Settings().group( ITEMGROUP ).maxCount( 1 ), DimensionalDoor::new );
    DIM_BLOCK = registerBlockItem( Blocks.DIM_BLOCK, new Item.Settings().group( ITEMGROUP ).maxCount( 8 ), BlockItem::new );

    DIM_ENHANCER = registerItem( "dimensional_enhancer", new DimensionalEnhancer( new Item.Settings().group( ITEMGROUP ).maxCount( 1 ) ) );
  }

  public static <T extends Item> T registerItem( String name, T item )
  {
    return Registry.register( Registry.ITEM, new Identifier( Dimensiown.MOD_ID, name ), item );
  }

  public static <T extends Item> T registerBlockItem( Block block, Item.Settings itemSettings, BiFunction<Block, Item.Settings, T> itemFactory )
  {
    T item = itemFactory.apply( block, itemSettings );
    return registerItem( Registry.BLOCK.getId( block ).getPath(), item );
  }

}
