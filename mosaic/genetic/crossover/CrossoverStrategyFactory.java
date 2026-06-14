package mosaic.genetic.crossover;

import java.util.Map;

/**
 * Factory class untuk membuat instance strategi crossover secara dinamis.
 * <p>
 * Kelas ini menerapkan desain pattern Factory untuk mempermudah pembuatan objek
 * dan memisahkan logika Instansiasi Crossover
 * dari logika utama
 * </p>
 * 
 * @author Michael P
 */
public class CrossoverStrategyFactory {

    /** Konstruktor private untuk mencegah instansiasi */
    private CrossoverStrategyFactory() {
    }

    /**
     * Membuat strategi crossover berdasarkan tipe dan parameter yang diberikan.
     *
     * @param type   Jenis crossover terdiri dari: "uniform", "onepoint",
     *               "twopoint", "singleblock", "multiblock", "hybrid"
     * @param params Parameter konfigurasi tambahan khusus untuk MultiBlock dan Hybrid
     *               (num_blocks dan block_size)
     * @return Instance {@link CrossoverStrategy} yang sesuai dengan @param type
     * @throws IllegalArgumentException exception kalau tipe strategi tidak dikenali
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
                //untuk multiblock karena memungkinkan num_blocks empty maka diberikan default value 3 agar bentuknya 3 x 3 sebanyak 3 buah
                int numBlocksMB = (int) params.getOrDefault("num_blocks", 3);
                int blockSizeMB = (int) params.getOrDefault("block_size", 3);
                return new MultiBlockCrossover(numBlocksMB, blockSizeMB);
                
                case "hybrid":
                //untuk hybrid karena memungkinkan num_blocks empty maka diberikan default value 3 agar bentuknya 3 x 3 sebanyak 3 buah
                int numBlocksH = (int) params.getOrDefault("num_blocks", 5);
                int blockSizeH = (int) params.getOrDefault("block_size", 3);
                return new HybridCrossover(numBlocksH, blockSizeH);

            default:
                throw new IllegalArgumentException("Tipe strategi crossover tidak dikenal: " + type);
        }
    }
}