package mosaic.genetic.crossover;

import java.util.Map;

/**
 * Factory class untuk membuat instance strategi crossover secara dinamis.
 * <p>
 * Kelas ini menerapkan pola desain Factory Method untuk memisahkan logika pembuatan objek
 * dari logika bisnis utama. Memungkinkan pemilihan strategi crossover berdasarkan string nama
 * dan parameter konfigurasi.
 * </p>
 */
public class CrossoverStrategyFactory {

    /** Konstruktor private untuk mencegah instansiasi. */
    private CrossoverStrategyFactory() {}

    /**
     * Membuat strategi crossover berdasarkan tipe dan parameter yang diberikan.
     *
     * @param type   Jenis crossover contoh: "uniform", "onepoint", "twopoint", "singleblock", "multiblock"
     * Tidak case-sensitive.
     * @param params Parameter konfigurasi tambahan khususnya untuk MultiBlock (num_blocks dan block_Size)
     * @return Instance {@link CrossoverStrategy} yang sesuai
     * @throws IllegalArgumentException exception kalau tipe strategi tidak dikenali.
     */
    public static CrossoverStrategy createStrategy(String type, Map<String, Object> params) {
        String typeLower = type.toLowerCase();

        switch (typeLower) {
            case "uniform":
                return new UniformCrossover();

            case "onepoint":
            case "one_point":
                return new OnePointCrossover();

            case "twopoint":
            case "two_point":
                return new TwoPointCrossover();

            case "singleblock":
            case "single_block":
                return new SingleBlockCrossover();

            case "multiblock":
            case "multi_block":
                int numBlocks = (int) params.getOrDefault("num_blocks", 3);
                int blockSize = (int) params.getOrDefault("block_size", 3);
                return new MultiBlockCrossover(numBlocks, blockSize);

            default:
                throw new IllegalArgumentException("Tipe strategi crossover tidak dikenal: " + type);
        }
    }
}