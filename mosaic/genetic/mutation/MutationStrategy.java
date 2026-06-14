package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;

/**
 * Interface Mutation Strategy yang memiliki method 
 * -{@link #mutate(Individual, Puzzle)} yaitu method untuk memutasi dan harus ada yang diubah pada individu tersebut 
 * -{@link #getStrategyName() yaitu method untuk mendapat nama dari strategy} 
 * @author Michael G
 */
public interface MutationStrategy { 
    void mutate(Individual individual, Puzzle puzzle);

    String getStrategyName();
}