package codechicken.microblock

import codechicken.lib.data.MCDataInput
import codechicken.multipart.{IDynamicPartFactory, MultiPartRegistry}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

abstract class MicroblockFactory extends IDynamicPartFactory
{
    def getName:String

    def baseTrait:Class[_ <: Microblock]

    @SideOnly(Side.CLIENT)
    def clientTrait:Class[_ <: MicroblockClient]

    def getResistanceFactor:Float

    val baseTraitId = MicroblockGenerator.registerTrait(baseTrait)

    @SideOnly(Side.CLIENT)
    lazy val clientTraitId = MicroblockGenerator.registerTrait(clientTrait)

    def register()
    {
        MultiPartRegistry.registerParts(this, Array(getName))
    }

    def create(client:Boolean, material:Int) = MicroblockGenerator.create(this, material, client)

    override def createPart(name:String, packet:MCDataInput) = create(true, if (packet != null) MicroMaterialRegistry.readMaterialID(packet) else 0)

    override def createPart(name:String, nbt:NBTTagCompound) = create(false, if (nbt != null) MicroMaterialRegistry.materialID(nbt.getString("material")) else 0)
}

/**
 * Microblocks with corresponding items
 */
abstract class CommonMicroFactory extends MicroblockFactory
{
    private var factoryID:Int = _

    def getFactoryID = factoryID
    def itemSlot:Int //The slot to use for rendering on an ItemStack
    def placementProperties:PlacementProperties

    def register(id:Int)
    {
        register()
        factoryID = id
        CommonMicroFactory.registerMicroFactory(this, id)
    }
}

object CommonMicroFactory
{
    val factories = new Array[CommonMicroFactory](256)

    def registerMicroFactory(factory:CommonMicroFactory, id:Int)
    {
        if(factories(id) != null)
            throw new IllegalArgumentException("Microblock factory id "+id+" is already taken by "+factories(id).getName+" when adding "+factory.getName)

        factories(id) = factory
    }
}