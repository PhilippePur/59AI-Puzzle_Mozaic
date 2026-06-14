package mosaic.genetic.mutation;

import java.util.Random;
import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * Factory class yang bertugas menyediakan instance strategi mutasi secara
 * dinamis.
 * Kelas ini memisahkan logika pembuatan objek dari logika bisnis utama,
 * memungkinkan pemilihan strategi
 * hanya berdasarkan string nama dan parameter konfigurasi.
 * * @author Michael G
 */
public class MutationStrategyFactory {

    /**
     * Konstruktor privat untuk mencegah instansiasi kelas utilitas ini.
     */
    private MutationStrategyFactory() {
    }

    /**
     * Membuat dan mengembalikan strategi mutasi yang sesuai berdasarkan parameter
     * tipe.
     * Metode ini menangani parsing parameter konfigurasi dan pemilihan implementasi
     * konkret.
     *
     * @param type   Jenis strategi mutasi yang diinginkan pilihan : "basic",
     *               "adaptive" , "hybrid" , "random" , "constraint"
     * @param random Generator angka acak untuk menjamin sifat deterministik
     *               eksperimen
     * @param params Peta konfigurasi yang berisi parameter tambahan seperti rate
     *               mutasi
     * @return Objek strategi mutasi yang siap digunakan
     * @throws IllegalArgumentException jika tipe strategi tidak dikenali
     */
    public static MutationStrategy createStrategy(String type, Random random, Map<String, Object> params) {
        String typeLower = type.toLowerCase();

        // Mengambil rate dari parameter atau menggunakan nilai default jika tidak
        // tersedia
        double rate = 0.05;
        if (params != null && params.containsKey("rate")) {
            rate = (double) params.get("rate");
        }

        if ("basic".equals(typeLower)) {
            return createBasicMutation(random, rate);
        } else if ("constraint".equals(typeLower) || "constraintaware".equals(typeLower)) {
            return createConstraintAwareMutation(random, rate);
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

    /**
     * Membentuk strategi Basic Mutation yang melakukan flip bit sederhana.
     */
    private static MutationStrategy createBasicMutation(Random random, double rate) {
        return new BasicMutation(rate, random);
    }

    /**
     * Membentuk strategi Constraint Aware Mutation yang memperbaiki area
     * berdasarkan clue.
     */
    private static MutationStrategy createConstraintAwareMutation(Random random, double rate) {
        return new ConstraintAwareMutation(random, rate);
    }

    /**
     * Membentuk strategi Adaptive Mutation yang dapat berganti metode berdasarkan
     * kondisi populasi.
     * Strategi ini dibekali dengan kumpulan strategi dasar untuk fase eksplorasi
     * dan eksploitasi.
     */
    private static MutationStrategy createAdaptiveMutation(Random random, Map<String, Object> params) {
        double baseRate = (double) params.getOrDefault("rate", 0.05);

        List<MutationStrategy> baseStrategies = Arrays.asList(
                new BasicMutation(baseRate, random),
                new BasicMutation(baseRate, random),
                new ConstraintAwareMutation(random, baseRate));

        return new AdaptiveMutation(baseStrategies);
    }

    /**
     * Membentuk strategi Hybrid yang menggabungkan Basic dan Constraint mutation
     * secara probabilistik.
     * Berguna untuk menyeimbangkan antara pengacakan murni dan perbaikan terarah.
     */
    private static MutationStrategy createHybridMutation(Random random, Map<String, Object> params) {
        double rate = (double) params.getOrDefault("rate", 0.05);

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

    /**
     * Membentuk strategi Random Mutation yang melakukan berbagai jenis mutasi acak
     * (sel, blok, baris, kolom)
     */
    private static MutationStrategy createRandomMutation(Random random, double rate) {
        return new MutationStrategy() {
            @Override
            public void mutate(Individual individual, Puzzle puzzle) {

                // memberi kesempatan dengan kemungkinan kecil untuk terjadi mutasi
                if (random.nextDouble() > rate)
                    return;

                double choice = random.nextDouble();

                if (choice < 0.33) {
                    // mengubah satu sel acak
                    int r = random.nextInt(individual.getRows());
                    int c = random.nextInt(individual.getCols());
                    if (!individual.isFixed(r, c)) {
                        individual.flipCell(r, c);
                    }
                } else if (choice < 0.66) {
                    // mengubah satu blok area 3x3 acak
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
                    // mengubah satu baris atau kolom secara acak
                    if (random.nextBoolean()) {
                        int r = random.nextInt(individual.getRows());
                        for (int c = 0; c < individual.getCols(); c++) {
                            if (!individual.isFixed(r, c))
                                individual.flipCell(r, c);
                        }
                    } else {
                        int c = random.nextInt(individual.getCols());
                        for (int r = 0; r < individual.getRows(); r++) {
                            if (!individual.isFixed(r, c))
                                individual.flipCell(r, c);
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