package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.List;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

/**
 * Implementasi Stochastic Universal Sampling
 * <p>
 * Stochastic Universal Sampling dilakukan dengan menaruh beberapa "pointer" di
 * sekitar roulette wheel, sehingga persebaran individu akan lebih merata dan
 * stabil. Titik pointer awal akan dipilih secara acak dan sisanya mengikuti
 * pointer + distance. Kemudian setiap pointer menunjuk individu yang akan
 * dimasukkan ke dalam mating pool. Individu dengan fitness yang lebih besar
 * memiliki wilayah yang lebih panjang pada garis fitness, sehingga memiliki
 * peluang lebih besar untuk terpilih.
 * </p>
 * 
 * <p>
 * Kelebihan:
 * <ul>
 * <li>Low variance
 * <li>Individu dominan lebih cenderung dapet jatah
 * </ul>
 * 
 * Kekurangan:
 * <ul>
 * <li>tergantung fitness yang dominan
 * </ul>
 * </p>
 * 
 * @author Greg
 */

public class StochasticSelection implements SelectionStrategy {

    private final int poolNumber;

    public StochasticSelection(int poolNumber) {
        this.poolNumber = poolNumber;
    }

    public List<Individual> select(List<Individual> population) {
        // ini bagian findPointers
        List<Individual> matingPool = new ArrayList<>();
        int size = population.size();
        double totalFitness = 0;
        for (int i = 0; i < size; i++) {
            totalFitness += population.get(i).getFitness();
        }

        double dist = totalFitness / this.poolNumber;
        double start = GlobalRandom.rdm.nextDouble(dist);

        double[] pointers = new double[this.poolNumber];
        for (int i = 0; i < this.poolNumber; i++) {
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