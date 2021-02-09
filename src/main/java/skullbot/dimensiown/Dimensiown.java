package skullbot.dimensiown;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import skullbot.dimensiown.registry.*;

public class Dimensiown implements ModInitializer
{
  public static final String MOD_ID = "dimensiown";
  public static final Marker MARKER = MarkerManager.getMarker( MOD_ID );
  public static final Logger LOGGER = LogManager.getLogger();

  public static MinecraftServer SERVER;


  @Override
  public void onInitialize()
  {
    ServerLifecycleEvents.SERVER_STARTING.register( server -> SERVER = server );

    Entities.init();
    Blocks.init();
    BlockEntities.init();
    Items.init();
  }
}

// TODO : FIX Ownership message spam
// TODO : Find a way to remove
