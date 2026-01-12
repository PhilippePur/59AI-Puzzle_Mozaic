package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import java.util.Random;

public class BasicMutation implements MutationStrategy {

    private final double mutationRate;
    private final Random random;

    public BasicMutation(double mutationRate, Random random) {

        this.mutationRate = mutationRate;
        this.random = random;
    }

    @Override
    public void mutate(Individual individual, Puzzle puzzle) {
        if (individual == null || puzzle == null) {
            throw new IllegalArgumentException("Individual dan Puzzle tidak boleh null");
        }

        int rows = individual.getRows();
        int cols = individual.getCols();
        int mutationCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!individual.isFixed(r, c) && random.nextDouble() < mutationRate) {
                    individual.flipCell(r, c);
                    mutationCount++;
                }
            }
        }

    }

    @Override
    public String getStrategyName() {
        return "BasicMutation";
    }

}