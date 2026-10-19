package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class ThaumaturgeJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BlockDetailsDataProvider.INSTANCE, Block.class);
        registration.registerEntityDataProvider(GolemDataProvider.INSTANCE, EntityThaumaturgeGolem.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        JadeConfig.register(registration);
        JadeMachineIconProvider.register(registration);
        registration.addRayTraceCallback((hit, accessor, original) -> {
            if (accessor instanceof BlockAccessor blockAccessor) {
                BlockEntity controller = JadeBlockProviderHolder.resolve(blockAccessor);
                boolean fullPreview = controller instanceof BlockEntityGolemBuilder
                        || controller instanceof BlockEntityThaumatorium
                        || controller instanceof BlockEntityInfernalFurnace
                        || controller instanceof BlockEntityEldritchObelisk;
                if (fullPreview
                        || controller instanceof BlockEntityAdvancedAlchemicalFurnace furnace
                                && furnace.isAssembled()) {
                    BlockAccessor.Builder builder = registration
                            .blockAccessor()
                            .from(blockAccessor)
                            .hit(blockAccessor.getHitResult().withPosition(controller.getBlockPos()))
                            .blockState(controller.getBlockState())
                            .blockEntity(controller);
                    if (fullPreview) {
                        builder.fakeBlock(
                                new ItemStack(controller.getBlockState().getBlock()));
                    }
                    return builder.build();
                }
            }
            return accessor;
        });
        registration.registerBlockComponent(NodeComponentProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(BlockDetailsComponentProvider.INSTANCE, Block.class);
        registration.registerEntityComponent(GolemComponentProvider.INSTANCE, EntityThaumaturgeGolem.class);
    }
}
