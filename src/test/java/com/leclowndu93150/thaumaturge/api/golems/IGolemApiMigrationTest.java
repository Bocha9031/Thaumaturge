package com.leclowndu93150.thaumaturge.api.golems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

class IGolemApiMigrationTest {
    @Test
    void preferredMethodsBridgeToImplementationsUsingTheLegacyApi() {
        LegacyState state = new LegacyState();
        IGolemAPI golem = legacyImplementation(state);
        IGolemProperties build = mock(IGolemProperties.class);

        assertTrue(golem.isFighting());
        golem.animateSwing();
        golem.grantRankExperience(7);
        golem.applyBuild(build);

        assertTrue(state.swung);
        assertEquals(7, state.awardedExperience);
        assertEquals(build, state.appliedBuild);
    }

    @Test
    void deprecatedAliasesForwardToImplementationsUsingPreferredMethods() throws ReflectiveOperationException {
        ModernState state = new ModernState();
        IGolemAPI golem = modernImplementation(state);
        IGolemProperties build = mock(IGolemProperties.class);

        assertEquals(Boolean.TRUE, invokeLegacy(golem, "isInCombat"));
        invokeLegacy(golem, "swingArm");
        invokeLegacy(golem, "addRankXp", int.class, 11);
        invokeLegacy(golem, "setProperties", IGolemProperties.class, build);

        assertTrue(state.swung);
        assertEquals(11, state.awardedExperience);
        assertEquals(build, state.appliedBuild);
    }

    private static IGolemAPI legacyImplementation(LegacyState state) {
        InvocationHandler handler = (proxy, method, arguments) -> switch (method.getName()) {
            case "isInCombat" -> true;
            case "swingArm" -> {
                state.swung = true;
                yield null;
            }
            case "addRankXp" -> {
                state.awardedExperience = (int) arguments[0];
                yield null;
            }
            case "setProperties" -> {
                state.appliedBuild = (IGolemProperties) arguments[0];
                yield null;
            }
            default -> InvocationHandler.invokeDefault(proxy, method, arguments);
        };
        return (IGolemAPI)
                Proxy.newProxyInstance(IGolemAPI.class.getClassLoader(), new Class<?>[] {IGolemAPI.class}, handler);
    }

    private static IGolemAPI modernImplementation(ModernState state) {
        InvocationHandler handler = (proxy, method, arguments) -> switch (method.getName()) {
            case "isFighting" -> true;
            case "animateSwing" -> {
                state.swung = true;
                yield null;
            }
            case "grantRankExperience" -> {
                state.awardedExperience = (int) arguments[0];
                yield null;
            }
            case "applyBuild" -> {
                state.appliedBuild = (IGolemProperties) arguments[0];
                yield null;
            }
            default -> InvocationHandler.invokeDefault(proxy, method, arguments);
        };
        return (IGolemAPI)
                Proxy.newProxyInstance(IGolemAPI.class.getClassLoader(), new Class<?>[] {IGolemAPI.class}, handler);
    }

    private static Object invokeLegacy(IGolemAPI golem, String name, Class<?>... parameterTypes)
            throws ReflectiveOperationException {
        Object[] arguments = new Object[parameterTypes.length];
        return invokeLegacy(golem, name, parameterTypes, arguments);
    }

    private static Object invokeLegacy(IGolemAPI golem, String name, Class<?> parameterType, Object argument)
            throws ReflectiveOperationException {
        return invokeLegacy(golem, name, new Class<?>[] {parameterType}, new Object[] {argument});
    }

    private static Object invokeLegacy(IGolemAPI golem, String name, Class<?>[] parameterTypes, Object[] arguments)
            throws ReflectiveOperationException {
        Method method = IGolemAPI.class.getMethod(name, parameterTypes);
        return method.invoke(golem, arguments);
    }

    private static final class LegacyState {
        private boolean swung;
        private int awardedExperience;
        private IGolemProperties appliedBuild;
    }

    private static final class ModernState {
        private boolean swung;
        private int awardedExperience;
        private IGolemProperties appliedBuild;
    }
}
