package codechicken.multipart.scalatraits

import java.util.{LinkedList => JLinkedList}

import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, ISidedInventory}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextComponentString

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

trait TIInventoryTile extends TileMultipart with ISidedInventory {
    var invList = new JLinkedList[IInventory]()
    var slotMap = Array[(IInventory, Int)]()

    override def copyFrom(that: TileMultipart) {
        super.copyFrom(that)
        if (that.isInstanceOf[TIInventoryTile]) {
            invList = that.asInstanceOf[TIInventoryTile].invList
            rebuildSlotMap()
        }
    }

    override def bindPart(part: TMultiPart) {
        super.bindPart(part)
        if (part.isInstanceOf[IInventory]) {
            invList += part.asInstanceOf[IInventory]
            rebuildSlotMap()
        }
    }

    override def partRemoved(part: TMultiPart, p: Int) {
        super.partRemoved(part, p)
        if (part.isInstanceOf[IInventory]) {
            invList -= part.asInstanceOf[IInventory]
            rebuildSlotMap()
        }
    }

    override def clearParts() {
        super.clearParts()
        invList.clear()
        slotMap = Array()
    }

    def rebuildSlotMap() {
        slotMap = Array.ofDim(invList.map(_.getSizeInventory).sum)
        var i = 0
        for (inv <- invList; s <- 0 until inv.getSizeInventory) {
            slotMap(i) = (inv, s)
            i += 1
        }
    }

    override def isEmpty = {
        for ((inv, slot) <- slotMap) {
            if (!inv.getStackInSlot(slot).isEmpty) {
                false
            }
        }
        true
    }

    override def getName = "Multipart Inventory"

    override def getDisplayName = new TextComponentString(getName)

    override def hasCustomName = false

    override def getSizeInventory: Int = slotMap.length

    override def getStackInSlot(index: Int) = {
        val (inv, slot) = slotMap(index)
        inv.getStackInSlot(slot)
    }

    override def decrStackSize(index: Int, count: Int) = {
        val (inv, slot) = slotMap(index)
        inv.decrStackSize(slot, count)
    }

    override def removeStackFromSlot(index: Int) = {
        val (inv, slot) = slotMap(index)
        inv.removeStackFromSlot(index)
    }

    override def setInventorySlotContents(index: Int, stack: ItemStack) = {
        val (inv, slot) = slotMap(index)
        inv.setInventorySlotContents(slot, stack)
    }

    override def getInventoryStackLimit = 64

    override def isUsableByPlayer(player: EntityPlayer) = true

    override def openInventory(player: EntityPlayer) {}

    override def closeInventory(player: EntityPlayer) {}

    override def isItemValidForSlot(i: Int, itemstack: ItemStack) = {
        val (inv, slot) = slotMap(i)
        inv.isItemValidForSlot(slot, itemstack)
    }

    override def getField(id: Int) = 0

    override def setField(id: Int, value: Int) {}

    override def getFieldCount = 0

    override def clear() {
        for (inv <- invList) inv.clear()
    }

    override def getSlotsForFace(side: EnumFacing) = {
        val buf = new ArrayBuffer[Int]()
        var base = 0
        for (inv <- invList) {
            inv match {
                case is: ISidedInventory => buf ++= is.getSlotsForFace(side).map(_ + base)
                case _ =>
            }
            base += inv.getSizeInventory
        }
        buf.toArray
    }

    override def canInsertItem(i: Int, itemstack: ItemStack, direction: EnumFacing) = {
        val (inv, slot) = slotMap(i)
        inv match {
            case is: ISidedInventory => is.canInsertItem(slot, itemstack, direction)
            case _ => true
        }
    }

    override def canExtractItem(i: Int, itemstack: ItemStack, direction: EnumFacing) = {
        val (inv, slot) = slotMap(i)
        inv match {
            case is: ISidedInventory => is.canExtractItem(slot, itemstack, direction)
            case _ => true
        }
    }
}

/**
 * To handle obfuscation issues, this is registered as a java trait.
 */
class JInventoryTile extends TileMultipart with TIInventoryTile {
}
