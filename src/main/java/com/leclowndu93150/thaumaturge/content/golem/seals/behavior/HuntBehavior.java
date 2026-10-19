package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

abstract class HuntBehavior implements ISealBehavior {
    private static final int MARK_LIFESPAN = 10;

    protected abstract boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target);

    protected void onHuntOver() {}

    protected static void mark(ServerLevel level, ISealEntity seal, LivingEntity target) {
        Task task = Task.onEntity(seal.pos(), target);
        task.setPriority(seal.priority());
        task.setLife(MARK_LIFESPAN);
        GolemDispatch.postTask(level, task);
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (task.entity() instanceof LivingEntity target
                && isQuarry(level, seal, target)
                && golem.asEntity() instanceof Mob hunter) {
            hunter.setTarget(target);
            golem.grantRankExperience(1);
        }
        task.end();
        onHuntOver();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        task.end();
        onHuntOver();
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        onHuntOver();
    }

    @Override
    public void onRemoved(ServerLevel level, ISealEntity seal) {
        onHuntOver();
    }
}
