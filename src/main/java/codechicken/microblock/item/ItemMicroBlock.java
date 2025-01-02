package codechicken.microblock.item;

import codechicken.lib.raytracer.RayTracer;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.init.CBMicroblockModContent;
import codechicken.microblock.part.ExecutablePlacement;
import codechicken.microblock.part.MicroblockPlacement;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Created by covers1624 on 20/10/22.
 */
public class ItemMicroBlock extends Item {

    public ItemMicroBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MicroMaterialComponent component = MicroMaterialComponent.getComponent(stack);
        if (component == null || component.factory() == null) {
            return Component.literal("Unnamed");
        }

        return Component.translatable("item." + component.factory().getRegistryName().toString().replace(':', '.') + "." + component.size(), component.material().getLocalizedName());
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        ItemStack stack = player.getItemInHand(ctx.getHand());
        MicroMaterialComponent component = MicroMaterialComponent.getComponent(stack);
        if (component == null || component.factory() == null) return InteractionResult.FAIL;

        BlockHitResult hit = RayTracer.retraceBlock(level, player, ctx.getClickedPos());
        if (hit != null) {
            ExecutablePlacement placement = new MicroblockPlacement(player, ctx.getHand(), hit, component.size(), component.material(), !player.getAbilities().instabuild, component.factory().placementProperties()).calculate();
            if (placement == null) return InteractionResult.FAIL;

            if (!level.isClientSide) {
                placement.place(level, player, stack);
                if (!player.getAbilities().instabuild) {
                    placement.consume(level, player, stack);
                }
                SoundType sound = component.material().getSound();
                if (sound != null) {
                    level.playSound(null, placement.pos.getX() + 0.5D, placement.pos.getY() + 0.5D, placement.pos.getZ() + 0.5D, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static ItemStack create(int factoryId, int size, MicroMaterial material) {
        return createStack(1, factoryId, size, material);
    }

    public static ItemStack createStack(int amount, int factoryId, int size, MicroMaterial material) {
        ItemStack stack = new ItemStack(CBMicroblockModContent.MICRO_BLOCK_ITEM.get(), amount);
        stack.set(CBMicroblockModContent.MICRO_MATERIAL_COMPONENT, new MicroMaterialComponent(factoryId, size, material));
        return stack;
    }
}
