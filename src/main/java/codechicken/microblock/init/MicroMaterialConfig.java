package codechicken.microblock.init;

import codechicken.microblock.api.BlockMicroMaterial;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.util.MicroMaterialRegistry;
import net.covers1624.quack.io.IOUtils;
import net.covers1624.quack.util.SneakyUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Created by covers1624 on 2/5/23.
 */
public class MicroMaterialConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> DEFAULT_CONFIG = List.of(
            "# Configuration file for adding microblock materials for aesthetic blocks added by mods",
            "# The '#' character defines a comment, everything after this character on a given line will be ignored.",
            "# Each line needs to be of the form <registry_name>[property=value,otherProperty=value]",
            "# <registry_name> being the registry name of the block. E.G: 'minecraft:stone'",
            "# This can optionally be followed by Key-Value pairs describing any block state properties.",
            "# If no properties are defined, the default state of the block will be used.",
            "# Examples:",
            "#minecraft:stone",
            "#minecraft:grass_block[snowy=true]"
    );

    public static void parse(Path file) {
        if (Files.notExists(file)) {
            try {
                Files.write(IOUtils.makeParents(file), DEFAULT_CONFIG, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                LOGGER.error("Failed to write default microblock config.", ex);
            }
            return;
        }
        ForgeRegistry<MicroMaterial> registry = (ForgeRegistry<MicroMaterial>) MicroMaterialRegistry.MICRO_MATERIALS;
        registry.unfreeze();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            int i = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line.trim(), i++, registry);
            }
        } catch (IOException ex) {
            LOGGER.error("failed to read microblock config.", ex);
        }
        registry.freeze();
    }

    private static void parseLine(String line, int lineNumber, IForgeRegistry<MicroMaterial> registry) {
        int hashIndex = line.indexOf("#");
        if (hashIndex != -1) {
            line = line.substring(0, hashIndex).trim();
        }
        if (line.isEmpty()) return;

        int openBracketIdx = line.indexOf("[");
        ResourceLocation resourceLocation = new ResourceLocation(openBracketIdx == -1 ? line : line.substring(0, openBracketIdx));
        Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
        if (block == null) {
            LOGGER.error("Error reading microblock config line {}, Missing block: '{}'", lineNumber, resourceLocation);
            return;
        }
        BlockState state = block.defaultBlockState();
        if (openBracketIdx != -1) {
            int closeBracketIdx = line.indexOf(']');
            if (closeBracketIdx == -1) {
                LOGGER.error("Error reading microblock config line {}: '{}', Missing closing brace.", lineNumber, line);
                return;
            }
            String rawProps = line.substring(openBracketIdx + 1, closeBracketIdx);
            String[] splits = rawProps.split(",");
            for (int i = 0; i < splits.length; i++) {
                String elem = splits[i];
                if (!elem.contains("=")) {
                    LOGGER.error("Error reading microblock config line {}. Property split {} missing equals.", lineNumber, i);
                    return;
                }
                String[] propSplit = elem.split("=");
                if (propSplit.length != 2) {
                    LOGGER.error("Error reading microblock config line {}. Property split {}, split on equals error: {}", lineNumber, i, elem);
                    return;
                }
                String propName = propSplit[0];
                String propValue = propSplit[1];
                Property<?> property = block.getStateDefinition().getProperty(propName);
                if (property == null) {
                    LOGGER.error("Error reading microblock config line {}. Property '{}' does not exist for block: {}", lineNumber, propName, resourceLocation);
                    return;
                }
                Optional<?> value = property.getValue(propValue);
                if (value.isEmpty()) {
                    LOGGER.error("Error reading microblock config line {}. Property '{}' does not have value {} for block: {}", lineNumber, propName, propValue, resourceLocation);
                    return;
                }
                state = state.setValue(property, SneakyUtils.unsafeCast(value.get()));
            }
        }
        if (registry.containsKey(BlockMicroMaterial.makeMaterialKey(state))) {
            LOGGER.warn("Skipping microblock config line {}. Micro material for BlockState {} already registered.", lineNumber, state);
            return;
        }
        registry.register(new BlockMicroMaterial(state));
    }
}
