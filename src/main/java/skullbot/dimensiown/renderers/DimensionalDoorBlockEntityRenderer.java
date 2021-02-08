package skullbot.dimensiown.renderers;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import skullbot.dimensiown.blockentities.DimensionalDoorBlockEntity;
import skullbot.dimensiown.blocks.DimensionalDoorBlock;

import java.util.Objects;

import static skullbot.dimensiown.registry.Dimensions.DIMENSION_WORLD;

public class DimensionalDoorBlockEntityRenderer extends BlockEntityRenderer<DimensionalDoorBlockEntity>
{
  private final MinecraftClient client = MinecraftClient.getInstance();

  public DimensionalDoorBlockEntityRenderer( BlockEntityRenderDispatcher dispatcher )
  {
    super( dispatcher );
  }

  @Override
  public void render( DimensionalDoorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay )
  {
    Direction direction = entity.getCachedState().get( DimensionalDoorBlock.FACING );

    if( entity.getCachedState().get( DimensionalDoorBlock.HALF ) == DoubleBlockHalf.LOWER )
    {
      // Only show owner if close enough
      if( !entity.getPos().isWithinDistance( client.player.getPos(), 10.0f ) )
        return;

      // Hide owner display for doors on the Personal Dimension side
      String doorWorld = entity.getWorld().getRegistryKey().getValue().toString();
      String voidWorld = DIMENSION_WORLD.getValue().toString();

      if( Objects.equals( doorWorld, voidWorld ) )
        return;

      matrices.push();
      transformToFace( matrices, direction );
      matrices.translate( 0.5, 2.1, -0.025 );
      matrices.multiply( Vector3f.POSITIVE_Z.getDegreesQuaternion( 180 ) );
      matrices.scale( 0.01F, 0.01F, 1 );
      drawCenteredText( "Owner : " + entity.getOwnerName(), 0, 0xFFFFFF, false, matrices.peek().getModel(), vertexConsumers, false, 0xFFFFFF, light);

      matrices.pop();
    }
  }

  public static void transformToFace( MatrixStack stack, Direction direction )
  {
    stack.translate( .5, 0, .5 );
    Quaternion rotationQuaternion = direction.getRotationQuaternion();
    rotationQuaternion.hamiltonProduct( Vector3f.POSITIVE_X.getDegreesQuaternion( -90.0F ) );

    stack.multiply( rotationQuaternion );
    stack.translate( -.5, 0, -.5 );
  }

  public void drawCenteredText( String text, int y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light )
  {
    int textLength = dispatcher.getTextRenderer().getWidth( text );
    dispatcher.getTextRenderer().draw( text, -textLength / 2.0f, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light );
  }
}
