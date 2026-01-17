package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

/**
 * Implementasi truncation selection
 * <p>
 * Truncation Selection dilakukan dengan mengambil "portion" terbaik dari sebuah
 * populasi, kemudian dari porsi tersebut akan dipilih sebuah inidividu random
 * </p>
 * <p>
 * Kelebihan:
 * <ul>
 * <li>Tingkat konvergensi tinggi
 * <li>Tingkat eksploitasi tinggi (karena mengambil sebagian individu terbaik)
 * </ul>
 * 
 * Kekurangan:
 * <ul>
 * <li>Premature convergence
 * <li>Mudah terjebak local optima
 * </ul>
 * </p>
 * 
 * @author Greg
 */

public class TruncationSelection {
  public static Individual select(List<Individual> population, int portion) {
    List<Individual> sorted = new ArrayList<>(population);

    Collections.sort(sorted, new Comparator<Individual>() {
      @Override
      public int compare(Individual a, Individual b) {
        return Double.compare(b.getFitness(), a.getFitness());
      }
    });

    int truncate = Math.max(1, (int) Math.floor(population.size() * (portion / 100.0)));
    Individual parent = sorted.get(GlobalRandom.rdm.nextInt(truncate));

    return parent.copy();
  }
}