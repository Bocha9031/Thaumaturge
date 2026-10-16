package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Deque;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

class EntryDetailBackNavigationTest {
    @Test
    void backspaceClosesAspectAndKnowledgeInserts() throws ReflectiveOperationException {
        EntryDetailScreen screen = screen();

        setBoolean(screen, "showingAspects", true);
        assertTrue(screen.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0));
        assertFalse(booleanField(screen, "showingAspects"));
        assertFalse(booleanField(screen, "showingKnowledge"));

        setBoolean(screen, "showingKnowledge", true);
        assertTrue(screen.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0));
        assertFalse(booleanField(screen, "showingAspects"));
        assertFalse(booleanField(screen, "showingKnowledge"));
    }

    @Test
    void recipeHistoryStillTakesPrecedenceOverClosingTheInsert() throws ReflectiveOperationException {
        EntryDetailScreen screen = screen();
        ResourceLocation previousRecipe = ResourceLocation.withDefaultNamespace("previous_recipe");
        ResourceLocation currentRecipe = ResourceLocation.withDefaultNamespace("current_recipe");
        Field historyField = EntryDetailScreen.class.getDeclaredField("history");
        historyField.setAccessible(true);
        Deque<?> history = (Deque<?>) historyField.get(screen);
        Method push = Deque.class.getMethod("push", Object.class);
        push.invoke(history, previousRecipe);
        setField(screen, "shownRecipe", currentRecipe);

        assertTrue(screen.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0));
        assertSame(previousRecipe, fieldValue(screen, "shownRecipe"));
    }

    private static EntryDetailScreen screen() {
        IResearchEntry entry = mock(IResearchEntry.class);
        when(entry.nameKey()).thenReturn("test.entry");
        return new EntryDetailScreen(Holder.direct(entry), ResourceLocation.withDefaultNamespace("test_entry"), null);
    }

    private static boolean booleanField(EntryDetailScreen screen, String name) throws ReflectiveOperationException {
        return (Boolean) fieldValue(screen, name);
    }

    private static Object fieldValue(EntryDetailScreen screen, String name) throws ReflectiveOperationException {
        Field field = EntryDetailScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(screen);
    }

    private static void setBoolean(EntryDetailScreen screen, String name, boolean value)
            throws ReflectiveOperationException {
        setField(screen, name, value);
    }

    private static void setField(EntryDetailScreen screen, String name, Object value)
            throws ReflectiveOperationException {
        Field field = EntryDetailScreen.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(screen, value);
    }
}
