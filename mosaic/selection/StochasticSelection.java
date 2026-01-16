import java.util.ArrayList;
import java.util.List;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

public class StochasticSelection {
    public static List<Individual> select(List<Individual> population, int poolNumber) {
        // ini bagian findPointers
        List<Individual> matingPool = new ArrayList<>();
        int size = population.size();
        double totalFitness = 0;
        for (int i = 0; i < size; i++) {
            totalFitness += population.get(i).getFitness();
        }

        double dist = totalFitness / poolNumber;
        double start = GlobalRandom.rdm.nextDouble(dist);

        double[] pointers = new double[poolNumber];
        for (int i = 0; i < poolNumber; i++) {
            pointers[i] = start + i * dist;
        }

        // seleksi individu berdasarkan pointer (stochasticnya)
        int index = 0;
        double total = population.get(0).getFitness();

        for (double pointer : pointers) {
            while (total < pointer) {
                index++;
                total += population.get(index).getFitness();
            }
            matingPool.add(population.get(index));
        }

        return matingPool;
    }
}
