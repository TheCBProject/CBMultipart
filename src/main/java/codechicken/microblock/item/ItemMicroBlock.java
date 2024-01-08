package codechicken.microblock.item;

import codechicken.lib.raytracer.RayTracer;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.ExecutablePlacement;
import codechicken.microblock.part.MicroblockPlacement;
import codechicken.microblock.part.StandardMicroFactory;
import codechicken.microblock.util.MicroMaterialRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 20/10/22.
 */
public class ItemMicroBlock extends Item {

    private static final String SIZE_TAG = "size";
    private static final String MATERIAL_TAG = "mat";
    private static final String FACTORY_ID_TAG = "factory_id";

    public ItemMicroBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MicroMaterial material = getMaterialFromStack(stack);
        StandardMicroFactory factory = getFactory(stack);
        int size = getSize(stack);
        if (material == null || factory == null) {
            return Component.literal("Unnamed");
        }

        return Component.translatable("item." + factory.getRegistryName().toString().replace(':', '.') + "." + size, material.getLocalizedName());
    }

    //TODO?
//    @Override
//    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
//        if (!allowedIn(tab)) return;
//
//        for (StandardMicroFactory factory : StandardMicroFactory.FACTORIES.values()) {
//            for (int size : new int[] { 1, 2, 4 }) {
//                for (MicroMaterial microMaterial : MicroMaterialRegistry.MICRO_MATERIALS) {
//                    items.add(create(factory.factoryId, size, microMaterial));
//                }
//            }
//        }
//    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        ItemStack stack = player.getItemInHand(ctx.getHand());
        MicroMaterial material = getMaterialFromStack(stack);
        StandardMicroFactory factory = getFactory(stack);
        int size = getSize(stack);
        if (material == null || factory == null) return InteractionResult.FAIL;

        BlockHitResult hit = RayTracer.retraceBlock(level, player, ctx.getClickedPos());
        if (hit != null) {
            ExecutablePlacement placement = new MicroblockPlacement(player, ctx.getHand(), hit, size, material, !player.getAbilities().instabuild, factory.placementProperties()).calculate();
            if (placement == null) return InteractionResult.FAIL;

            if (!level.isClientSide) {
                placement.place(level, player, stack);
                if (!player.getAbilities().instabuild) {
                    placement.consume(level, player, stack);
                }
                SoundType sound = material.getSound();
                if (sound != null) {
                    level.playSound(null, placement.pos.getX() + 0.5D, placement.pos.getY() + 0.5D, placement.pos.getZ() + 0.5D, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    public static StandardMicroFactory getFactory(ItemStack stack) {
        int factoryId = getFactoryID(stack);
        if (factoryId == -1 || factoryId > StandardMicroFactory.FACTORIES.size()) return null;

        return StandardMicroFactory.FACTORIES.get(factoryId);
    }

    public static int getFactoryID(ItemStack stack) {
        if (!stack.getOrCreateTag().contains(FACTORY_ID_TAG)) return -1;

        return stack.getOrCreateTag().getInt(FACTORY_ID_TAG);
    }

    public static int getSize(ItemStack stack) {
        if (!stack.getOrCreateTag().contains(SIZE_TAG)) return -1;

        return stack.getOrCreateTag().getInt(SIZE_TAG);
    }

    @Nullable
    public static MicroMaterial getMaterialFromStack(ItemStack stack) {
        if (!stack.getOrCreateTag().contains(MATERIAL_TAG)) return null;

        return MicroMaterialRegistry.getMaterial(stack.getOrCreateTag().getString(MATERIAL_TAG));
    }

    public static ItemStack create(int factoryId, int size, MicroMaterial material) {
        return create(factoryId, size, material.getRegistryName());
    }

    public static ItemStack create(int factoryId, int size, ResourceLocation material) {
        return createStack(1, factoryId, size, material);
    }

    public static ItemStack createStack(int amount, int factoryId, int size, MicroMaterial material) {
        return createStack(amount, factoryId, size, material.getRegistryName());
    }

    public static ItemStack createStack(int amount, int factoryId, int size, ResourceLocation material) {
        ItemStack stack = new ItemStack(CBMicroblockModContent.MICRO_BLOCK_ITEM.get(), amount);
        stack.getOrCreateTag().putInt(FACTORY_ID_TAG, factoryId);
        stack.getOrCreateTag().putInt(SIZE_TAG, size);
        stack.getOrCreateTag().putString(MATERIAL_TAG, material.toString());

        return stack;
    }
}
