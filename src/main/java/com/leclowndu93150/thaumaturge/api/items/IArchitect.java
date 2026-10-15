package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * An item that previews a set of block positions before acting.
 *
 * @deprecated use {@link AreaPreviewer}. This interface remains as a bridge for existing addons. Each old and new
 *     method set delegates to the other; implementations must override one complete set to avoid recursion.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public interface IArchitect extends AreaPreviewer {
    @Deprecated(since = "1.1.0", forRemoval = true)
    @Nullable
    default HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        return traceAnchor(stack, level, caster);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    default boolean replacesBlockHighlight(ItemStack stack) {
        return hidesBlockOutline(stack);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    default List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        return previewArea(stack, level, pos, side, player);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    default boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        return showsAxisArrows(stack, level, player, side, axis);
    }

    @Override
    @Nullable
    default HitResult traceAnchor(ItemStack stack, Level level, LivingEntity holder) {
        return aim(stack, level, holder);
    }

    @Override
    default boolean hidesBlockOutline(ItemStack stack) {
        return replacesBlockHighlight(stack);
    }

    @Override
    default List<BlockPos> previewArea(ItemStack stack, Level level, BlockPos anchor, Direction face, Player player) {
        return previewBlocks(stack, level, anchor, face, player);
    }

    @Override
    default boolean showsAxisArrows(ItemStack stack, Level level, Player player, Direction face, Direction.Axis axis) {
        return showsAxis(stack, level, player, face, axis);
    }
}
