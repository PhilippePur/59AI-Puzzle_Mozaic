package mosaic.genetic.selection;

import java.util.Map;
import java.util.Random;

/**
 * Factory class untuk membuat instance strategi selection secara dinamis.
 * <p>
 * Kelas ini menerapkan desain pattern Factory untuk mempermudah pembuatan objek
 * dan memisahkan logika Instansiasi selection
 * dari logika utama
 * </p>
 * 
 * @author Michael P
 */
public class SelectionStrategyFactory {

    /** Konstruktor private untuk mencegah instansiasi*/
    private SelectionStrategyFactory() {}

    /**
     * Membuat strategi seleksi berdasarkan tipe dan parameter yang diberikan
     *
     * @param type   Jenis strategy yang tersedia: "tournament", "roulette", "rank", "stochastic", "truncation"
     * 
     * @param rng    Generator angka acak yang spesifik untuk thread/eksperimen ini 
     * @param params Parameter konfigurasi seperti ukuran pool, nilai k, pressure, dll kalau ada 
     * @return Instance {@link SelectionStrategy} yang sesuai
     * @throws IllegalArgumentException exception kalau tipe strategi tidak dikenali
     */
    public static SelectionStrategy createStrategy(String type, Random rng, Map<String, Object> params) {
        String typeLower = type.toLowerCase();
        
        // Jumlah individu yang akan dipilih, default ke 200 jika tidak ada
        int poolNumber = (int) params.getOrDefault("pool_size", 200);

        switch (typeLower) {
            case "tournament":
                int k = (int) params.getOrDefault("k", 5); 
                return new TournamentSelection(poolNumber, k, rng);

            case "roulette":
                return new RouletteSelection(poolNumber, rng);

            case "stochastic":
            case "sus": 
                return new StochasticSelection(poolNumber, rng);

            case "rank":
            case "linearrank":
                double pressure = 1.5; 
                if (params.containsKey("pressure")) {
                    pressure = Double.parseDouble(params.get("pressure").toString());
                }
                return new LinearRankSelection(pressure, poolNumber, rng);

            case "truncation":
                int portion = (int) params.getOrDefault("portion", 50);
                return new TruncationSelection(portion, poolNumber, rng);

            default:
                throw new IllegalArgumentException("Tipe strategi seleksi tidak dikenal: " + type);
        }
    }
}