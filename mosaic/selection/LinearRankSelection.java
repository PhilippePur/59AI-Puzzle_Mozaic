import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

public class LinearRankSelection {
    public static List<Individual> select(List<Individual> population, double selectivePressure, int poolNumber) {

        // Linear Rank dari populasi, lalu dipilih hasil population baru menggunakan SUS
        List<Individual> sorted = new ArrayList<>(population);

        Collections.sort(sorted, new Comparator<Individual>() {
            @Override
            public int compare(Individual a, Individual b) {
                return Double.compare(a.getFitness(), b.getFitness());
            }
        });

        int size = population.size();

        double fitnessRank = 0;
        for (int i = 0; i < size; i++) {
            int pos = i;

            fitnessRank = 2 - selectivePressure + 2 * (selectivePressure - 1) * (pos - 1) / (size - 1);
            sorted.get(i).setFitness(fitnessRank); // overwrite fitness
        }

        return StochasticSelection.select(sorted, poolNumber);
    }
}
