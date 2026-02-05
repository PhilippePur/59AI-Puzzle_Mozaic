package mosaic.genetic.crossover;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mosaic.puzzle.Individual;

/**
 * Strategi crossover hybrid yang memilih secara acak salah satu dari beberapa strategi crossover.
 * <p>
 * Strategi yang mungkin terpilih adalah: MultiBlock, OnePoint, SingleBlock, dan TwoPoint.
 * Setiap strategi memiliki peluang 25% untuk terpilih
 * </p>
 * @author Andrew
 */
public class HybridCrossover implements CrossoverStrategy {

    private final List<CrossoverStrategy> strategies;

    /**
     * Konstruktor untuk HybridCrossover.
     * Menginisialisasi strategi yang akan digunakan secara acak.
     * 
     * @param numBlocks parameter untuk MultiBlockCrossover
     * @param blockSize parameter untuk MultiBlockCrossover
     */
    public HybridCrossover(int numBlocks, int blockSize) {
        strategies = new ArrayList<>();
        strategies.add(new MultiBlockCrossover(numBlocks, blockSize));
        strategies.add(new OnePointCrossover());
        strategies.add(new SingleBlockCrossover());
        strategies.add(new TwoPointCrossover());
    }

    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        // Memilih satu strategi secara acak dengan peluang yang sama (25% masing-masing)
        int index = rng.nextInt(strategies.size());
        CrossoverStrategy selectedStrategy = strategies.get(index);

        return selectedStrategy.crossover(parent1, parent2, rng);
    }
}
