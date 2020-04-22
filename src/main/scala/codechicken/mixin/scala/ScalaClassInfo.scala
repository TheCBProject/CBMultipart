package codechicken.mixin.scala

import java.util.Optional

import codechicken.mixin.api.MixinCompiler
import codechicken.mixin.scala.MixinScalaLanguageSupport._
import codechicken.mixin.util.ClassNodeInfo
import org.objectweb.asm.tree.ClassNode

import scala.jdk.CollectionConverters._

/**
 * Created by covers1624 on 2/17/20.
 */
case class ScalaClassInfo(mc: MixinCompiler, cNode: ClassNode, sig: ScalaSignature, cSym: ScalaSignature#ClassSymbolRef) extends ClassNodeInfo(mc, cNode) {
    interfaces = cSym.jInterfaces.map(mixinCompiler.getClassInfo).asJava

    override def concreteParent = asScalaOption(getSuperClass) match {
        case Some(e: ScalaClassInfo) if e.isTrait => e.concreteParent
        case e => e
    }

    override def getSuperClass = Optional.ofNullable(mixinCompiler.getClassInfo(cSym.jParent))

    def isTrait = cSym.isTrait

    def isObject = cSym.isObject

}
