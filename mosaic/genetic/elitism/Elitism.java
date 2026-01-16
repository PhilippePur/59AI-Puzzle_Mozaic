package mosaic.genetic.elitism;
import java.util.*;

import mosaic.puzzle.Individual;

public class Elitism {
    public static List<Individual> selectElite(List<Individual> population, int eliteCount) {
        Collections.sort(population, (a, b) ->
                Double.compare(b.getFitness(), a.getFitness()));

        List<Individual> elite = new ArrayList<>();
        for (int i = 0; i < Math.min(eliteCount, population.size()); i++) {
            elite.add(population.get(i).copy());
        }
        return elite;
    }
}