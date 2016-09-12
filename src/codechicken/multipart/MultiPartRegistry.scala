package codechicken.multipart

import java.lang.Iterable
import java.util.{Map => JMap}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.packet.PacketCustom
import codechicken.lib.vec.BlockCoord
import com.google.common.collect.ArrayListMultimap
import net.minecraft.block.Block
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.fml.common.{Loader, ModContainer}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer, Map => MMap}

/**
  * Interface to be registered for constructing parts.
  * Every instance of every multipart is constructed from an implementor of this.
  */
trait IPartFactory
{
    /**
      * Create a new instance of the part with the specified type name identifier
      *
      * @param client If the part instance is for the client or the server
      */
    def createPart(name:String, client:Boolean):TMultiPart
}

/**
  * A version of IPartFactory that can construct parts based on the data that is to be loaded to it (NBT for servers, packets for clients).
  * This is used in cases where the class of the part can change depending on the data it will be given.
  */
trait IDynamicPartFactory
{
    /**
      * Create a new server instance of the part with the specified type name identifier
      *
      * @param tag The tag compound that you need to pass to part.load. As there is no gaurantee
      *            on if this tag is non-null or what it contains, you need to be able to
      *            safely handle invalid/null tags.
      */
    def createPart(name:String, tag:NBTTagCompound):TMultiPart

    /**
      * Create a new client instance of the part with the specified type name identifier
      *
      * @param packet The packet that you need to pass to part.readDesc. As there is no gaurantee
      *               on if this packet is non-null or what it contains, you need to be able to
      *               safely handle invalid/null packets.
      */
    def createPart(name:String, packet:MCDataInput):TMultiPart
}

/**
  * An interface for converting existing blocks/tile entities to multipart versions.
  */
trait IPartConverter
{
    /**
      * Return true if this converter can handle the specific blockID (may or may not actually convert the block)
      */
    def blockTypes:Iterable[Block]

    /**
      * Return a multipart version of the block at pos in world. Return null if no conversion is possible.
      */
    def convert(world:World, pos:BlockCoord):TMultiPart
}

/**
 * This class handles the registration and internal ID mapping of all multipart classes.
 */
object MultiPartRegistry
{
    private val nameToFactory = MMap[String, IDynamicPartFactory]()
    private val nameToID = MMap[String, Int]()
    private var idToFactory:Array[IDynamicPartFactory] = _
    private var idToName:Array[String] = _
    private val idWriter = new IDWriter
    private val converters = ArrayListMultimap.create[Block, IPartConverter]()
    private val modContainers = MMap[String, ModContainer]() //TODO does not apear to be used

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
    def registerParts(partFactory:IPartFactory, types:Array[String])
    {
        registerParts(partFactory.createPart _, types)
    }

    /**
      * Scala functional version of registerParts. Must be called before postInit
      */
    def registerParts(partFactory:(String, Boolean) => TMultiPart, types:Array[String])
    {
        registerParts(new IDynamicPartFactory {
            override def createPart(name:String, tag:NBTTagCompound) = partFactory(name, false)
            override def createPart(name:String, packet:MCDataInput) = partFactory(name, true)
        }, types)
    }

    /**
     * Registers an IDynamicPartFactory with an array of types it is capable of instantiating. Must be called before postInit
     */
    def registerParts(partFactory:IDynamicPartFactory, types:Array[String])
    {
        if (loaded)
            throw new IllegalStateException("Parts must be registered in the init methods.")
        state = 1

        val modContainer = Loader.instance.activeModContainer
        if (modContainer == null)
            throw new IllegalStateException("Parts must be registered during the initialization phase of a mod container")

        types.foreach { s =>
            if (nameToFactory.contains(s))
                throw new IllegalStateException("Part with id "+s+" is already registered.")

            logger.info("Registered multipart "+s)

            nameToFactory.put(s, partFactory)
        }
    }

    /**
     * Register a part converter instance
     */
    def registerConverter(c:IPartConverter)
    {
        c.blockTypes.foreach(converters.put(_, c))
    }

    private[multipart] def beforeServerStart()
    {
        val (a1, a2) = nameToFactory.toArray.sortBy(_._1).unzip
        idToName = a1
        idToFactory = a2
        idWriter.setMax(idToName.length)
        nameToID.clear()
        for ((name, id) <- idToName.zipWithIndex)
            nameToID.put(name, id)
    }

    private[multipart] def writeIDMap(packet:PacketCustom)
    {
        packet.writeInt(idToName.length)
        idToName.foreach(packet.writeString)
    }

    private[multipart] def readIDMap(packet:PacketCustom):Seq[String] =
    {
        val k = packet.readInt()
        idWriter.setMax(k)
        idToName = new Array(k)
        idToFactory = new Array(k)
        nameToID.clear()
        val missing = ListBuffer[String]()
        for (i <- 0 until k) {
            val s = packet.readString()
            val v = nameToFactory.get(s)
            if (v.isEmpty)
                missing += s
            else {
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

    private[multipart] def postInit()
    {
        state = 2
    }

    def getRegisteredParts = nameToFactory.keys

    /**
     * Writes the id of part to data
     */
    def writePartID(data:MCDataOutput, part:TMultiPart)
    {
        idWriter.write(data, nameToID(part.getType))
    }

    /**
     * Uses instantiators to create a new part from the id read from data
     */
    def readPart(data:MCDataInput) =
    {
        val id = idWriter.read(data)
        val (name, factory) = (idToName(id), idToFactory(id))
        factory.createPart(name, data)
    }

    /**
     * Uses instantiators to create a new part from the a tag compound
     */
    def loadPart(name:String, nbt:NBTTagCompound) = nameToFactory.get(name) match
    {
        case Some(factory) => factory.createPart(name, nbt)
        case None =>
            logger.error("Missing mapping for part with ID: "+name)
            null
    }

    /**
      * Calls converters to create a multipart version of the block at pos
      */
    def convertBlock(world:World, pos:BlockCoord, block:Block):TMultiPart =
    {
        for (c <- converters.get(block)) {
            val ret = c.convert(world, pos)
            if (ret != null)
                return ret
        }
        null
    }

    def getModContainer(name:String) = modContainers(name)
}

trait IMultipartStateMapper
{
    def putStateModelLocations(partName:String, container:BlockStateContainer):JMap[IBlockState, ModelResourceLocation]
}

@SideOnly(Side.CLIENT)
object MultiPartRegistryClient
{
    private[multipart] val nameToStateContainer = MMap[String, BlockStateContainer]()
    private[multipart] val nameToModelPath = MMap[String, ResourceLocation]()
    private[multipart] val nameToModelMapper = MMap[String, IMultipartStateMapper]()

    private[multipart] def getModelPartContainer(part:IModelRenderPart) =
        nameToStateContainer.getOrElseUpdate(part.getType, {
            nameToModelPath += (part.getType -> part.getModelPath)
            part.createBlockStateContainer
        })

    /**
      * Optionally register a custom state mapper for this part. If you dont register
      * a custom mapper, a default vanilla mapper will be used.
      */
    def registerCustomStateMapper(partName:String, mapper:IMultipartStateMapper)
    {
        nameToModelMapper += (partName -> mapper)
    }
}