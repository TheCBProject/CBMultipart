package codechicken.microblock.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.microblock.api.MicroMaterial;
import codechicken.microblock.util.MaskedCuboid;
import codechicken.microblock.factory.MicroblockPartFactory;
import codechicken.microblock.util.MicroMaterialRegistries;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.AbstractMultiPart;
import codechicken.multipart.util.PartRayTraceResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Created by covers1624 on 26/6/22.
 */
public abstract class MicroblockPart extends AbstractMultiPart {

    public MicroMaterial material;
    public byte shape = 0;

    public MicroblockPart(MicroMaterial material) {
        this.material = material;
    }

    public abstract MicroblockPartFactory<?> getMicroFactory();

    public final int getSize() {
        return shape >> 4;
    }

    public final int getShapeSlot() {
        return shape & 0xF;
    }

    public MicroMaterial getMaterial() {
        return material;
    }

    /**
     * General purpose microblock description value. These values are only used by
     * subclass overrides in this class, so they can be whatever you would like.
     *
     * @param size A 28 bit value representing the current size
     * @param slot A 4 bit value representing the current slot
     */
    public void setShape(int size, int slot) {
        shape = (byte) (size << 4 | slot & 0xF);
    }

    @Override
    public void writeDesc(MCDataOutput packet) {
        packet.writeRegistryIdUnsafe(MicroMaterialRegistries.MICRO_MATERIALS, material);
        packet.writeByte(shape);
    }

    @Override
    public void readDesc(MCDataInput packet) {
        shape = packet.readByte();
    }

    public void sendShapeUpdate() {
        sendUpdate(p -> p.writeByte(shape));
    }

    @Override
    public void readUpdate(MCDataInput packet) {
        super.readUpdate(packet);
        tile().notifyPartChange(this);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putByte("shape", shape);
        tag.putString("material", material.getRegistryName().toString());
    }

    @Override
    public void load(CompoundTag tag) {
        shape = tag.getByte("shape");
        // TODO redundant because of `createServer`
        material = MicroMaterialRegistries.getMaterial(tag.getString("material"));
    }

    public abstract Cuboid6 getBounds();

    /**
     * The Micro factory that controls placement from item stack form.
     *
     * @return The id of said factory.
     */
    public abstract int getItemFactoryId();

    public abstract List<MaskedCuboid> getRenderCuboids(boolean isInventory);

    @Override
    public final MultiPartType<?> getType() {
        return getMicroFactory().getType();
    }

    @Override
    public float getStrength(Player player, PartRayTraceResult hit) {
        if (material == null) return super.getStrength(player, hit);

        return material.getStrength(player);
    }
}
