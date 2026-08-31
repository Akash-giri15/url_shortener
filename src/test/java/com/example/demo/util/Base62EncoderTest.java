package com.example.demo.util;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    void bijectionHoldsAcrossEdgeCasesAndARange() {
        // Edge cases named explicitly, not left to chance
        long[] edgeCases = { 0L, 1L, 61L, 62L, 63L, 1_000_000L, Long.MAX_VALUE };
        for (long value : edgeCases) {
            String encoded = Base62Encoder.encode(value);
            assertEquals(value, Base62Encoder.decode(encoded),
                    "decode(encode(" + value + ")) should equal " + value);
        }

        // A wide sweep, not just hand-picked values
        for (long value = 0; value < 50_000; value++) {
            assertEquals(value, Base62Encoder.decode(Base62Encoder.encode(value)));
        }
    }

    @Test
    void oneHundredThousandIdsProduceZeroCollisions() {
        Set<String> seen = new HashSet<>();
        for (long id = 1; id <= 100_000; id++) {
            String code = Base62Encoder.encode(id);
            assertTrue(seen.add(code), "Collision detected at id=" + id + " -> " + code);
        }
        assertEquals(100_000, seen.size());
    }
}