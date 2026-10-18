package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GolemEventPortTest {
    @Test
    void eventIdsRemainUniqueAndResolvable() {
        Set<Byte> ids = new HashSet<>();
        for (GolemEvent event : GolemEvent.values()) {
            assertTrue(ids.add(event.id()), "duplicate golem event id " + event.id());
            assertSame(event, GolemEvent.byId(event.id()));
        }
        assertNull(GolemEvent.byId((byte) 6));
    }

    @Test
    void entityEventIdsStayCompatibleWithTheEstablishedProtocol() {
        assertEquals(5, GolemEvent.TASK_CLAIMED.id());
        assertEquals(101, GolemEvent.TASK_FAILED.id());
        assertEquals(102, GolemEvent.CONFUSED.id());
        assertEquals(103, GolemEvent.STAY.id());
        assertEquals(104, GolemEvent.FOLLOW.id());
        assertEquals(105, GolemEvent.RANK_UP.id());
    }
}
