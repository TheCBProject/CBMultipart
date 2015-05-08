package codechicken.multipart.asm

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import java.util.{List => JList}
import scala.collection.mutable.{ListBuffer => MList}
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

import ScalaSignature._

object ScalaSignature
{
    object Bytes
    {
        implicit def reader(bc:Bytes) = new ByteCodeReader(bc)
        
        def apply(bytes:Array[Byte], pos:Int, len:Int) = new Bytes(bytes, pos, len)
        def apply(bytes:Array[Byte]):Bytes = apply(bytes, 0, bytes.length)
    }
    
    class Bytes(val bytes:Array[Byte], val pos:Int, val len:Int)
    {    
        def section() = bytes take pos drop len
    }
    
    trait Flags
    {
        def hasFlag(flag:Int):Boolean
        
        def isPrivate = hasFlag(0x00000004)
        def isProtected = hasFlag(0x00000008)
        def isAbstract = hasFlag(0x00000080)
        def isDeferred = hasFlag(0x00000100)//abstract for methods
        def isMethod = hasFlag(0x00000200)
        def isModule = hasFlag(0x00000400)//object module class
        def isInterface = hasFlag(0x00000800)
        def isParam = hasFlag(0x00002000)
        def isStatic = hasFlag(0x00800000)
        def isTrait = hasFlag(0x02000000)
        def isAccessor = hasFlag(0x08000000)
    }
    
    trait SymbolRef extends Flags
    {
        def full:String
        def flags:Int
        def hasFlag(flag:Int) = (flags&flag) != 0
    }

    trait ClassSymbolRef extends SymbolRef
    {
        def name:String
        def owner:SymbolRef
        def flags:Int
        def info:Int

        def full = owner.full+"."+name
        override def toString = getClass.getName+"("+name+","+owner+","+flags.toHexString+","+info+")"

        def info(sig:ScalaSignature):ClassType = sig.evalT(info)
        def jParent(sig:ScalaSignature) = info(sig).parent.jName
        def jInterfaces(sig:ScalaSignature) = info(sig).interfaces.map(_.jName)
    }
    
    case class ClassSymbol(name:String, owner:SymbolRef, flags:Int, info:Int) extends ClassSymbolRef
    case class ObjectSymbol(name:String, owner:SymbolRef, flags:Int, info:Int) extends ClassSymbolRef

    case class MethodSymbol(name:String, owner:SymbolRef, flags:Int, info:Int) extends SymbolRef
    {
        override def toString = "MethodSymbol("+name+","+owner+","+flags.toHexString+","+info+")"
        def full = owner.full+"."+name
        
        def info(sig:ScalaSignature):SMethodType = sig.evalT(info)
        def jDesc(sig:ScalaSignature):String = info(sig).jDesc(sig)
    }
    
    case class ExternalSymbol(name:String) extends SymbolRef
    {
        override def toString = name
        def full = name
        def flags = 0
    }
    
    case object NoSymbol extends SymbolRef
    {
        def full = "<no symbol>"
        def flags = 0
    }
    
    trait SMethodType
    {
        def jDesc(sig:ScalaSignature):String = "("+params.map(m => m.info(sig).returnType.jDesc).mkString+")"+returnType.jDesc
        def returnType:TypeRef
        def params:List[MethodSymbol]
    }
    
    case class ClassType(owner:SymbolRef, parents:List[TypeRef])
    {
        def parent = parents.head
        def interfaces = parents.drop(1)
    }
    
    case class MethodType(returnType:TypeRef, params:List[MethodSymbol]) extends SMethodType
    
    case class ParameterlessType(returnType:TypeRef) extends SMethodType
    {
        def params = List()
    }
    
    trait TypeRef
    {
        def sym:SymbolRef
        def name = sym.full

        def jName = name.replace('.', '/') match {
            case "scala/AnyRef" | "scala/Any" => "java/lang/Object"
            case s => s
        }

        def jDesc = name match {
            case "scala.Array" => null
            case "scala.Long" => "J"
            case "scala.Int" => "I"
            case "scala.Short" => "S"
            case "scala.Byte" => "B"
            case "scala.Double" => "D"
            case "scala.Float" => "F"
            case "scala.Boolean" => "Z"
            case "scala.Unit" => "V"
            case _ => "L"+jName+";"
        }
    }
    
    case class TypeRefType(owner:TypeRef, sym:SymbolRef, typArgs:List[TypeRef]) extends TypeRef with SMethodType
    {
        def params = List()
        
        def returnType = this
        
        override def jDesc = name match {
            case "scala.Array" => "["+typArgs(0).jDesc
            case _ => super.jDesc
        }
    }
    
    case class ThisType(sym:SymbolRef) extends TypeRef

    case class SingleType(owner:TypeRef, sym:SymbolRef) extends TypeRef
    {
        override def jName = super.jName+"$"
    }

    case object NoType extends TypeRef
    {
        def sym = null
        override def name = "<no type>"
    }
    
    class SigEntry(val start:Int, val bytes:Bytes)
    {
        def id = bytes.bytes(start)
        
