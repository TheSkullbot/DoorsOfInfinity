package skullbot.dimensiown;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import skullbot.dimensiown.registry.*;

public class Dimensiown implements ModInitializer
{
  public static final String MOD_ID = "dimensiown";

  public static MinecraftServer SERVER;

  public static boolean immersivePortalsPresent;

  @Override
  public void onInitialize()
  {
    immersivePortalsPresent = true;

    ServerLifecycleEvents.SERVER_STARTED.register( server -> SERVER = server );

    Entities.init();
    Blocks.init();
    BlockEntities.init();
    Items.init();
  }
}
