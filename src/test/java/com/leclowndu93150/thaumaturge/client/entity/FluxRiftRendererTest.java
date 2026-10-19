package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class FluxRiftRendererTest {
    @Test
    void dedicatedOpaqueCoreTypeIsUsedOnlyAfterTheGoggleAwareGlowPasses() {
        assertSame(FluxRiftRenderer.renderTypeForPass(0, false), FluxRiftRenderer.renderTypeForPass(0, true));
        assertNotSame(FluxRiftRenderer.renderTypeForPass(2, false), FluxRiftRenderer.renderTypeForPass(2, true));

        var core = FluxRiftRenderer.renderTypeForPass(3, false);
        assertSame(core, FluxRiftRenderer.renderTypeForPass(3, true));
        assertSame(core, FluxRiftRenderer.renderTypeForPass(4, false));
        assertNotSame(core, FluxRiftRenderer.renderTypeForPass(2, true));
    }
}
