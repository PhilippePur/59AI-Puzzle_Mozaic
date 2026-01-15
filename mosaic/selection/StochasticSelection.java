import java.util.List;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

public class StochasticSelection {
    public static Individual select(List<Individual> population) {
        int size = population.size();
        double totalFitness = 0;
        for (int i = 0; i < size; i++) {
            totalFitness += population.get(i).getFitness();
        }
        double mean = totalFitness / size;
        double alpha = GlobalRandom.rdm.nextDouble(1);
        double sum = population.get(0).getFitness();
        double delta = alpha * mean;
        for (int i = 0; i < size; i++) {
            if(delta < sum)
        }
    }
}
