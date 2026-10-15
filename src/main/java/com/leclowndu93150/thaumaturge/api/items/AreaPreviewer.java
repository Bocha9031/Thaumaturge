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
 * An item that can preview the blocks it would affect before it acts.
 *
 * <p>Implementations are queried on the client while held and must remain cheap and read-only. A trace anchors the
 * preview only when it returns a block hit; preview positions should be unique. The preview is drawn whether or not
 * it hides the vanilla block outline, and axis arrows indicate which extent is being adjusted.
 * A focus caster uses the first matching spell behavior as its preview source.
 *
 * @since 1.1.0
 */
public interface AreaPreviewer {
    /** Returns the current block anchor, or {@code null} when no preview can be anchored. */
    @Nullable
    HitResult traceAnchor(ItemStack stack, Level level, LivingEntity holder);

    /** Returns whether to hide the vanilla outline while a non-empty preview is shown. */
    boolean hidesBlockOutline(ItemStack stack);

    /** Returns the unique positions that would be affected at the anchor. */
    List<BlockPos> previewArea(ItemStack stack, Level level, BlockPos anchor, Direction face, Player player);

    /** Returns whether to draw adjustment arrows for this axis. */
    boolean showsAxisArrows(ItemStack stack, Level level, Player player, Direction face, Direction.Axis axis);
}
