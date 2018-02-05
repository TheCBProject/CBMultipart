package codechicken.microblock

import java.util.{BitSet => JBitSet}

import codechicken.multipart.asm.{ASMMixinFactory, ScratchBitSet}

trait IGeneratedMaterial
{
    def addTraits(traits:JBitSet, mcrFactory:MicroblockFactory, client:Boolean)
}

object MicroblockGenerator extends ASMMixinFactory(classOf[Microblock], classOf[Int]) with ScratchBitSet
{
    def create(mcrFactory:MicroblockFactory, material:Int, client:Boolean) =
    {
        val bitset = freshBitSet
        bitset.set(mcrFactory.baseTraitId)
        if(client) bitset.set(mcrFactory.clientTraitId)

        MicroMaterialRegistry.getMaterial(material) match {
            case genMat:IGeneratedMaterial => genMat.addTraits(bitset, mcrFactory, client)
            case _ =>
        }

        construct(bitset, material:Integer)
    }
}
