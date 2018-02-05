package codechicken.microblock

import java.io._
import java.lang.{Iterable => JIterable}

import codechicken.microblock.BlockMicroMaterial.{createAndRegister, materialKey}
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MMap}

object ConfigContent {
    private val nameMap = MMap[String, Seq[Int]]()

    def parse(cfgDir: File) {
        val cfgFile = new File(cfgDir, "microblocks.cfg")
        try {
            if (!cfgFile.exists()) {
                generateDefault(cfgFile)
            } else {
                loadLines(cfgFile)
            }
        }
        catch {
            case e: IOException => logger.error("Error parsing config", e)
        }
    }

    def generateDefault(cfgFile: File) {
        val writer = new PrintWriter(cfgFile)
        writer.println("#Configuration file for adding microblock materials for aesthetic blocks added by mods")
        writer.println("#Each line needs to be of the form <name>:<meta>")
        writer.println("#<name> is the registry key of the block/item enclosed in quotes. NEI can help you find these")
        writer.println("#<meta> may be ommitted, in which case it defaults to 0, otherwise it can be a number, a comma separated list of numbers, or a dash separated range")
        writer.println("#Ex. \"dirt\" \"minecraft:planks\":3 \"iron_ore\":1,2,3,5 \"ThermalFoundation:Storage\":0-15")
        writer.close()
    }

    def loadLine(line: String) {
        if (line.startsWith("#") || line.length < 3) {
            return
        }

        if (line.charAt(0) != '\"') {
            throw new IllegalArgumentException("Line must begin with a quote")
        }
        val q2 = line.indexOf('\"', 1)
        if (q2 < 0) {
            throw new IllegalArgumentException("Unmatched quotes")
        }

        var name = line.substring(1, q2)
        if (!name.contains('.') && !name.contains(':')) {
            name = "minecraft:" + name
        }

        var metas = Seq(0)
        if (line.length > q2 + 1) {
            if (line.charAt(q2 + 1) != ':') {
                throw new IllegalArgumentException("Name must be followed by a colon separator")
            }

            metas = line.substring(q2 + 2).split(",").flatMap { s =>
                if (s.contains("-")) {
                    val split2 = s.split("-")
                    if (split2.length != 2) {
                        throw new IllegalArgumentException("Invalid - separated range")
                    }
                    split2(0).toInt to split2(1).toInt
                }
                else {
                    Seq(s.toInt)
                }
            }
        }

        nameMap.put(name, metas)
    }

    def loadLines(cfgFile: File) {
        val reader = new BufferedReader(new FileReader(cfgFile))
        var s: String = null
        do {
            s = reader.readLine
            if (s != null) {
                try {
                    loadLine(s)
                }
                catch {
                    case e: Exception =>
                        logger.error("Invalid line in microblocks.cfg: " + s)
                        logger.error(e.getMessage)
                }
            }
        }
        while (s != null)
        reader.close()
    }

    def load() {
        for (block <- Block.REGISTRY.asInstanceOf[JIterable[Block]]) {
            val metas = Seq(block.getRegistryName.toString).flatMap(nameMap.remove).flatten
            metas.foreach { m =>
                val state = block.getStateFromMeta(m)

                try {
                    createAndRegister(state)
                    logger.debug("Adding micro material from config: " + materialKey(state))
                }
                catch {
                    case e: IllegalStateException => logger.error("Unable to register micro material: " +
                        materialKey(state) + "\n\t" + e.getMessage)
                    case e: Exception =>
                        logger.error("Unable to register micro material: " + materialKey(state), e)
                }
            }
        }

        nameMap.foreach(e => logger.warn("Unable to add micro material for block with unlocalised name " + e._1 + " as it doesn't exist"))
    }

    def handleIMC(messages: Seq[IMCMessage]) {
        messages.filter(_.key == "microMaterial").foreach { msg =>

            def error(s: String) {
                logger.error("Invalid microblock IMC message from " + msg.getSender + ": " + s)
            }

            if (msg.getMessageType != classOf[ItemStack]) {
                error("value is not an instanceof ItemStack")
            } else {
                val stack = msg.getItemStackValue
                if (!Block.REGISTRY.containsKey(stack.getItem.getRegistryName)) {
                    error("Invalid Block: " + stack.getItem)
                } else if (stack.getItemDamage < 0 || stack.getItemDamage >= 16) {
                    error("Invalid metadata: " + stack.getItemDamage)
                } else {

                    val state = Block.getBlockFromItem(stack.getItem).getStateFromMeta(stack.getItemDamage)

                    try {
                        createAndRegister(state)
                        logger.debug("Adding micro material from IMC. Mod: [" + msg.getSender + "] Material Key: [" + materialKey(state) + "]")
                    }
                    catch {
                        case e: IllegalStateException => error("Unable to register micro material: " +
                            materialKey(state) + "\n\t" + e.getMessage)
                    }
                }
            }
        }
    }
}
