package codechicken.mixin.scala

import java.util.Optional

import codechicken.mixin.api.{MixinCompiler, MixinLanguageSupport}
import codechicken.mixin.scala.MixinScalaLanguageSupport._
import codechicken.mixin.util.{ClassInfo, FieldMixin, MixinInfo}
import net.minecraftforge.fml.loading.FMLEnvironment
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type
import org.objectweb.asm.Type._
import org.objectweb.asm.tree.{ClassNode, MethodNode}

import scala.collection.mutable.{ListBuffer => MList}
import scala.jdk.CollectionConverters._


/**
 * Default implementation for Scala.
 *
 * Created by covers1624 on 2/11/20.
 */
@MixinLanguageSupport.LanguageName("scala")
class MixinScalaLanguageSupport(mixinCompiler: MixinCompiler) extends MixinLanguageSupport {

    //TODO, This still has a hard dependency on forge.
    def listSideOnly(sig: ScalaSignature): Set[String] = {
        if (FMLEnvironment.dist == null) return Set()
        val side = "net.minecraftforge.api.distmarker.Dist." + FMLEnvironment.dist.name
        sig.collect[sig.AnnotationInfo](40).filter { a =>
            a.annType.name == "net.minecraftforge.api.distmarker.OnlyIn" &&
                a.getValue[sig.EnumLiteral]("value").value.full != side
        }.map(_.owner.full).toSet
    }

    override def obtainInfo(name: String, cNode: ClassNode): Optional[ClassInfo] = {
        if (cNode == null) {
            return None
        }

        def scalaInfo(cnode: ClassNode, obj: Boolean) =
            ScalaSigReader.ann(cnode).flatMap { ann =>
                val sig = ScalaSigReader.read(ann)
                val name = cnode.name.replace('/', '.')
                (if (obj) sig.findObject(name) else sig.findClass(name))
                    .map(csym => ScalaClassInfo(mixinCompiler, cnode, sig, csym))
            }

        if (cNode.name.endsWith("$")) {
            //find scala object
            val baseName = cNode.name.substring(0, cNode.name.length - 1)
            val baseNode = mixinCompiler.getClassNode(baseName)
            if (baseNode != null) {
                scalaInfo(baseNode, true) match {
                    case Some(info) => return Some(info)
                    case None =>
                }
            }
        }
        scalaInfo(cNode, false)
    }

    def getAndRegisterParentTraits(cnode: ClassNode) = cnode.interfaces.asScala.map(mixinCompiler.getClassInfo).collect {
        case i: ScalaClassInfo if i.isTrait && !i.cSym.isInterface =>
            mixinCompiler.registerTrait(i.getCNode)
    }

    override def buildMixinTrait(cNode: ClassNode): Optional[MixinInfo] = {
        //If we hit cache, return that.
        Option(mixinCompiler.getMixinInfo(cNode.name)) match {
            case Some(info) => return Some(info)
            case None =>
        }

        //If we aren't a scala class, we don't do anything.
        val info = mixinCompiler.getClassInfo(cNode) match {
            case info: ScalaClassInfo if info.isTrait => info
            case _ => return None
        }

        val sig = info.sig
        val sideOnly = listSideOnly(sig)

        val parentTraits = getAndRegisterParentTraits(cNode)
        val fields = MList[FieldMixin]()
        val methods = MList[MethodNode]()
        val supers = MList[String]()

        val csym = info.cSym
        for (sym <- sig.collect[sig.MethodSymbol](8)) {
            logger.debug(sym)
            if (sym.isParam || sym.owner != csym) {}
            else if (sideOnly(sym.full)) {}
            else if (sym.isAccessor) {
                if (!sym.name.trim.endsWith("$eq")) {
                    fields += new FieldMixin(sym.name.trim, getReturnType(sym.jDesc).getDescriptor,
                        if (sym.isPrivate) ACC_PRIVATE else ACC_PUBLIC)
                }
            }
            else if (sym.isMethod) {
                val desc = sym.jDesc
                if (sym.name.startsWith("super$")) {
                    supers += sym.name.substring(6) + desc
                } else if (!sym.isPrivate && !sym.isDeferred && sym.name != "$init$") {
                    //also check if the return type is a scala object
                    val objectDesc = Type.getMethodDescriptor(Type.getObjectType(Type.getReturnType(desc).getInternalName + "$"), Type.getArgumentTypes(desc): _*)
                    methods += (cNode.methods.asScala.find(m => m.name == sym.name && (m.desc == desc || m.desc == objectDesc)) match {
                        case Some(m) => m
                        case None => throw new IllegalArgumentException("Unable to add mixin trait " + cNode.name + ": " +
                            sym.name + desc + " found in scala signature but not in class file. Most likely an obfuscation issue.")
                    })
                }
            }
        }
        Some(new MixinInfo(cNode.name, csym.jParent, parentTraits.toList.asJava, fields.asJava, methods.asJava, supers.asJava))
    }
}

object MixinScalaLanguageSupport {

    import scala.language.implicitConversions

    var logger = LogManager.getLogger()

    //Auto convert scala Option to Optional
    implicit def asJavaOptional[T](op: Option[T]): Optional[T] = op match {
        case Some(e) => Optional.of(e)
        case None => Optional.empty()
    }

    implicit def asScalaOption[T](op: Optional[T]): Option[T] = if (op.isPresent) Some(op.get) else None

}
