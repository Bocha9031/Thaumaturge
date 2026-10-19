package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

enum JadeMachineIconProvider implements IComponentProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = TTIds.rl("machine_icon");

    static void register(IWailaClientRegistration registration) {
        registration.registerBlockIcon(INSTANCE, BlockGolemBuilder.class);
        registration.registerBlockIcon(INSTANCE, BlockEldritchObelisk.class);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        if (accessor.getBlockState().getBlock() instanceof BlockGolemBuilder) {
            return IElementHelper.get().item(new ItemStack(TTItems.GOLEM_BUILDER.get()));
        }
        if (accessor.getBlockState().getBlock() instanceof BlockEldritchObelisk) {
            return IElementHelper.get().item(new ItemStack(TTItems.ELDRITCH_OBELISK.get()));
        }
        return currentIcon;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // This provider only replaces the icon.
    }
}
