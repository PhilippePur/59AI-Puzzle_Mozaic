package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.List;
import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

public class TournamentSelection implements SelectionStrategy {

    private final int poolNumber;
    private final int k;

    public TournamentSelection(int poolNumber, int k) {
        this.poolNumber = poolNumber;
        this.k = k;
    }

    @Override
    public List<Individual> select(List<Individual> population) {
        List<Individual> matingPool = new ArrayList<>();

        for (int i = 0; i < poolNumber; i++) {
            matingPool.add(get(population));
        }

        return matingPool;
    }

    public Individual get(List<Individual> population) {
        Individual best = null;

        for (int i = 0; i < k; i++) {
            int idx = GlobalRandom.rdm.nextInt(population.size());
            Individual candidate = population.get(idx);

            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }

        return best.copy();
    }

}