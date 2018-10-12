package codechicken.multipart

import java.lang.Iterable
import java.util
import java.util.{Map => JMap}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.packet.PacketCustom
import codechicken.multipart.api.{IDynamicPartFactory, IPartConverter, IPartFactory, IPlacementConverter}
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{EnumFacing, EnumHand, ResourceLocation}
import net.minecraft.util.math.{BlockPos, Vec3d}
import net.minecraft.world.World
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.{ListBuffer, Map => MMap}

/**
 * This class handles the registration and internal ID mapping of all multipart classes.
 */
object MultiPartRegistry {
    private val nameToFactory = MMap[ResourceLocation, IDynamicPartFactory]()
    private val nameToID = MMap[ResourceLocation, Int]()
    private var idToFactory: Array[IDynamicPartFactory] = _
    private var idToName: Array[ResourceLocation] = _
    private val idWriter = new IDWriter
    private val converters = new util.HashSet[IPartConverter]()
    private val placementConverters = new util.HashSet[IPlacementConverter]()

    /**
     * The state of the registry. 0 = no parts, 1 = registering, 2 = registered
     */
    private var state = 0

    /**
     * Returns true if no more parts can be registered
     */
    def loaded = state == 2

    /**
     * Registers an IPartFactory for an array of types it is capable of instantiating. Must be called before postInit
     */
    def registerParts(partFactory: IPartFactory, types: collection.Iterable[ResourceLocation]) {
        registerParts(partFactory.createPart _, types)
    }

    def registerParts(partFactory: IPartFactory, types: Iterable[ResourceLocation]) {
        registerParts(partFactory.createPart _, types.asScala)
    }

    /**
     * Scala functional version of registerParts. Must be called before postInit
     */
    def registerParts(partFactory: (ResourceLocation, Boolean) => TMultiPart, types: collection.Iterable[ResourceLocation]) {
        registerParts(new IDynamicPartFactory {
            override def createPartServer(name: ResourceLocation, tag: NBTTagCompound) = partFactory(name, false)

            override def createPartClient(name: ResourceLocation, packet: MCDataInput) = partFactory(name, true)
        }, types)
    }

    def registerParts(partFactory: (ResourceLocation, Boolean) => TMultiPart, types: Iterable[ResourceLocation]) {
        registerParts(partFactory, types.asScala)
    }

    /**
     * Registers an IDynamicPartFactory with an array of types it is capable of instantiating. Must be called before postInit
     */
    def registerParts(partFactory: IDynamicPartFactory, types: collection.Iterable[ResourceLocation]) {
        if (loaded) {
            throw new IllegalStateException("Parts must be registered in the init methods.")
        }
        state = 1

        val modContainer = Loader.instance.activeModContainer
        if (modContainer == null) {
            throw new IllegalStateException("Parts must be registered during the initialization phase of a mod container")
        }

        types.foreach { s =>
            if (nameToFactory.contains(s)) {
                throw new IllegalStateException("Part with id " + s + " is already registered.")
            }

            nameToFactory.put(s, partFactory)
        }
    }

    def registerParts(partFactory: IDynamicPartFactory, types: Iterable[ResourceLocation]) {
        registerParts(partFactory, types.asScala)
    }

    /**
     * Register a part converter instance
     */
    def registerConverter(c: IPartConverter) {
        converters.add(c)
    }

    def registerPlacementConverter(c:IPlacementConverter) {
        placementConverters.add(c)
    }

    private[multipart] def beforeServerStart() {
        val (a1, a2) = nameToFactory.toArray.sortBy(_._1).unzip
        idToName = a1
        idToFactory = a2
        idWriter.setMax(idToName.length)
        nameToID.clear()
        for ((name, id) <- idToName.zipWithIndex)
            nameToID.put(name, id)
    }

    private[multipart] def writeIDMap(packet: PacketCustom) {
        packet.writeInt(idToName.length)
        idToName.foreach(packet.writeResourceLocation)
    }

    private[multipart] def readIDMap(packet: PacketCustom): Seq[ResourceLocation] = {
        val k = packet.readInt()
        idWriter.setMax(k)
        idToName = new Array(k)
        idToFactory = new Array(k)
        nameToID.clear()
        val missing = ListBuffer[ResourceLocation]()
        for (i <- 0 until k) {
            val s = packet.readResourceLocation()
            val v = nameToFactory.get(s)
            if (v.isEmpty) {
                missing += s
            } else {
                idToName(i) = s
                idToFactory(i) = v.get
                nameToID.put(s, i)
            }
        }
        missing
    }

    /**
     * Return true if any multiparts have been registered
     */
    private[multipart] def required = state > 0

    private[multipart] def postInit() {
        state = 2
    }

    def getRegisteredParts = nameToFactory.keys

    /**
     * Writes the id of part to data
     */
    def writePartID(data: MCDataOutput, part: TMultiPart) {
        idWriter.write(data, nameToID(part.getType))
    }

    /**
     * Uses instantiators to create a new part from the id read from data
     */
    def readPart(data: MCDataInput) = {
        val id = idWriter.read(data)
        val (name, factory) = (idToName(id), idToFactory(id))
        factory.createPartClient(name, data)
    }

    /**
     * Uses instantiators to create a new part from the a tag compound
     */
    def loadPart(name: ResourceLocation, nbt: NBTTagCompound) = nameToFactory.get(name) match {
        case Some(factory) => factory.createPartServer(name, nbt)
        case None =>
            logger.error("Missing mapping for part with ID: " + name)
            null
    }

    /**
     * Calls converters to create a multipart version of the block at pos
     */
    def convertBlock(world: World, pos: BlockPos, state: IBlockState): Iterable[TMultiPart] = {
        converters.find(_.canConvert(world, pos, state)) match {
            case Some(p) => p.convertToParts(world, pos, state)
            case None => Seq()
        }
    }

    def convertItem(stack:ItemStack, world:World, pos:BlockPos, sideHit:EnumFacing, hitVec:Vec3d, entityPlayer:EntityLivingBase, hand:EnumHand) = {
        placementConverters.find(_.canConvert(stack)) match {
            case Some(p) => p.convert(stack, world, pos, sideHit, hitVec, entityPlayer, hand)
            case None => null
        }
    }
}

trait IMultipartStateMapper {
    def putStateModelLocations(partName: String, container: BlockStateContainer): JMap[IBlockState, ModelResourceLocation]
}

@SideOnly(Side.CLIENT)
object MultiPartRegistryClient {
    private[multipart] val nameToStateContainer = MMap[ResourceLocation, BlockStateContainer]()
    private[multipart] val nameToModelPath = MMap[ResourceLocation, ResourceLocation]()
    private[multipart] val nameToModelMapper = MMap[ResourceLocation, IMultipartStateMapper]()

    private[multipart] def getModelPartContainer(part: IModelRenderPart) =
        nameToStateContainer.getOrElseUpdate(part.getType, {
            nameToModelPath += (part.getType -> part.getModelPath)
            part.createBlockStateContainer
        })

    /**
     * Optionally register a custom state mapper for this part. If you dont register
     * a custom mapper, a default vanilla mapper will be used.
     */
    def registerCustomStateMapper(part: IModelRenderPart, mapper: IMultipartStateMapper) {
        getModelPartContainer(part) //register right away so it can be mapped
        nameToModelMapper += (part.getType -> mapper)
    }
}
