package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

/**
 * Implementasi roulette wheel selection
 * <p>
 * Roulette Wheel Selection dilakukan dengan memberikan semua individu dalam
 * populasi kesempatan sebesar p(i) sesuai proporsi fitness untuk "dipilih"
 * layaknya roulette wheel, semakin besar fitness dari sebuah individu, maka
 * "potongan wheel" dari individu tersebut juga makin besar
 * </p>
 * <p>
 * Kelebihan:
 * <ul>
 * <li>Sesuai teori evolusi (yang kuat yang berkembang biak)
 * <li>Sederhana dan intuitif, serta relatif cepat
 * </ul>
 * 
 * Kekurangan:
 * <ul>
 * <li>Dominasi individu dengan fitness tinggi
 * <li>
 * </ul>
 * </p>
 * 
 * @author Greg
 */

public class RouletteSelection implements SelectionStrategy {

    private final int poolNumber;
    private final Random rng;

    public RouletteSelection(int poolNumber, Random rng) {
        this.poolNumber = poolNumber;
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
        double totalFitness = 0;
        for (Individual ind : population) {
            totalFitness += ind.getFitness();
        }

        double alpha = rng.nextDouble() * totalFitness;
        
        double currentSum = 0;
        for (Individual ind : population) {
            currentSum += ind.getFitness();
            if (currentSum >= alpha) {
                return ind.copy();
            }
        }
        
        return population.get(population.size() - 1).copy();
    }
}