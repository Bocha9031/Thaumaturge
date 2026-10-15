package com.leclowndu93150.thaumaturge.api.infusion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Lets a block take part in the infusion altar's stability survey as a stabiliser.
 *
 * <p>The survey checks every column within 8 blocks of the matrix on both horizontal axes,
 * except the matrix column itself, from 3 blocks above the matrix down to 7 blocks below it. A
 * position counts as a stabiliser when its block is in {@code thaumaturge:infusion_stabilisers},
 * or when its block implements this interface and {@link #stabilisesNow} returns {@code true}.
 * This test only selects candidate positions. Tagged blocks that do not implement this interface
 * use {@link #STANDARD_PAIR_VALUE} and never report a symmetry penalty.
 *
 * <p>Each selected block is paired with the block at the mirrored position at the same height.
 * The mirror is scored by its own identity, pair value, and penalty hooks, whether or not it
 * currently stabilises. Different identities or pair values subtract the larger value and mark
 * the candidate as a problem. Matching pairs that report a symmetry penalty subtract the larger
 * penalty and mark the candidate as a problem. Other matching pairs add their value, multiplied
 * by 0.75 for every earlier matched, unpenalised pair of that identity in the survey.
 *
 * @apiNote Only {@link #stabilisesNow} has no default. Implementations are intended for
 *     {@link Block} subclasses.
 * @implNote Members are called on the server thread and may be asked about thousands of positions
 *     per survey. Implementations must be cheap, must only read the world, must not schedule ticks,
 *     and must not retain the passed level.
 * @since 1.1.0
 */
public interface AltarStabiliser {
    /** Value of one matched pair of ordinary stabilisers before diminishing returns. */
    float STANDARD_PAIR_VALUE = 0.1F;

    /** Penalty returned by {@link #penaltyValue} by default. */
    float NO_PENALTY = 0.0F;

    /**
     * Tests whether this block acts as a stabiliser at the moment of the survey. A false result
     * excludes it as a candidate, but it can still be scored as another candidate's mirror.
     */
    boolean stabilisesNow(Level level, BlockPos pos);

    /**
     * Returns the stability added by one matched pair before diminishing returns. Matching blocks
     * must return exactly the same finite, non-negative value. Defaults to
     * {@link #STANDARD_PAIR_VALUE}.
     */
    default float pairValue(Level level, BlockPos pos) {
        return STANDARD_PAIR_VALUE;
    }

    /**
     * Returns the block identity used for mirror matching and diminishing returns. A container
     * may return the block it holds. The default is the block at the position and must not be null.
     */
    default Block countsAs(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock();
    }

    /**
     * Tests whether this matching pair must be penalised. The survey asks both sides, with the
     * other side's position as {@code mirror}, and only asks after identity and pair values match.
     * Defaults to {@code false}.
     */
    default boolean penalisesPairWith(Level level, BlockPos pos, BlockPos mirror) {
        return false;
    }

    /**
     * Returns the finite, non-negative stability subtracted when a pair involving this block is
     * penalised. Defaults to {@link #NO_PENALTY}.
     */
    default float penaltyValue(Level level, BlockPos pos) {
        return NO_PENALTY;
    }
}
