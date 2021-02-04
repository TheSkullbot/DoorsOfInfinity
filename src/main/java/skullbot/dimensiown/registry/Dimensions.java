package skullbot.dimensiown.registry;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import skullbot.dimensiown.Dimensiown;

public class Dimensions
{
  // Loaded from datapack
  public static final RegistryKey<DimensionOptions> DIMENSION_KEY      = RegistryKey.of( Registry.DIMENSION_OPTIONS,  new Identifier( Dimensiown.MOD_ID, "empty" ) );
  public static final RegistryKey<DimensionType>    DIMENSION_TYPE_KEY = RegistryKey.of( Registry.DIMENSION_TYPE_KEY, new Identifier( Dimensiown.MOD_ID, "empty_type" ) );
  public static final RegistryKey<Biome>            DIMENSION_BIOME    = RegistryKey.of( Registry.BIOME_KEY,          new Identifier( Dimensiown.MOD_ID, "empty_biome" ) );
  public static final RegistryKey<World>            DIMENSION_WORLD    = RegistryKey.of( Registry.DIMENSION, DIMENSION_KEY.getValue() );
}
