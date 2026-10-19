package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class FellBehaviorPortTest {
    @Test
    void endsAndReleasesAnUntrackedChopTask() {
        FellBehavior behavior = new FellBehavior();
        Task task = Task.atBlock(null, BlockPos.ZERO);

        boolean complete =
                behavior.completeTask(mock(ServerLevel.class), mock(ISealEntity.class), mock(IGolemAPI.class), task);

        assertTrue(complete);
        assertTrue(task.isEnded());
    }
}
