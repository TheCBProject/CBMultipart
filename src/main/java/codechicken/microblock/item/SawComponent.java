package codechicken.microblock.item;

import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.recipe.MicroRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Component to be added to saw items that can cut microblocks. Provides the logic that
 * determines which blocks can be cut by the saw item this is attached to.
 * <p>
 * For standard Tier-based saws, use {@link #forTier(Tier)}
 *
 * @see SawItem
 * @see MicroRecipe
 */
public record SawComponent(List<Rule> rules, boolean defaultAllowCut) {
    //region Codecs
    public static final Codec<SawComponent> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Rule.CODEC.listOf().fieldOf("rules").forGetter(SawComponent::rules),
                    Codec.BOOL.fieldOf("defaultAllowCut").forGetter(SawComponent::defaultAllowCut)
            ).apply(inst, SawComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SawComponent> STREAM_CODEC = StreamCodec.composite(
            Rule.STREAM_CODEC.apply(ByteBufCodecs.list()), SawComponent::rules,
            ByteBufCodecs.BOOL, SawComponent::defaultAllowCut,
            SawComponent::new
    );
    //endregion

    //region Utilities
    public boolean canCut(BlockState state) {
        for (Rule rule : rules) {
            if (state.is(rule.blocks)) {
                return rule.allowCut();
            }
        }
        return defaultAllowCut;
    }

    /**
     * Creates a SawComponent that cuts any block at the given tier or lower. If a tool of
     * the given tier can harvest a block, then the saw can cut it.
     *
     * @param tier The tier of the saw.
     * @return The saw component.
     */
    public static SawComponent forTier(Tier tier) {
        return new SawComponent(
                List.of(SawComponent.Rule.deniesCut(tier.getIncorrectBlocksForDrops())),
                true);
    }

    public static @Nullable SawComponent getComponent(ItemStack stack) {
        return stack.get(CBMicroblockModContent.SAW_COMPONENT);
    }
    //endregion

    //region Rule
    public record Rule(HolderSet<Block> blocks, boolean allowCut) {
        //region Codecs
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(rule -> rule.group(
                        RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Rule::blocks),
                        Codec.BOOL.fieldOf("allowCut").forGetter(Rule::allowCut)
                ).apply(rule, Rule::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Rule> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderSet(Registries.BLOCK), Rule::blocks,
                ByteBufCodecs.BOOL, Rule::allowCut,
                Rule::new
        );
        //endregion

        //region Utilities
        //@formatter:off
        public static Rule allowsCut(TagKey<Block> tag) { return forTag(tag, true); }
        public static Rule allowsCut(List<Block> blocks) { return forBlocks(blocks, true); }
        public static Rule deniesCut(TagKey<Block> tag) { return forTag(tag, false); }
        public static Rule deniesCut(List<Block> blocks) { return forBlocks(blocks, false); }
        //@formatter:on

        private static Rule forTag(TagKey<Block> tag, boolean allowCut) {
            return new Rule(BuiltInRegistries.BLOCK.getOrCreateTag(tag), allowCut);
        }

        private static Rule forBlocks(List<Block> blocks, boolean allowCut) {
            return new Rule(HolderSet.direct(blocks.stream().map(BuiltInRegistries.BLOCK::wrapAsHolder).toList()), allowCut);
        }
        //endregion
    }
    //endregion
}
