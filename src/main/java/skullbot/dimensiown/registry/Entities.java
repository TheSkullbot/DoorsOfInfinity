package skullbot.dimensiown.registry;

import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import skullbot.dimensiown.Dimensiown;
import skullbot.dimensiown.entities.DimensionalPortal;

public class Entities
{
  public static EntityType<DimensionalPortal> DIM_PORTAL;

  public static void init()
  {
    DIM_PORTAL = Registry.register( Registry.ENTITY_TYPE, new Identifier( Dimensiown.MOD_ID, "dimensional_portal" ), FabricEntityTypeBuilder.create(
      SpawnGroup.MISC,
      DimensionalPortal::new
    ).size( EntityDimensions.fixed( 1, 1 ) ).setImmuneToFire().build() );
  }
}
