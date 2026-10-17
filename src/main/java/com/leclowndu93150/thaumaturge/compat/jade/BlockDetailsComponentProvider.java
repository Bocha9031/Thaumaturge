package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum BlockDetailsComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final int DEFAULT_PRIORITY = 1100;

    @Override
    public ResourceLocation getUid() {
        return BlockDetailsDataProvider.UID;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public int getDefaultPriority() {
        return DEFAULT_PRIORITY;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        replaceDynamicTreeBranchName(tooltip, accessor);
        TTNbt.read(accessor.getServerData(), BlockDetailsDataProvider.DATA, JadeBlockDetails.CODEC)
                .ifPresent(data -> {
                    if (!JadeConfig.shouldShow(config, data.option(), accessor)) {
                        if (data.hideFluid()) tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
                        return;
                    }
                    data.title()
                            .ifPresent(title -> tooltip.replace(
                                    JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(title)));
                    data.summary().forEach(tooltip::add);
                    if (accessor.showDetails()) data.details().forEach(tooltip::add);
                });
    }

    private static void replaceDynamicTreeBranchName(ITooltip tooltip, BlockAccessor accessor) {
        ResourceLocation blockId =
                BuiltInRegistries.BLOCK.getKey(accessor.getBlockState().getBlock());
        if (!blockId.getNamespace().equals("dynamictrees")) return;
        String path = blockId.getPath().replace('.', '_');
        Component name = null;
        if (path.contains("silverwood") && path.contains("branch")) {
            name = Component.translatable(
                    path.contains("stripped")
                            ? "block.thaumaturge.stripped_log_silverwood"
                            : "block.thaumaturge.log_silverwood");
        } else if (path.contains("greatwood") && path.contains("branch")) {
            name = Component.translatable(
                    path.contains("stripped")
                            ? "block.thaumaturge.stripped_log_greatwood"
                            : "block.thaumaturge.log_greatwood");
        }
        if (name != null) {
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(name));
        }
    }
}
