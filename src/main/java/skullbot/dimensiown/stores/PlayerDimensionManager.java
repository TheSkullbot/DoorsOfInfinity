package skullbot.dimensiown.stores;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import skullbot.dimensiown.Dimensiown;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class PlayerDimensionManager
{
  private static PlayerDimensionManager instance;
  private final  PlayerIndexedDatabase  database;

  private PlayerDimensionManager( PlayerIndexedDatabase database )
  {
    this.database = database;
  }

  public static void init()
  {
    ServerLifecycleEvents.SERVER_STARTING.register( server -> instance = PlayerDimensionManager.open( server ) );
    ServerLifecycleEvents.SERVER_STOPPING.register( server ->
                                                    {
                                                      PlayerDimensionManager instance = PlayerDimensionManager.instance;

                                                      if( instance != null )
                                                        instance.close( server );

                                                      PlayerDimensionManager.instance = null;
                                                    } );
  }

  private static PlayerDimensionManager open( MinecraftServer server )
  {
    try
    {
      Path                  path     = server.getSavePath( WorldSavePath.PLAYERDATA ).resolve( "player_dimensions" );
      PlayerIndexedDatabase database = PlayerIndexedDatabase.open( path );
      return new PlayerDimensionManager( database );
    }
    catch( IOException e )
    {
      throw new RuntimeException( "Failed to open player dimensions database" );
    }
  }

  public static PlayerDimensionManager get()
  {
    return Objects.requireNonNull( instance, "Player dimensions manager not initialized" );
  }

  public void onPlayerJoin( ServerPlayerEntity player )
  {
    DimensionOwner owner = (DimensionOwner) player;
    this.loadDimensions( player.getUuid(), owner.getDimensionInfo() );
  }

  public void onPlayerLeave( ServerPlayerEntity player )
  {
    DimensionOwner owner      = (DimensionOwner) player;
    DimensionInfo  dimensions = owner.getDimensionInfo();
    if( dimensions.isDirty() )
    {
      try
      {
        this.saveDimensions( player.getUuid(), dimensions );
        dimensions.setDirty( false );
      }
      catch( IOException e )
      {
        Dimensiown.LOGGER.error( "Failed to save dimensions for {}", player.getEntityName(), e );
      }
    }
  }

  private void close( MinecraftServer server )
  {
    try
    {
      for( ServerPlayerEntity player : server.getPlayerManager().getPlayerList() )
      {
        this.onPlayerLeave( player );
      }
    }
    finally
    {
      IOUtils.closeQuietly( this.database );
    }
  }

  private void loadDimensions( UUID uuid, DimensionInfo dimensions )
  {
    try
    {
      ByteBuffer bytes = this.database.get( uuid );
      if( bytes != null )
        deserialize( dimensions, bytes );
    }
    catch( IOException e )
    {
      Dimensiown.LOGGER.error( "Failed to load dimensions for {}", uuid, e );
    }
  }

  private void saveDimensions( UUID uuid, DimensionInfo dimensions ) throws IOException
  {
    ByteBuffer bytes = serialize( dimensions );
    this.database.put( uuid, bytes );
  }


  private static ByteBuffer serialize( DimensionInfo dimensions ) throws IOException
  {
    CompoundTag nbt = new CompoundTag();
    nbt.put( "dimensions", dimensions.serialize() );

    try( ByteArrayOutputStream output = new ByteArrayOutputStream() )
    {
      NbtIo.writeCompressed( nbt, output );
      return ByteBuffer.wrap( output.toByteArray() );
    }
  }

  private static void deserialize( DimensionInfo dimensions, ByteBuffer bytes ) throws IOException
  {
    try( ByteArrayInputStream input = new ByteArrayInputStream( bytes.array() ) )
    {
      CompoundTag nbt = NbtIo.readCompressed( input );
      dimensions.deserialize( nbt.getList( "dimensions", NbtType.STRING ) );
    }
  }

  public void loadFromPlayer( DimensionOwner owner, ListTag nbt )
  {
    DimensionInfo info = owner.getDimensionInfo();
    info.deserialize( nbt );
    info.setDirty( true );
  }

  public DimensionInfo getInfo( MinecraftServer server, UUID uuid )
  {
    DimensionOwner owner = getDimensionOwner( server, uuid );
    if( owner != null )
    {
      return owner.getDimensionInfo();
    }
    else
    {
      DimensionInfo dimensions = new DimensionInfo( uuid );
      this.loadDimensions( uuid, dimensions );
      return dimensions;
    }
  }

  @Nullable
  private static DimensionOwner getDimensionOwner( MinecraftServer server, UUID uuid )
  {
    ServerPlayerEntity player = server.getPlayerManager().getPlayer( uuid );
    return (DimensionOwner) player;
  }
}
