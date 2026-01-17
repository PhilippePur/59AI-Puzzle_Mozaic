package mosaic.genetic.selection;

import java.util.List;

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