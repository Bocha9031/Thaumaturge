package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;

class CollectBehaviorPortTest {
    @Test
    void untrackedEntityTaskIsRejectedAndEnded() {
        CollectBehavior behavior = new CollectBehavior();
        Task task = Task.onEntity(null, mock(Entity.class));

        boolean canPerform = behavior.canPerform(mock(ISealEntity.class), mock(IGolemAPI.class), task);

        assertFalse(canPerform);
        assertTrue(task.isEnded());
    }
}
