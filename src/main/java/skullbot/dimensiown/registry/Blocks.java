package skullbot.dimensiown.registry;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.blocks.DimensionalDoorBlock;

public class Blocks
{
  public static Block DIM_BLOCK_UNBREAKABLE = new Block( FabricBlockSettings.copy( net.minecraft.block.Blocks.BEDROCK  ).build() );
  public static Block DIM_BLOCK             = new Block( FabricBlockSettings.copy( net.minecraft.block.Blocks.OBSIDIAN ).build() );

  public static DimensionalDoorBlock DIM_DOOR_UNBREAKABLE = new DimensionalDoorBlock( FabricBlockSettings.copy( net.minecraft.block.Blocks.IRON_DOOR ).strength( -1.0F, 3600000.0F ).sounds( BlockSoundGroup.STONE ).build() );
  public static DimensionalDoorBlock DIM_DOOR             = new DimensionalDoorBlock( FabricBlockSettings.copy( net.minecraft.block.Blocks.IRON_DOOR ).sounds( BlockSoundGroup.STONE ).build() );

  public static void init()
  {
    Registry.register( Registry.BLOCK, new Identifier( Dimensiown.MOD_ID, "dimensional_block_unbreakable" ),  DIM_BLOCK_UNBREAKABLE );
    Registry.register( Registry.BLOCK, new Identifier( Dimensiown.MOD_ID, "dimensional_block" ),              DIM_BLOCK );
    Registry.register( Registry.BLOCK, new Identifier( Dimensiown.MOD_ID, "dimensional_door_unbreakable" ),   DIM_DOOR_UNBREAKABLE );
    Registry.register( Registry.BLOCK, new Identifier( Dimensiown.MOD_ID, "dimensional_door" ),               DIM_DOOR );
  }
}
