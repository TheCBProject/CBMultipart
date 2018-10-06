package codechicken.multipart.capability

import net.minecraft.item.ItemStack
import net.minecraftforge.items.{IItemHandler, IItemHandlerModifiable}

/**
 * This handles merging IItemHandler capabilities together.
 *
 * Created by covers1624 on 6/10/18.
 */
object ItemCapMerger {

    def merge(impls: Iterable[IItemHandler]): IItemHandler = {
        if (impls.forall(_.isInstanceOf[IItemHandlerModifiable])) {
            new MergedModifiableHandler().buildSlotMap(impls.map(_.asInstanceOf[IItemHandlerModifiable]))
        } else {
            new MergedHandler().buildSlotMap(impls)
        }
    }


    class MergedHandler extends IItemHandler {

        var invMap = Array[IItemHandler]()
        var slotMap: Array[Int] = Array.empty

        def buildSlotMap(caps: Iterable[IItemHandler]) = {
            val num = caps.map(_.getSlots).sum
            invMap = Array.ofDim(num)
            slotMap = Array.ofDim(num)
            var i = 0
            for (inv <- caps; s <- 0 until inv.getSlots) {
                invMap(i) = inv
                slotMap(i) = s
                i += 1
            }
            this
        }

        override def getSlots = slotMap.length

        override def getStackInSlot(slot: Int) =
            invMap(slot).getStackInSlot(slotMap(slot))

        override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean) =
            invMap(slot).insertItem(slotMap(slot), stack, simulate)

        override def extractItem(slot: Int, amount: Int, simulate: Boolean) =
            invMap(slot).extractItem(slotMap(slot), amount, simulate)

        override def getSlotLimit(slot: Int) =
            invMap(slot).getSlotLimit(slotMap(slot))

        override def isItemValid(slot: Int, stack: ItemStack) =
            invMap(slot).isItemValid(slotMap(slot), stack)
    }

    class MergedModifiableHandler extends MergedHandler with IItemHandlerModifiable {

        override def setStackInSlot(slot: Int, stack: ItemStack) =
            invMap(slot).asInstanceOf[IItemHandlerModifiable].setStackInSlot(slotMap(slot), stack)
    }

}
