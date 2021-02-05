package skullbot.dimensiown.helpers;

import com.qouteall.immersive_portals.my_util.IntBox;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalManipulation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import skullbot.dimensiown.entities.DimensionalPortal;

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

  public static DimensionalPortal spawnBreakable( World world, Vec3d pos, double width, double height, Vector3f axisW, Vector3f axisH, RegistryKey<World> dimensionTo, Vec3d dest, boolean teleportable, Quaternion rot, boolean biWay, World transmitterWorld )
  {
    DimensionalPortal portal = new DimensionalPortal( world );

    portal.width            = width;
    portal.height           = height;
    portal.axisH            = new Vec3d( axisH );
    portal.axisW            = new Vec3d( axisW );
    portal.dimensionTo      = dimensionTo;
    portal.destination      = dest;
    portal.teleportable     = teleportable;
    portal.cullableXEnd     = 0;
    portal.cullableYEnd     = 0;
    portal.cullableXStart   = 0;
    portal.cullableYStart   = 0;
    portal.transmitterWorld = transmitterWorld;

    if( rot != null )
      portal.rotation = rot;

    portal.setPos( pos.getX(), pos.getY(), pos.getZ() );
    world.spawnEntity( portal );

    if( biWay )
      PortalManipulation.completeBiWayPortal( portal, Portal.entityType );

    return portal;
  }

  public static Portal spawn( World world, Vec3d pos, double width, double height, Direction axisW, RegistryKey<World> dimensionTo, Vec3d dest, boolean teleportable, Quaternion rot, boolean biWay )
  {
    return spawn( world, pos, width, height, axisW.getUnitVector(), Direction.UP.getUnitVector(), dimensionTo, dest, teleportable, rot, biWay );
  }

  public static Portal spawn( World world, Vec3d pos, double width, double height, Direction axisW, RegistryKey<World> dimensionTo, Vec3d dest, boolean teleportable, Quaternion rot )
  {
    return spawn( world, pos, width, height, axisW.getUnitVector(), Direction.UP.getUnitVector(), dimensionTo, dest, teleportable, rot, true );
  }

}