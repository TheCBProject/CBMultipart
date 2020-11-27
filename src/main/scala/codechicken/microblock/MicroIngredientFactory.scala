//package codechicken.microblock
//
//import codechicken.microblock.MicroIngredientFactory._
//import com.google.gson.{JsonObject, JsonParseException}
//import net.minecraft.block.state.IBlockState
//import net.minecraft.item.ItemStack
//import net.minecraft.util.{JsonUtils, ResourceLocation}
//import net.minecraftforge.common.crafting.{IIngredientFactory, IngredientNBT, JsonContext}
//import net.minecraftforge.fml.common.registry.ForgeRegistries
//
///**
// * Created by covers1624 on 7/12/2017.
// */
//class MicroIngredientFactory extends IIngredientFactory {
//
//    override def parse(context: JsonContext, json: JsonObject) = {
//        val factory = JsonUtils.getString(json, "factory")
//        val size: Int = JsonUtils.getInt(json, "size")
//        val mat_obj = JsonUtils.getJsonObject(json, "material")
//        val block = new ResourceLocation(JsonUtils.getString(mat_obj, "block"))
//        val meta = JsonUtils.getInt(mat_obj, "meta", 0)
//
//        if (!material_factories.contains(factory)) {
//            throw new JsonParseException(s"Factory should be one of: ${material_factories.keys.mkString(",")}. Got: $factory.")
//        }
//        if (!ForgeRegistries.BLOCKS.containsKey(block)) {
//            throw new JsonParseException(s"Block '${block.toString}' doesn't seem to exist..")
//        }
//        var state: IBlockState = null
//        try {
//            state = ForgeRegistries.BLOCKS.getValue(block).getStateFromMeta(meta)
//        } catch {
//            case _: Throwable => throw new JsonParseException(s"Invalid block metadata. $meta")
//        }
//        new MicroIngredient(ItemMicroPart.create(material_factories(factory), size, BlockMicroMaterial.materialKey(state)))
//    }
//}
//
//class MicroIngredient(stack: ItemStack) extends IngredientNBT(stack)
//
//
//object MicroIngredientFactory {
//
//    final val material_factories: Map[String, Int] = {
//        var v: Map[String, Int] = Map()
//        v += "face" -> FaceMicroFactory.getFactoryID
//        v += "edge" -> EdgeMicroFactory.getFactoryID
//        v += "corner" -> CornerMicroFactory.getFactoryID
//        v += "hollow" -> HollowMicroFactory.getFactoryID
//        v
//    }
//}
