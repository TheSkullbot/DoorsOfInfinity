package skullbot.dimensiown.helpers;

import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class PortalCreationHelper
{

  public static Portal spawn( World world, Vec3d pos, double width, double height, Vector3f axisW, Vector3f axisH, RegistryKey<World> dimensionTo, Vec3d dest, boolean teleportable, Quaternion rot, boolean biWay )
  {
    Portal portal = new Portal( Portal.entityType, world );

    portal.width          = width;
    portal.height         = height;
    portal.axisH          = new Vec3d( axisH );
    portal.axisW          = new Vec3d( axisW );
    portal.dimensionTo    = dimensionTo;
    portal.destination    = dest;
    portal.teleportable   = teleportable;
    portal.cullableXEnd   = 0;
    portal.cullableYEnd   = 0;
    portal.cullableXStart = 0;
    portal.cullableYStart = 0;

    if( rot != null )
      portal.rotation = rot;

    portal.setPos( pos.getX(), pos.getY(), pos.getZ() );
    world.spawnEntity( portal );

    if( biWay )
      PortalManipulation.completeBiWayPortal( portal, Portal.entityType );

    return portal;
  }

  public static Portal spawn( World world, Vec3d pos, double width, double height, Direction axisW, RegistryKey<World> dimensionTo, Vec3d dest, boolean teleportable, Quaternion rot )
  {
    Vector3f w = new Vector3f((float)axisW.getOffsetX(), (float)axisW.getOffsetY(), (float)axisW.getOffsetZ());
    Vector3f u = new Vector3f((float)Direction.UP.getOffsetX(), (float)Direction.UP.getOffsetY(), (float)Direction.UP.getOffsetZ());

    return spawn( world, pos, width, height, w, u, dimensionTo, dest, teleportable, rot, true );
  }

}
