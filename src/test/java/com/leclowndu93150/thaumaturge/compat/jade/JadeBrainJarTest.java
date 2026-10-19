package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJarBrain;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

class JadeBrainJarTest {
    @Test
    void displaysStoredExperienceAndCapacity() {
        BlockEntityJarBrain jar = mock(BlockEntityJarBrain.class);
        when(jar.xp()).thenReturn(275);
        JadeDetailBuilder builder = new JadeDetailBuilder(JadeConfig.BRAIN_JARS, mock(Player.class));

        JadeEssentiaDetails.brainJar(jar, builder);

        JadeBlockDetails details = builder.build();
        assertEquals(JadeConfig.BRAIN_JARS, details.option());
        assertEquals(
                List.of(Component.translatable(
                        "jade.thaumaturge.brain_jar.experience", 275, BlockEntityJarBrain.XP_MAX)),
                details.summary());
    }
}
