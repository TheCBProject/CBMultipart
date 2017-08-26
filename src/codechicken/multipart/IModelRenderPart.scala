package codechicken.multipart

import net.minecraft.block.state.{IBlockState, BlockStateContainer}
import net.minecraft.util.{BlockRenderLayer, ResourceLocation}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Marker Interface for parts that wish to use vanilla-like models. Register an instance
  * of this part in MultipartRegistryClient for custom state mapping if needed.
  *
  * Note that the standard render methods (renderStatic, renderDynamic/renderFast) will
  * still be called should you wish to render portions of this part that way.
  */
trait IModelRenderPart
{
    /**
      * Pass-down from TMultiPart
      */
    def getType:ResourceLocation

    /**
      * Used to determine if this part should be rendered in
      * the layer.
      */
    def canRenderInLayer(layer:BlockRenderLayer):Boolean

    /**
      * Returns the path to the model used by this part. This will be turned into a
      * ModelResourceLocation by the default state mapper.
      */
    def getModelPath:ResourceLocation

    /**
      * Returns a BlockStateContainer object with all required properties for rendering. This
      * is called one time and cached.
      *
      * This container can contain normal and unlisted properties. The block parameter
      * for the container is ignored. You may use null or BlockMultipart if you wish.
      *
      * Note that this data is used for rendering only. Do not store data here. Nothing
      * inside these states will be saved.
      */
    def createBlockStateContainer:BlockStateContainer

    /**
      * Used to mutate the default state that is passed in into the current state of the part.
      * You must set both normal and unlisted properties here, as there is no separate method
      * for unlisted properties as with normal blocks.
      *
      * Note that this data is used for rendering only. Do not store data here. Nothing
      * inside these states will be saved.
      *
      * @param state The default state of this part
      * @return The current state of this part, with all properties assigned to proper values
      */
    def getCurrentState(state:IBlockState):IBlockState
}
