package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mosaic.util.GlobalRandom;

/**
 * Strategi mutasi adaptif yang menyesuaikan perilaku mutasi berdasarkan kondisi
 * evolusi saat ini.
 * Strategi ini membagi proses evolusi ke dalam beberapa fase (eksplorasi,
 * seimbang, eksploitasi)
 * dan memilih metode mutasi yang paling sesuai untuk fase tersebut berdasarkan
 * metrik populasi
 * seperti diversity dan average fitness.
 * @author Michael G
 */
public class AdaptiveMutation implements MutationStrategy {

    private final List<MutationStrategy> availableStrategies;
    private final Map<String, MutationStrategy> strategyMap;
    private final Random random;
    private int currentGeneration;
    private double averageFitness;
    private double diversity;

    /**
     * Menginisialisasi strategi adaptif dengan daftar strategi dasar yang tersedia.
     * Membangun peta strategi untuk pencarian yang efisien saat runtime.
     *
     * @param strategies daftar strategi mutasi dasar yang akan digunakan secara
     *                   bergantian.
     */
    public AdaptiveMutation(List<MutationStrategy> strategies) {
        this.availableStrategies = new ArrayList<>(strategies);
        this.strategyMap = new HashMap<>();

        for (MutationStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyName(), strategy);
        }
        this.currentGeneration = 0;
        this.averageFitness = 0.0;
        this.diversity = 1.0;
        this.random = GlobalRandom.rdm;
    }

    /**
     * Update statistik populasi saat ini
     * Metode ini akan dipanggil oleh GeneticAlgorithm pada setiap generasi agar
     * keputusan adaptasi didasarkan pada data yang akurat.
     *
     * @param generation nomor generasi saat ini.
     * @param avgFitness rata-rata fitness populasi.
     * @param diversity  nilai diversitas populasi (0.0 - 1.0).
     */
    public void updatePopulationInfo(int generation, double avgFitness, double diversity) {
        this.currentGeneration = generation;
        this.averageFitness = avgFitness;
        this.diversity = Math.max(0.0, Math.min(1.0, diversity));
    }

    /**
     * Menjalankan mutasi pada individu dengan meneruskan tugas ke strategi yang
     * dipilih berdasarkan kondisi evolusi saat ini.
     */
    @Override
    public void mutate(Individual individual, Puzzle puzzle) {
        MutationStrategy selectedStrategy = selectStrategyBasedOnConditions();
        selectedStrategy.mutate(individual, puzzle);
    }

    /**
     * Menentukan strategi mana yang akan digunakan berdasarkan fase generasi.
     * Fase dibagi menjadi: Eksplorasi / tahap awal, Seimbang / tahap tengah, dan
     * Eksploitasi / tahap akhir.
     */
    private MutationStrategy selectStrategyBasedOnConditions() {
        if (currentGeneration < 50) {
            return selectForExplorationPhase();
        }

        if (currentGeneration < 200) {
            return selectForBalancedPhase();
        }

        return selectForExploitationPhase();
    }

    /**
     * Memilih strategi untuk fase eksplorasi.
     * Jika diversitas terlalu rendah, dipaksa menggunakan BasicMutation untuk
     * menambah variasi.
     * Jika tidak, memilih strategi secara acak untuk menjaga keragaman.
     */
    private MutationStrategy selectForExplorationPhase() {
        if (diversity < 0.3) {
            return findStrategyByName("BasicMutation");
        }
        return availableStrategies.get(random.nextInt(availableStrategies.size()));
    }

    /**
     * Memilih strategi untuk fase seimbang.
     * Memberikan peluang yang sama untuk menggunakan ConstraintAwareMutation dan
     * BasicMutation.
     */
    private MutationStrategy selectForBalancedPhase() {
        double rand = random.nextDouble();

        if (rand > 0.5) {
            return findStrategyByName("ConstraintAwareMutation");
        } else {
            return findStrategyByName("BasicMutation");
        }
    }

    /**
     * Memilih strategi untuk fase eksploitasi.
     * Jika rata-rata fitness sudah tinggi, memprioritaskan ConstraintAwareMutation
     * untuk
     * penyelesaian akhir (fine-tuning).
     */
    private MutationStrategy selectForExploitationPhase() {
        if (averageFitness > 0.8) {
            return findStrategyByName("ConstraintAwareMutation");
        } else {
            double rand = random.nextDouble();

            if (rand < 0.5) {
                return findStrategyByName("ConstraintAwareMutation");
            } else {
                return findStrategyByName("BasicMutation");
            }
        }
    }

    /**
     * Mencari strategi berdasarkan nama dari peta strategi yang telah diindeks.
     * Mengembalikan strategi default jika nama tidak ditemukan.
     */
    private MutationStrategy findStrategyByName(String name) {
        MutationStrategy strategy = strategyMap.get(name);
        return (strategy != null) ? strategy : availableStrategies.get(0);
    }

    public int getStrategyCount() {
        return availableStrategies.size();
    }

    @Override
    public String getStrategyName() {
        return "AdaptiveMutation";
    }
}