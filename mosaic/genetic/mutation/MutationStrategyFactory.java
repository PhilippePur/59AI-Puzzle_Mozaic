package mosaic.genetic.mutation;

import java.util.Random;
import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * Factory class untuk membuat instance strategi mutasi secara dinamis.
 * <p>
 * Kelas ini memisahkan logika pembuatan objek dari logika bisnis utama,
 * memungkinkan pemilihan strategi mutasi berdasarkan string nama dan parameter konfigurasi.
 * </p>
 * @author Michael G
 */
public class MutationStrategyFactory {

    /** Konstruktor private untuk mencegah instansiasi. */
    private MutationStrategyFactory() {}

    /**
     * Membuat strategi mutasi berdasarkan tipe dan parameter yang diberikan.
     *
     * @param type   Jenis strategi mutasi (contoh: "basic", "constraint", "adaptive"). Tidak case-sensitive.
     * @param random Generator angka acak yang spesifik untuk thread/eksperimen ini.
     * @param params Map yang berisi parameter konfigurasi (wajib berisi "rate" untuk sebagian besar strategi).
     * @return Instance {@link MutationStrategy} yang sesuai.
     * @throws IllegalArgumentException jika tipe strategi tidak dikenali.
     */
    public static MutationStrategy createStrategy(String type, Random random, Map<String, Object> params) {
        String typeLower = type.toLowerCase();

        // Ambil rate dari parameter, default 0.05 jika tidak ada
        double rate = 0.05;
        if (params != null && params.containsKey("rate")) {
            rate = (double) params.get("rate");
        }

        if ("basic".equals(typeLower)) {
            return createBasicMutation(random, params);
        } else if ("constraint".equals(typeLower) || "constraintaware".equals(typeLower)) {
            return createConstraintAwareMutation(random, rate); // Pass rate here!
        } else if ("adaptive".equals(typeLower)) {
            return createAdaptiveMutation(random, params);
        } else if ("hybrid".equals(typeLower)) {
            return createHybridMutation(random, params);
        } else if ("random".equals(typeLower)) {
            return createRandomMutation(random, rate);
        } else {
            throw new IllegalArgumentException("Unknown mutation strategy: " + type);
        }
    }

    /** Membuat strategi Basic Bit-Flip Mutation. */
    private static MutationStrategy createBasicMutation(Random random, Map<String, Object> params) {
        double rate = (double) params.getOrDefault("rate", 0.05);
        return new BasicMutation(rate, random);
    }

    /** Membuat strategi Constraint Aware Mutation dengan rate spesifik. */
    private static MutationStrategy createConstraintAwareMutation(Random random, double rate) {
        return new ConstraintAwareMutation(random, rate);
    }

    /** Membuat strategi Adaptive Mutation yang mengelola beberapa strategi dasar. */
    private static MutationStrategy createAdaptiveMutation(Random random, Map<String, Object> params) {
        // Adaptive mutation strategy pool
        List<MutationStrategy> baseStrategies = Arrays.asList(
                new BasicMutation(0.05, random),
                new BasicMutation(0.1, random),
                new ConstraintAwareMutation(random, 0.05)); // Default rate untuk komponen adaptive

        return new AdaptiveMutation(baseStrategies);
    }

    /** Membuat strategi Hybrid yang menggabungkan Basic dan Constraint mutation secara probabilistik. */
    private static MutationStrategy createHybridMutation(Random random, Map<String, Object> params) {
        double rate = (double) params.getOrDefault("rate", 0.05);
        
        // Hybrid: 50% basic, 50% constraint-aware
        return new MutationStrategy() {
            @Override
            public void mutate(Individual individual, Puzzle puzzle) {
                if (random.nextDouble() < 0.5) {
                    new BasicMutation(rate, random).mutate(individual, puzzle);
                } else {
                    new ConstraintAwareMutation(random, rate).mutate(individual, puzzle);
                }
            }

            @Override
            public String getStrategyName() {
                return "HybridMutation";
            }
        };
    }

    /** Membuat strategi Random Mutation (sel, blok, baris/kolom acak) yang menghormati rate. */
    private static MutationStrategy createRandomMutation(Random random, double rate) {
        return new MutationStrategy() {
            @Override
            public void mutate(Individual individual, Puzzle puzzle) {
                // Cek rate dulu (Gatekeeper)
                if (random.nextDouble() > rate) return;

                double choice = random.nextDouble();
                if (choice < 0.33) {
                    // Flip single random cell
                    int r = random.nextInt(individual.getRows());
                    int c = random.nextInt(individual.getCols());
                    if (!individual.isFixed(r, c)) {
                        individual.flipCell(r, c);
                    }
                } else if (choice < 0.66) {
                    // Flip 3x3 block
                    int centerR = 1 + random.nextInt(individual.getRows() - 2);
                    int centerC = 1 + random.nextInt(individual.getCols() - 2);
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int r = centerR + dr;
                            int c = centerC + dc;
                            if (r >= 0 && r < individual.getRows() &&
                                    c >= 0 && c < individual.getCols() &&
                                    !individual.isFixed(r, c)) {
                                individual.flipCell(r, c);
                            }
                        }
                    }
                } else {
                    // Flip random row/col
                    if (random.nextBoolean()) {
                        int r = random.nextInt(individual.getRows());
                        for (int c = 0; c < individual.getCols(); c++) {
                            if (!individual.isFixed(r, c)) individual.flipCell(r, c);
                        }
                    } else {
                        int c = random.nextInt(individual.getCols());
                        for (int r = 0; r < individual.getRows(); r++) {
                            if (!individual.isFixed(r, c)) individual.flipCell(r, c);
                        }
                    }
                }
            }

            @Override
            public String getStrategyName() {
                return "RandomMutation";
            }
        };
    }
}