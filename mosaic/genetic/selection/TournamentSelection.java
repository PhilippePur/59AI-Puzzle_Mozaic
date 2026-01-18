package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mosaic.puzzle.Individual;

/**
 * Implementasi Tournament Selection.
 * <p>
 * Memilih k individu secara acak, lalu mengambil yang terbaik di antaranya.
 * Metode ini sangat populer karena efisien dan mudah mengatur tekanan seleksi melalui parameter k.
 * </p>
 */
public class TournamentSelection implements SelectionStrategy {

    private final int poolNumber;
    private final int k;
    private final Random rng;

    public TournamentSelection(int poolNumber, int k, Random rng) {
        this.poolNumber = poolNumber;
        this.k = k;
        this.rng = rng;
    }

    @Override
    public List<Individual> select(List<Individual> population) {
        List<Individual> matingPool = new ArrayList<>();
        for (int i = 0; i < poolNumber; i++) {
            matingPool.add(get(population));
        }
        return matingPool;
    }

    private Individual get(List<Individual> population) {
        Individual best = null;
        for (int i = 0; i < k; i++) {
            int idx = rng.nextInt(population.size());
            Individual candidate = population.get(idx);

            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best.copy();
    }
}