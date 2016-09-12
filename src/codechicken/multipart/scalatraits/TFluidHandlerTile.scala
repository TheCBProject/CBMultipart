package codechicken.multipart.scalatraits

import java.util.LinkedList

import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTankInfo, IFluidHandler}

import scala.collection.JavaConversions._

/**
 * Mixin trait implementation for parts implementing IFluidHandler.
 * Distributes fluid manipulation among fluid handling parts.
 */
trait TFluidHandlerTile extends TileMultipart with IFluidHandler
{
    var tankList = new LinkedList[IFluidHandler]()

    override def copyFrom(that:TileMultipart)
    {
        super.copyFrom(that)
        if(that.isInstanceOf[TFluidHandlerTile])
            tankList = that.asInstanceOf[TFluidHandlerTile].tankList
    }

    override def bindPart(part:TMultiPart)
    {
        super.bindPart(part)
        if(part.isInstanceOf[IFluidHandler])
            tankList+=part.asInstanceOf[IFluidHandler]
    }

    override def partRemoved(part:TMultiPart, p:Int)
    {
        super.partRemoved(part, p)
        if(part.isInstanceOf[IFluidHandler])
            tankList-=part.asInstanceOf[IFluidHandler]
    }

    override def clearParts()
    {
        super.clearParts()
        tankList.clear()
    }

    override def getTankInfo(dir:EnumFacing):Array[FluidTankInfo] =
    {
        var tankCount:Int = 0
        tankList.foreach(t => tankCount += t.getTankInfo(dir).length)
        val tanks = new Array[FluidTankInfo](tankCount)
        var i = 0
        tankList.foreach(p => p.getTankInfo(dir).foreach{t =>
            tanks(i) = t
            i+=1
        })
        return tanks
    }

    override def fill(dir:EnumFacing, liquid:FluidStack, doFill:Boolean):Int =
    {
        var filled = 0
        val initial = liquid.amount
        tankList.foreach(p =>
            filled+=p.fill(dir, copy(liquid, initial-filled), doFill)
        )
        return filled
    }

    override def canFill(dir:EnumFacing, liquid:Fluid) = tankList.find(_.canFill(dir, liquid)).isDefined

    override def canDrain(dir:EnumFacing, liquid:Fluid) = tankList.find(_.canDrain(dir, liquid)).isDefined

    private def copy(liquid:FluidStack, quantity:Int):FluidStack =
    {
        val copy = liquid.copy
        copy.amount = quantity
        return copy
    }

    override def drain(dir:EnumFacing, amount:Int, doDrain:Boolean):FluidStack =
    {
        var drained:FluidStack = null
        var d_amount = 0
        tankList.foreach{p =>
            val drain = amount-d_amount
            val ret = p.drain(dir, drain, false)
            if(ret != null && ret.amount > 0 && (drained == null || drained.isFluidEqual(ret)))
            {
                if(doDrain)
                    p.drain(dir, drain, true)

                if(drained == null)
                    drained = ret

                d_amount+=ret.amount
            }
        }
        if(drained != null)
            drained.amount = d_amount

        return drained
    }

    override def drain(dir:EnumFacing, fluid:FluidStack, doDrain:Boolean):FluidStack =
    {
        val amount = fluid.amount
        var drained:FluidStack = null
        var d_amount = 0
        tankList.foreach{p =>
            val drain = copy(fluid, amount-d_amount)
            val ret = p.drain(dir, drain, false)
            if(ret != null && ret.amount > 0 && (drained == null || drained.isFluidEqual(ret)))
            {
                if(doDrain)
                    p.drain(dir, drain, true)

                if(drained == null)
                    drained = ret

                d_amount+=ret.amount
            }
        }
        if(drained != null)
            drained.amount = d_amount

        return drained
    }
}