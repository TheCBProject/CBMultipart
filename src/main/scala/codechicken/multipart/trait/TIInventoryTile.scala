package codechicken.multipart.`trait`

import java.util.{LinkedList => JLinkedList}

import codechicken.multipart.TileMultipart
import codechicken.multipart.api.annotation.MultiPartTrait
import codechicken.multipart.api.annotation.MultiPartTrait.TraitList
import codechicken.multipart.api.part.TMultiPart
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.{IInventory, ISidedInventory}
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction

import scala.jdk.CollectionConverters._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

trait TIInventoryTile extends TileMultipart with ISidedInventory {
    var invList = ListBuffer[IInventory]()
    var slotMap = Array[(IInventory, Int)]()

    override def copyFrom(that: TileMultipart) {
        super.copyFrom(that)
        that match {
            case tile: TIInventoryTile =>
                invList = tile.invList
                rebuildSlotMap()
            case _ =>
        }
    }

    override def bindPart(part: TMultiPart) {
        super.bindPart(part)
        part match {
            case inventory: IInventory =>
                invList += inventory
                rebuildSlotMap()
            case _ =>
        }
    }

    override def partRemoved(part: TMultiPart, p: Int) {
        super.partRemoved(part, p)
        part match {
            case inventory: IInventory =>
                invList -= inventory
                rebuildSlotMap()
            case _ =>
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

    override def isUsableByPlayer(player: PlayerEntity) = true

    override def openInventory(player: PlayerEntity) {}

    override def closeInventory(player: PlayerEntity) {}

    override def isItemValidForSlot(i: Int, itemstack: ItemStack) = {
        val (inv, slot) = slotMap(i)
        inv.isItemValidForSlot(slot, itemstack)
    }

    override def clear() {
        for (inv <- invList) inv.clear()
    }

    override def getSlotsForFace(side: Direction) = {
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

    override def canInsertItem(i: Int, itemstack: ItemStack, direction: Direction) = {
        val (inv, slot) = slotMap(i)
        inv match {
            case is: ISidedInventory => is.canInsertItem(slot, itemstack, direction)
            case _ => true
        }
    }

    override def canExtractItem(i: Int, itemstack: ItemStack, direction: Direction) = {
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
//Thanks scala...
//@TraitList(Array(
//    new MultiPartTrait(classOf[IInventory]),
//    new MultiPartTrait(classOf[ISidedInventory])
//))
class JInventoryTile extends TileMultipart with TIInventoryTile {
}
