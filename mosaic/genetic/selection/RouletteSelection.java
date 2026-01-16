package mosaic.genetic.selection;

import java.util.List;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

public class RouletteSelection {
    public static Individual select(List<Individual> population) {
        int size = population.size();
        double totalFitness = 0;
        for (int i = 0; i < size; i++) {
            totalFitness += population.get(i).getFitness();
        }

        double alpha = GlobalRandom.rdm.nextDouble(totalFitness);
        double iSum = 0;
        int j = 0;
        while (iSum < alpha && j < size) {
            iSum += population.get(j).getFitness();
            j++;
        }

        return population.get(j).copy();
    }
}
