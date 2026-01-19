package mosaic.util;

import java.util.*;

/**
 * Kelas yang menyediakan Random Number Generator global.
 * <p>
 * Digunakan untuk memastikan hanya 1 Random Number Generator yang digunakan dan
 * dapat direproduksi (deterministic) jika seed-nya diatur.
 * </p>
 */
public class GlobalRandom {
    /** Seed default. */
    public static final long seed = 12345L;

    /** Instance Random global yang dapat diakses oleh semua kelas */
    public static final Random rdm = new Random(seed);
}
