package codechicken.microblock

import codechicken.microblock.api.MicroBlockTrait.TraitList
import codechicken.microblock.api.{MicroBlockTrait, MicroMaterial}
import codechicken.mixin.api.{MixinCompiler, MixinFactory}
import codechicken.mixin.forge.{ForgeMixinBackend, SidedGenerator}
import codechicken.multipart.util.MultiPartGenerator
import com.google.common.collect.ImmutableSet

/**
 * Created by covers1624 on 4/18/20.
 */
object MicroBlockGenerator extends SidedGenerator[Microblock, MicroMaterial](MultiPartGenerator.MIXIN_COMPILER, classOf[Microblock], "cmb", classOf[Int]) {

    def loadAnnotations() {
        loadAnnotations(classOf[MicroBlockTrait], classOf[TraitList])
    }

    def create(factory: MicroblockFactory, material: Int, client: Boolean): Microblock = {
        val microMaterial = MicroMaterialRegistry.getMaterial(material)
        val traitBuilder = ImmutableSet.builder[MixinFactory.TraitKey]()
        traitBuilder.addAll(getTraitsForObject(microMaterial, client))
        traitBuilder.add(factory.baseTraitKey)
        if (client) {
            traitBuilder.add(factory.clientTraitKey)
        }
        construct(traitBuilder.build(), material: Integer)
    }

}
