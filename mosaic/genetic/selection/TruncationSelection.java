package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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

public class TruncationSelection implements SelectionStrategy {

    private final int portion; 
    private final int poolNumber;
    private final Random rng;

    public TruncationSelection(int portion, int poolNumber, Random rng) {
        this.portion = portion;
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
        List<Individual> sorted = new ArrayList<>(population);
        sorted.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));

        int truncateCount = Math.max(1, (int) (sorted.size() * (this.portion / 100.0)));
        
        int randomIdx = rng.nextInt(truncateCount);
        return sorted.get(randomIdx).copy();
    }
}