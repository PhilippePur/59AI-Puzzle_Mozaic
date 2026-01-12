package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;

public interface MutationStrategy { // di kontrol sama adaptive mutation
    void mutate(Individual individual, Puzzle puzzle);

    String getStrategyName();
}