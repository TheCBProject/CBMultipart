//package codechicken.multipart.trait;
//
//import codechicken.lib.render.CCRenderState;
//import codechicken.lib.vec.Vector3;
//import codechicken.mixin.forge.TraitSide;
//import codechicken.multipart.TileMultipart;
//import codechicken.multipart.TileMultipartClient;
//import codechicken.multipart.api.annotation.MultiPartTrait;
//import codechicken.multipart.api.part.IModelRenderPart;
//import codechicken.multipart.api.part.TMultiPart;
//import com.mojang.blaze3d.matrix.MatrixStack;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.BlockRendererDispatcher;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.world.ILightReader;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Random;
//
///**
// * Created by covers1624 on 1/9/20.
// */
////@MultiPartTrait (value = IModelRenderPart.class, side = TraitSide.CLIENT)
//class TModelRenderTile extends TileMultipart implements TileMultipartClient {
//
//    private final List<IModelRenderPart> modelPartList = new LinkedList<>();
//
//    @Override
//    public void copyFrom(TileMultipart that) {
//        super.copyFrom(that);
//        if (that instanceof TModelRenderTile) {
//            modelPartList.clear();
//            modelPartList.addAll(((TModelRenderTile) that).modelPartList);
//        }
//    }
//
//    @Override
//    public void bindPart(TMultiPart part) {
//        super.bindPart(part);
//        if (part instanceof IModelRenderPart) {
//            modelPartList.add((IModelRenderPart) part);
//        }
//    }
//
//    @Override
//    public void partRemoved(TMultiPart part, int p) {
//        super.partRemoved(part, p);
//        if (part instanceof IModelRenderPart) {
//            modelPartList.remove(part);
//        }
//    }
//
//    @Override
//    public void clearParts() {
//        super.clearParts();
//        modelPartList.clear();
//    }
//
//    @Override
//    public boolean renderStatic(RenderType layer, CCRenderState ccrs) {
//        boolean ret = TileMultipartClient.super.renderStatic(layer, ccrs);
//        BlockRendererDispatcher rendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//
//        ILightReader world = ccrs.lightMatrix.access;
//
//        MatrixStack stack = new MatrixStack();
//        for (IModelRenderPart part : modelPartList) {
//            Random random = new Random();
//            if (part.canRenderInLayer(layer)) {
//                stack.push();
//                ret |= rendererDispatcher.renderModel(part.getCurrentState(), getPos(), world, stack, ccrs.getConsumer(), true, random, part.getModelData());
//                stack.pop();
//            }
//        }
//        return ret;
//    }
//}
