package mosaic.genetic.crossover;

import java.util.Random;
import mosaic.puzzle.Individual;

/**
 * Interface untuk crossover strategy
 * Setiap implementasi konkrit harus menghasilkan individu baru (anak)
 * @author Andrew
 */
public interface CrossoverStrategy {
    /**
     * Melakukan crossover antara dua orang tua untuk menghasilkan satu anak
     * @param parent1 Orang tua 1
     * @param parent2 Orang tua 2
     * @param rng Generator random global untuk konsistensi seed
     * @return Anak hasil crossover
     */
    Individual crossover(Individual parent1, Individual parent2, Random rng);
}
