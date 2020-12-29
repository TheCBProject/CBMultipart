package codechicken.microblock

import codechicken.lib.data.MCDataInput
import codechicken.microblock.api.MicroMaterial
import codechicken.multipart.api.MultiPartType
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

abstract class MicroblockFactory extends MultiPartType[Microblock] {

    def getType: MultiPartType[_]

    def baseTrait: Class[_ <: Microblock]

    @OnlyIn(Dist.CLIENT)
    def clientTrait: Class[_ <: MicroblockClient]

    def getResistanceFactor: Float

    val baseTraitKey = MicroBlockGenerator.registerTrait(baseTrait)

    @OnlyIn(Dist.CLIENT)
    lazy val clientTraitKey = MicroBlockGenerator.registerTrait(clientTrait)

    def register() {
    }

    def create(client: Boolean, material: MicroMaterial) = MicroBlockGenerator.create(this, material, client)

    override def createPartClient(packet: MCDataInput) = create(true, packet.readRegistryIdUnsafe(MicroMaterialRegistry.MICRO_MATERIALS))

    override def createPartServer(tag: CompoundNBT) = create(false, MicroMaterialRegistry.getMaterial(tag.getString("material")))
}

/**
 * Microblocks with corresponding items
 */
abstract class CommonMicroFactory extends MicroblockFactory {
    private var factoryID: Int = _

    def getFactoryID = factoryID

    def itemSlot: Int //The slot to use for rendering on an ItemStack
    def placementProperties: PlacementProperties

    def register(id: Int) {
        register()
        factoryID = id
        CommonMicroFactory.registerMicroFactory(this, id)
    }
}

object CommonMicroFactory {
    val factories = new Array[CommonMicroFactory](256)

    def registerMicroFactory(factory: CommonMicroFactory, id: Int) {
        if (factories(id) != null) {
            throw new IllegalArgumentException("Microblock factory id " + id + " is already taken by " + factories(id).getType.getRegistryName + " when adding " + factory.getType.getRegistryName)
        }

        factories(id) = factory
    }
}
