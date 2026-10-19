package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ResearchConstructCategory implements IRecipeCategory<ResearchConstructRecipe> {
    public static final RecipeType<ResearchConstructRecipe> RECIPE_TYPE =
            RecipeType.create(TTIds.MODID, "research_construct", ResearchConstructRecipe.class);

    private static final int WIDTH = 144;
    private static final int HEIGHT = 136;
    private final IDrawable icon;

    public ResearchConstructCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TTItems.THAUMONOMICON.get()));
    }

    @Override
    public RecipeType<ResearchConstructRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.thaumaturge.recipe_type.construct");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ResearchConstructRecipe recipe, IFocusGroup focuses) {
        int materialIndex = 0;
        for (Map.Entry<String, Integer> material : recipe.materialCounts().entrySet()) {
            int row = materialIndex / 6;
            int column = materialIndex % 6;
            List<ItemStack> representations = constructMaterials(material.getKey());
            representations.forEach(stack -> stack.setCount(material.getValue()));
            if (!representations.isEmpty()) {
                builder.addInputSlot(5 + column * 22, 78 + row * 20).addItemStacks(representations);
                materialIndex++;
            }
        }

        recipe.entry().icons().stream()
                .filter(icon -> icon.kind() == ResearchIcon.Kind.ITEM)
                .map(ResearchIcon::id)
                .map(BuiltInRegistries.ITEM::get)
                .filter(item -> item != Items.AIR)
                .findFirst()
                .ifPresent(item -> builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 20, 38)
                        .addItemStack(new ItemStack(item)));

        List<AspectInstance> cost = recipe.construct().cost().entries();
        int stride = 20;
        int totalWidth = cost.isEmpty() ? 0 : stride * (cost.size() - 1) + 16;
        int x = WIDTH / 2 - totalWidth / 2;
        for (int i = 0; i < cost.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, x + i * stride, HEIGHT - 18)
                    .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                    .addIngredient(AspectIngredientType.INSTANCE, cost.get(i));
        }
    }

    @Override
    public void draw(
            ResearchConstructRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        Map<BlockPos, BlockState> blocks = constructBlocks(recipe.construct());
        RecipeDisplayWidget.renderBlockPreview(
                graphics, WIDTH / 2.0F - 6, 40.0F, blocks, 110.0F, 66.0F, 12.0F, (Util.getMillis() % 36_000L) / 100.0F);
        Component title = Component.translatable(recipe.entry().nameKey());
        graphics.drawString(Minecraft.getInstance().font, title, 4, 4, 0x404040, false);
        if (!doesPassGate(recipe)) {
            graphics.renderItem(Items.BARRIER.getDefaultInstance(), WIDTH - 20, 38);
        }
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            ResearchConstructRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY) {
        tooltip.add(Component.translatable(recipe.entry().nameKey()));
        if (!recipe.construct().cost().isEmpty()) {
            var cost = Component.empty();
            List<AspectInstance> entries = recipe.construct().cost().entries();
            for (int i = 0; i < entries.size(); i++) {
                AspectInstance instance = entries.get(i);
                if (i > 0) {
                    cost.append(Component.literal(", "));
                }
                cost.append(AspectComponents.name(instance.aspect())
                        .withStyle(style ->
                                style.withColor(instance.aspect().value().color())));
                cost.append(Component.literal(" " + instance.amount()));
            }
            tooltip.add(Component.translatable("gui.thaumaturge.thaumonomicon.research_cost")
                    .append(Component.literal(" "))
                    .append(cost));
        }
        if (!doesPassGate(recipe) && mouseX > WIDTH - 22 && mouseX < WIDTH - 2 && mouseY > 36 && mouseY < 56) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(researchGate(recipe)));
        }
    }

    private static ResearchGate researchGate(ResearchConstructRecipe recipe) {
        return new ResearchGate(recipe.entryId(), Optional.of(recipe.stageIndex()), false);
    }

    private static boolean doesPassGate(ResearchConstructRecipe recipe) {
        var player = Minecraft.getInstance().player;
        return player != null && ResearchGate.passes(player, researchGate(recipe));
    }

    private static Map<BlockPos, BlockState> constructBlocks(ResearchConstruct construct) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        int index = 0;
        for (int y = 0; y < construct.ySize(); y++) {
            for (int z = construct.zSize() - 1; z >= 0; z--) {
                for (int x = construct.xSize() - 1; x >= 0; x--) {
                    ItemStack stack = resolveCell(construct.cells().get(index++));
                    Block block = Block.byItem(stack.getItem());
                    if (block != Blocks.AIR) {
                        blocks.put(new BlockPos(x, construct.ySize() - y - 1, z), block.defaultBlockState());
                    }
                }
            }
        }
        return blocks;
    }

    private static List<ItemStack> constructMaterials(String spec) {
        if (spec.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(spec.substring(1)));
            List<ItemStack> items = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> items.add(new ItemStack(holder)));
            return items;
        }
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(spec));
        return item == Items.AIR ? List.of() : List.of(new ItemStack(item));
    }

    private static ItemStack resolveCell(String spec) {
        if (spec.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (spec.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(spec.substring(1)));
            List<ItemStack> items = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> items.add(new ItemStack(holder)));
            return items.isEmpty() ? ItemStack.EMPTY : items.get(0);
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(spec)));
    }
}
