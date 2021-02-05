package skullbot.dimensiown;

import com.qouteall.immersive_portals.render.PortalEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import skullbot.dimensiown.registry.*;
import skullbot.dimensiown.renderers.DimensionalDoorBlockEntityRenderer;

public class DimensiownClient implements ClientModInitializer
{
  @Override
  public void onInitializeClient()
  {
    // Initialize renderers
    BlockRenderLayerMap.INSTANCE.putBlock( Blocks.DIM_DOOR_UNBREAKABLE, RenderLayer.getCutout() );
    BlockRenderLayerMap.INSTANCE.putBlock( Blocks.DIM_DOOR,             RenderLayer.getCutout() );
    BlockEntityRendererRegistry.INSTANCE.register( BlockEntities.DIM_DOOR, DimensionalDoorBlockEntityRenderer::new );
    EntityRendererRegistry.INSTANCE.register( Entities.DIM_PORTAL, ( entityRenderDispatcher, context ) -> new PortalEntityRenderer( entityRenderDispatcher ) );
  }

}
