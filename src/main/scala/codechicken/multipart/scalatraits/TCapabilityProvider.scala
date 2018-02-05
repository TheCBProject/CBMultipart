package codechicken.multipart.scalatraits

import java.util
import java.util.{LinkedList => JLinkedList}

import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandler}

import scala.collection.JavaConversions._

/**
 * Created by covers1624 on 16/10/2017.
 */
trait TCapabilityProvider extends TileMultipart {

    val ITEM_CAP = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
    var caps = new JLinkedList[ICapabilityProvider]()

    var itemCaps = Array.fill(6)(new WrappedItemHandler)
    var genericItemCap = new WrappedItemHandler

    override def copyFrom(that: TileMultipart) {
        super.copyFrom(that)
        that match {
            case other: TCapabilityProvider =>
                caps = other.caps
                rebuildSlotMap()
            case _ =>
        }
    }

    override def bindPart(part: TMultiPart) {
        super.bindPart(part)
        var rebuild = false
        part match {
            case p: ICapabilityProvider =>
                caps += p
                rebuild = true

            case _ =>
        }

        if (rebuild) {
            rebuildSlotMap()
        }
    }

    override def clearParts() {
        super.clearParts()
        caps.clear()
        itemCaps.foreach(_.clear())
        genericItemCap.clear
    }

    def rebuildSlotMap() {
        var invMap = new util.HashMap[EnumFacing, JLinkedList[IItemHandler]]()
        var generic_invList = new JLinkedList[IItemHandler]()
        for (p <- caps) {
            for (f <- EnumFacing.VALUES) {
                if (p.hasCapability(ITEM_CAP, f)) {
                    val list = invMap.getOrElse(f, new JLinkedList[IItemHandler]())
                    list.add(ITEM_CAP.cast(p.getCapability(ITEM_CAP, f)))
                }
            }
            if (p.hasCapability(ITEM_CAP, null)) {
                generic_invList += ITEM_CAP.cast(p.getCapability(ITEM_CAP, null))
            }
        }
        for ((s, c) <- invMap) {
            itemCaps(s.ordinal()).rebuildSlotMap(c)
        }
        genericItemCap.rebuildSlotMap(generic_invList)
    }

    override def hasCapability(capability: Capability[_], side: EnumFacing): Boolean = {
        if (capability == ITEM_CAP) {
            true
        } else {
            for (c <- caps) {
                if (c.hasCapability(capability, side)) {
                    return true
                }
            }
            super.hasCapability(capability, side)
        }
    }

    override def getCapability[T](capability: Capability[T], side: EnumFacing): T = capability match {
        case ITEM_CAP =>
            ITEM_CAP.cast(if (side == null) genericItemCap else itemCaps(side.ordinal))
        case _ =>
            for (c <- caps) {
                if (c.hasCapability(capability, side)) {
                    return capability.cast(c.getCapability(capability, side))
                }
            }
            super.getCapability(capability, side)
    }
}

/**
 * To handle scala Mixin compiler issues with generics.
 */
class JCapabilityProvider extends TileMultipart with TCapabilityProvider {
}

class WrappedItemHandler extends IItemHandler {

    var slotMap = Array[(IItemHandler, Int)]()

    def rebuildSlotMap(caps: JLinkedList[IItemHandler]) {
        slotMap = Array.ofDim(caps.map(_.getSlots).sum)
        var i = 0
        for (inv <- caps; s <- 0 until inv.getSlots) {
            slotMap(i) = (inv, s)
            i += 1
        }
    }

    def clear() {
        slotMap = Array()
    }

    override def getSlots = slotMap.length

    override def getStackInSlot(index: Int) = {
        val (inv, slot) = slotMap(index)
        inv.getStackInSlot(slot)
    }

    override def insertItem(index: Int, stack: ItemStack, simulate: Boolean) = {
        val (inv, slot) = slotMap(index)
        inv.insertItem(slot, stack, simulate)
    }

    override def extractItem(index: Int, amount: Int, simulate: Boolean) = {
        val (inv, slot) = slotMap(index)
        inv.extractItem(slot, amount, simulate)
    }

    override def getSlotLimit(index: Int) = {
        val (inv, slot) = slotMap(index)
        inv.getSlotLimit(slot)
    }
}
