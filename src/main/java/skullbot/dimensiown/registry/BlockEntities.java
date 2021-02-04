package skullbot.dimensiown.registry;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;

public class BlockEntities
{
  public static BlockEntityType<DimensionalDoorBlockEntity> DIM_DOOR;

  public static void init()
  {
    DIM_DOOR = Registry.register( Registry.BLOCK_ENTITY_TYPE, new Identifier( Dimensiown.MOD_ID, "dimensional_door" ), BlockEntityType.Builder.create(
      DimensionalDoorBlockEntity::new,
      Blocks.DIM_DOOR_UNBREAKABLE,
      Blocks.DIM_DOOR
    ).build( null ) );
  }
}
