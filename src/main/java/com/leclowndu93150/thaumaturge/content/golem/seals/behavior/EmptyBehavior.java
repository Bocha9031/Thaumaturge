package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class EmptyBehavior implements ISealBehavior {
    public static final SealSetting CYCLE =
            new SealSetting("cycle_whitelist", "gui.thaumaturge.seal.setting.cycle", false).readingAlso("pcycle");
    public static final SealSetting LEAVE_ONE =
            new SealSetting("leave_one", "gui.thaumaturge.seal.setting.leave", false).readingAlso("pleave");

    private static final int STAGGER = 43;
    private static final int SCAN_PERIOD = 20;
    private static final int TIDY_PERIOD = 100;
    private static final int TASK_LIFESPAN = 5;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<ItemStack> takings = new TaskLedger<>();
    private int turn;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % TIDY_PERIOD == 0) {
            takings.dropFinished(level);
        }
        if (now % SCAN_PERIOD != 0) {
            return;
        }
        IItemHandler source =
                InvHelper.handlerAt(level, seal.pos().pos(), seal.pos().face());
        if (source == null) {
            return;
        }
        ISealFilter filter = seal.filter().orElseThrow();
        ItemStack found = StackFilter.of(candidates(seal, filter), filter.isBlacklist(), ItemMatchSettings.of(seal))
                .firstAccepted(source, seal.setting(LEAVE_ONE));
        if (!found.isEmpty()) {
            Task task = Task.atBlock(seal.pos(), seal.pos().pos());
            task.setPriority(seal.priority());
            task.setLife(TASK_LIFESPAN);
            GolemDispatch.postTask(level, task);
            takings.record(task, found);
        }
    }

    private List<ItemStack> candidates(ISealEntity seal, ISealFilter filter) {
        if (!seal.setting(CYCLE) || filter.isBlacklist()) {
            return filter.stacks();
        }
        List<ItemStack> listed =
                filter.stacks().stream().filter(stack -> !stack.isEmpty()).toList();
        return listed.isEmpty() ? filter.stacks() : List.of(listed.get(cycleIndex(turn, listed.size())));
    }

    static int cycleIndex(int turn, int size) {
        return Math.floorMod(turn, size);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemStack wanted = takings.get(task);
        SealPos at = seal.pos();
        var matching = ItemMatchSettings.of(seal);
        if (wanted != null && !wanted.isEmpty() && seal.setting(LEAVE_ONE)) {
            int stored = InvHelper.countAt(level, at.pos(), at.face(), wanted, matching);
            if (stored <= wanted.getCount()) {
                wanted = wanted.copyWithCount(stored - 1);
            }
        }
        if (wanted != null && !wanted.isEmpty()) {
            int room = golem.hands().room(wanted);
            if (room > 0) {
                ItemStack taken = InvHelper.extractAt(
                        level, at.pos(), at.face(), InvHelper.capped(wanted, room), matching, false);
                ItemStack spill = golem.hands().hold(taken);
                if (!spill.isEmpty()) {
                    InvHelper.eject(
                            level, at.pos().relative(at.face()), at.face().getOpposite(), spill);
                }
                HandlingSound.play(golem, HandlingSound.HIGH);
                golem.animateSwing();
            }
        }
        takings.forget(task);
        turn++;
        task.end();
        return true;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemStack wanted = takings.get(task);
        return wanted != null && !wanted.isEmpty() && golem.hands().canTake(wanted, true);
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        takings.forget(task);
    }
}
