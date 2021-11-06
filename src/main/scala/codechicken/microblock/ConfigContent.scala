package codechicken.microblock

import codechicken.lib.util.SneakyUtils
import codechicken.microblock.api.{BlockMicroMaterial, MicroMaterial}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.{ForgeRegistries, IForgeRegistry}
import org.apache.logging.log4j.LogManager

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._
import scala.util.Using

object ConfigContent {

    private val LOGGER = LogManager.getLogger()
    private val DEFAULT_FILE = List(
        "# Configuration file for adding microblock materials for aesthetic blocks added by mods",
        "# The '#' character defines a comment, everything after this character on a given line will be ignored.",
        "# Each line needs to be of the form <registry_name>[property=value,otherProperty=value]",
        "# <registry_name> being the registry name of the block. E.G: 'minecraft:stone'",
        "# This can optionally be followed by Key-Value pairs describing any block state properties.",
        "# If no properties are defined, the default state of the block will be used.",
        "# Examples:",
        "#  'minecraft:stone'",
        "#  'minecraft:grass_block[snowy=true]'",
    )

    def parse(file: Path): Unit = {
        if (Files.notExists(file)) {
            Files.write(file, DEFAULT_FILE.asJava, StandardCharsets.UTF_8)
            return
        }
        val registry = MicroMaterialRegistry.MICRO_MATERIALS
        registry.unfreeze()
        Using(Files.newBufferedReader(file, StandardCharsets.UTF_8)) { reader =>
            var lineNumber = 0
            reader.lines().forEach(line => {
                try {
                    lineNumber += 1;
                    parseLine(lineNumber, line, registry)
                } catch {
                    case e: Throwable =>
                        LOGGER.error(s"Failed to read microblock config line $lineNumber: '$line'. Error: ${e.getMessage}")
                        LOGGER.debug(s"Failed to read microblock config line $lineNumber: {}", line, e)
                }
            })
        }
        registry.freeze()
    }

    def parseLine(lineNumber: Int, _line: String, r: IForgeRegistry[MicroMaterial]): Unit = {
        // No point.
        if (_line.startsWith("#")) return

        var line = _line.trim
        val hashIdx = line.lastIndexOf("#")
        if (hashIdx != -1) {
            line = line.substring(0, hashIdx).trim
        }
        // Line is empty after comment.
        if (line.isEmpty) return
        val openBracketIdx = line.indexOf('[')
        val resourceLocation = new ResourceLocation(if (openBracketIdx == -1) line else line.substring(0, openBracketIdx))
        if (!ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
            LOGGER.error(s"Error reading microblock config line $lineNumber, Missing block: '$line'")
            return
        }
        val block = ForgeRegistries.BLOCKS.getValue(resourceLocation)
        var state = block.defaultBlockState()
        if (openBracketIdx != -1) {
            val closeBracketIdx = line.indexOf(']')
            if (closeBracketIdx == -1) {
                LOGGER.error(s"Error reading microblock config line $lineNumber: '$line', Missing closing brace.")
                return
            }
            val rawProps = line.substring(openBracketIdx + 1, closeBracketIdx)
            for ((elem, i) <- rawProps.split(',').zipWithIndex) {
                if (!elem.contains("=")) {
                    LOGGER.error(s"Error reading microblock config line $lineNumber. Property split $i missing equals.")
                    return;
                }
                val propSplit = elem.split("=")
                if (propSplit.size != 2) {
                    LOGGER.error(s"Error reading microblock config line $lineNumber. Property split $i, split on equals error: $elem")
                    return
                }
                val propName = propSplit(0)
                val propValue = propSplit(1)
                val property = block.getStateDefinition.getProperty(propName)
                if (property == null) {
                    LOGGER.error(s"Error reading microblock config line $lineNumber. Property '$propName' does not exist for block: $resourceLocation")
                    return
                }
                val value = property.getValue(propValue)
                if (!value.isPresent) {
                    LOGGER.error(s"Error reading microblock config line $lineNumber. Property '$propName' does not have value $propValue for block: $resourceLocation")
                    return
                }
                state = state.setValue(property, SneakyUtils.unsafeCast(value.get()))
            }
        }
        r.register(BlockMicroMaterial(state))
    }
}
