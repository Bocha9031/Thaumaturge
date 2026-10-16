package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class DynamicTreesJoCodeResourceTest {
    private static final String[] SPECIES = {"greatwood", "silverwood"};
    private static final String[] SOURCE_SHA_256 = {
        "9e34109625e8b3cfd0d57a1dd26db477725a3cfb65c937e6c2d8b188b9cfb5c5",
        "392fa2eee71dd26517d38fc0ed05bed7166142d26bb8ea066e6bf4cf3404db19"
    };

    @Test
    void speciesJoCodesAreOnClasspathWithPinnedSourceBytes() throws Exception {
        ClassLoader classLoader = DynamicTreesJoCodeResourceTest.class.getClassLoader();

        for (int speciesIndex = 0; speciesIndex < SPECIES.length; speciesIndex++) {
            String resourcePath = "trees/thaumaturge/jo_codes/" + SPECIES[speciesIndex] + ".txt";
            byte[] bytes;
            try (InputStream resource = classLoader.getResourceAsStream(resourcePath)) {
                assertNotNull(resource, resourcePath);
                bytes = resource.readAllBytes();
            }

            assertEquals(SOURCE_SHA_256[speciesIndex], sha256(bytes), resourcePath);
        }
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        return HexFormat.of().formatHex(digest);
    }
}
