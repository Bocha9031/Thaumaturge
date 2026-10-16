package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ThaumonomiconBrowserVisibilityCycleTest {
    @Test
    void parentCycleIsVisibleAndRepeatedChecksFinish() throws ReflectiveOperationException {
        ThaumonomiconBrowserScreen screen = new ThaumonomiconBrowserScreen();
        PlayerResearch knowledge = mock(PlayerResearch.class);
        ResourceLocation firstId = ResourceLocation.fromNamespaceAndPath("test", "first");
        ResourceLocation secondId = ResourceLocation.fromNamespaceAndPath("test", "second");
        IResearchEntry first = entryWithParents(Set.of(ResearchParent.of(secondId)));
        IResearchEntry second = entryWithParents(Set.of(ResearchParent.of(firstId)));
        Object firstNode = node(firstId, first);
        Object secondNode = node(secondId, second);
        addNode(screen, firstNode);
        addNode(screen, secondNode);

        assertTrue(isVisible(screen, knowledge, firstNode));
        assertTrue(isVisible(screen, knowledge, firstNode));
    }

    @Test
    void acyclicParentChainRemainsVisible() throws ReflectiveOperationException {
        ThaumonomiconBrowserScreen screen = new ThaumonomiconBrowserScreen();
        PlayerResearch knowledge = mock(PlayerResearch.class);
        ResourceLocation parentId = ResourceLocation.fromNamespaceAndPath("test", "parent");
        ResourceLocation childId = ResourceLocation.fromNamespaceAndPath("test", "child");
        IResearchEntry parent = entryWithParents(Set.of());
        IResearchEntry child = entryWithParents(Set.of(ResearchParent.of(parentId)));
        Object parentNode = node(parentId, parent);
        Object childNode = node(childId, child);
        addNode(screen, parentNode);
        addNode(screen, childNode);

        assertTrue(isVisible(screen, knowledge, childNode));
    }

    private static IResearchEntry entryWithParents(Set<ResearchParent> parents) {
        IResearchEntry entry = mock(IResearchEntry.class);
        when(entry.parents()).thenReturn(parents);
        return entry;
    }

    private static Object node(ResourceLocation id, IResearchEntry entry) throws ReflectiveOperationException {
        Class<?> nodeType = Class.forName(
                "com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen$EntryNode");
        Constructor<?> constructor = nodeType.getDeclaredConstructor(
                ResourceLocation.class, IResearchEntry.class, Holder.class, Holder.Reference.class);
        constructor.setAccessible(true);
        return constructor.newInstance(id, entry, Holder.direct(entry), null);
    }

    private static void addNode(ThaumonomiconBrowserScreen screen, Object node) throws ReflectiveOperationException {
        Field allEntries = ThaumonomiconBrowserScreen.class.getDeclaredField("allEntries");
        allEntries.setAccessible(true);
        Method add = List.class.getMethod("add", Object.class);
        add.invoke(allEntries.get(screen), node);
    }

    private static boolean isVisible(ThaumonomiconBrowserScreen screen, PlayerResearch knowledge, Object node)
            throws ReflectiveOperationException {
        Class<?> nodeType = node.getClass();
        Method isVisible =
                ThaumonomiconBrowserScreen.class.getDeclaredMethod("isVisible", PlayerResearch.class, nodeType);
        isVisible.setAccessible(true);
        return (Boolean) isVisible.invoke(screen, knowledge, node);
    }
}
