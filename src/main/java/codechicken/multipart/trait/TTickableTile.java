//package codechicken.multipart.trait;
//
//import codechicken.multipart.api.annotation.MultiPartTrait;
//import codechicken.multipart.api.part.ITickablePart;
//import codechicken.multipart.api.part.TMultiPart;
//import codechicken.multipart.block.TileMultiPart;
//import net.minecraft.tileentity.ITickableTileEntity;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by covers1624 on 18/9/20.
// */
//@MultiPartTrait (ITickablePart.class)
//public class TTickableTile extends TileMultiPart implements ITickableTileEntity {
//
//    private final List<ITickablePart> tickingParts = new ArrayList<>();
//    private boolean doesTick;
//
//    @Override
//    public void copyFrom(TileMultiPart that) {
//        super.copyFrom(that);
//        if (that instanceof TTickableTile) {
//            tickingParts.clear();
//            tickingParts.addAll(((TTickableTile) that).tickingParts);
//            doesTick = ((TTickableTile) that).doesTick;
//        }
//    }
//
//    @Override
//    public void bindPart(TMultiPart part) {
//        super.bindPart(part);
//        if (part instanceof ITickablePart) {
//            tickingParts.add((ITickablePart) part);
//            setTicking(true);
//        }
//    }
//
//    @Override
//    public void partRemoved(TMultiPart part, int p) {
//        super.partRemoved(part, p);
//        if (part instanceof ITickablePart) {
//            tickingParts.remove(part);
//        }
//    }
//
//    @Override
//    public void clearParts() {
//        super.clearParts();
//        tickingParts.clear();
//    }
//
//    @Override
//    public void loadFrom(TileMultiPart that) {
//        super.loadFrom(that);
//        if (doesTick) {
//            getLevel().tickableBlockEntities.add(this);
//        }
//    }
//
//    @Override
//    public void loadTo(TileMultiPart that) {
//        super.loadTo(that);
//        if (doesTick) {
//            getLevel().tickableBlockEntities.remove(this);
//        }
//    }
//
//    @Override
//    public void tick() {
//        getCapCache().tick();
//        for (int i = 0, tickingPartsSize = tickingParts.size(); i < tickingPartsSize; i++) {
//            ITickablePart part = tickingParts.get(i);
//            if (((TMultiPart) part).tile() != null) {
//                part.tick();
//            }
//        }
//    }
//
//    private void setTicking(boolean tick) {
//        if (doesTick == tick) {
//            return;
//        }
//        doesTick = tick;
//        if (getLevel() != null && getLevel().getBlockEntity(getBlockPos()) == this) {
//            if (tick) {
//                getLevel().addBlockEntity(this);
//            } else {
//                getLevel().tickableBlockEntities.remove(this);
//            }
//        }
//    }
//}