        def delete() {
            bytes.bytes(start) = 3
        }
    }
}
case class ScalaSignature(major:Int, minor:Int, table:Array[SigEntry], bytes:Bytes)
{
    def evalS(i:Int):String = {
        val e = table(i)
        val bc = e.bytes
        val bcr = bc:ByteCodeReader
        e.id match {
            case 1|2 => bcr.readString(bc.len)
            case 3 => NoSymbol.full
            case 9|10 =>
                var s = evalS(bcr.readNat)
                if(bc.pos+bc.len > bcr.pos)
                    s = evalS(bcr.readNat)+"."+s
                s
        }
    }
    
    def evalT[T](i:Int):T = eval(i).asInstanceOf[T]
    
    def evalList[T](bcr:ByteCodeReader) = {
        var l = MList[T]()
        while(bcr.more)
            l+=evalT(bcr.readNat)
        l.toList
    }
    
    def eval(i:Int):Any = {//only parse the ones that matter for this project
        val e = table(i)
        val bc = e.bytes
        val bcr = bc:ByteCodeReader
        e.id match {
            case 1|2 => evalS(i)
            case 6 => ClassSymbol(evalS(bcr.readNat), evalT(bcr.readNat), bcr.readNat, bcr.readNat)
            case 7 => ObjectSymbol(evalS(bcr.readNat), evalT(bcr.readNat), bcr.readNat, bcr.readNat)
            case 8 => MethodSymbol(evalS(bcr.readNat), evalT(bcr.readNat), bcr.readNat, bcr.readNat)
            case 9|10 => ExternalSymbol(evalS(i))
            case 11|12 => NoType //12 is actually NoPrefixType (no lower bound)
            case 13 => ThisType(evalT(bcr.readNat))
            case 14 => SingleType(evalT(bcr.readNat), evalT(bcr.readNat))
            case 16 => TypeRefType(evalT(bcr.readNat), evalT(bcr.readNat), evalList(bcr))
            case 19 => ClassType(evalT(bcr.readNat), evalList(bcr))
            case 20 => MethodType(evalT(bcr.readNat), evalList(bcr))
            case 21|48 => ParameterlessType(evalT(bcr.readNat))//48 is actually a bounded super type, but it should work fine for this project
            case _ => NoSymbol
        }
    }

    def collect[T](id:Int) = (0 until table.length).collect {
        case i if table(i).id == id => evalT(i):T
    }

    def findObject(name:String) = collect[ObjectSymbol](7).find(_.full == name)
    def findClass(name:String) = collect[ClassSymbol](6).find(c => !c.isModule && c.full == name)
}

class ByteCodeReader(val bc:Bytes)
{
    var pos = bc.pos
    
    def more = pos < bc.pos+bc.len
    
    def readString(len:Int) = advance(len)(new String(bc.bytes.drop(pos).take(len)))
    
    def readByte = advance(1)(bc.bytes(pos))
    
    def readNat = {
        var r = 0
        var b = 0
        do {
            b = readByte
            r = r<<7|b&0x7F
        }
        while((b&0x80) != 0)
        r
    }
    
    def advance[A](len:Int)(r:A) = {
        if(pos+len > bc.pos+bc.len)
            throw new IllegalArgumentException("Ran off the end of bytecode")
        pos+=len
        r
    }
    
    def readEntry = {
        val p = pos
        val tpe = readByte
        val len = readNat
        advance(len)(new SigEntry(p, new Bytes(bc.bytes, pos, len)))
    }
    
    def readSig = {
        val major = readByte
        val minor = readByte
        val table = new Array[SigEntry](readNat)
        for(i <- 0 until table.size)
            table(i) = readEntry
        ScalaSignature(major, minor, table, bc)
    }
}

object ScalaSigReader
{
    def decode(s:String) = {
        val bytes = s.getBytes
        bytes take ByteCodecs.decode(bytes)
    }
    
    def encode(b:Array[Byte]) = {
        val bytes = ByteCodecs.encode8to7(b)
        var i = 0
        while(i < bytes.length) {
            bytes(i) = ((bytes(i)+1)&0x7F).toByte
            i+=1
        }
        new String(bytes.take(bytes.length-1), "UTF-8")
    }
    
    def read(ann:AnnotationNode):ScalaSignature = Bytes(decode(ann.values.get(1).asInstanceOf[String])).readSig
    
    def write(sig:ScalaSignature, ann:AnnotationNode) = ann.values.set(1, encode(sig.bytes.bytes))
    
    def ann(cnode:ClassNode):Option[AnnotationNode] = cnode.visibleAnnotations match {
        case null => None
        case a => a.find(ann => ann.desc.equals("Lscala/reflect/ScalaSignature;"))
    }
}

class ScalaSigSideTransformer
{
    def transform(ann:AnnotationNode, cnode:ClassNode, removedFields:JList[FieldNode], removedMethods:JList[MethodNode]) {
        if(removedFields.isEmpty && removedMethods.isEmpty)
            return
        
        val remFields = removedFields.asScala.map(f => (f.name, f.desc.replace('$', '/')))
        val remMethods = removedMethods.asScala.map(f => (f.name, f.desc.replace('$', '/')))
        
        val sig = ScalaSigReader.read(ann)
        for(i <- 0 until sig.table.length) {
            val e = sig.table(i)
            if (e.id == 8) {//check and remove
                val sym: MethodSymbol = sig.evalT(i)
                if (sym.isAccessor) {
                    val fName = if (sym.name.endsWith("_$eq")) sym.name.substring(0, sym.name.length - 4) else sym.name
                    if (remFields.find(t => t._1 == sym.name.trim).nonEmpty) {
                        e.delete()
                        val it = cnode.methods.iterator
                        while (it.hasNext) {
                            val m = it.next
                            if (m.name == sym.name && m.desc == sym.jDesc(sig))
                                it.remove()
                        }
                    }
                }
                else if (sym.isMethod) {
                    if (remMethods.find(t => t._1 == sym.name && t._2 == sym.jDesc(sig)).nonEmpty)
                        e.delete()
                }
                else {//field
                    if (remFields.find(t => t._1 == sym.name.trim).nonEmpty)
                        e.delete()
                }
            }
        }
        ScalaSigReader.write(sig, ann)
    }
}