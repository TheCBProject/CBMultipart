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
object MicroBlockGenerator extends SidedGenerator[Microblock, Factory, MicroMaterial](MultiPartGenerator.MIXIN_COMPILER, classOf[Microblock], classOf[Factory], "cmb") {

    def loadAnnotations() {
        loadAnnotations(classOf[MicroBlockTrait], classOf[TraitList])
    }

    def create(factory: MicroblockFactory, material: MicroMaterial, client: Boolean): Microblock = {
        val traitBuilder = ImmutableSet.builder[MixinFactory.TraitKey]()
        traitBuilder.addAll(getTraitsForObject(material, client))
        traitBuilder.add(factory.baseTraitKey)
        if (client) {
            traitBuilder.add(factory.clientTraitKey)
        }
        construct(traitBuilder.build()).create(material)
    }
}

trait Factory {
    def create(material:MicroMaterial): Microblock
}
