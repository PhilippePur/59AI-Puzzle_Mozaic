package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final Random rng;

    public StochasticSelection(int poolNumber, Random rng) {
        this.poolNumber = poolNumber;
        this.rng = rng;
    }

    @Override
    public List<Individual> select(List<Individual> population) {
        List<Individual> matingPool = new ArrayList<>();
        int size = population.size();
        
        double totalFitness = 0;
        for (Individual ind : population) {
            totalFitness += ind.getFitness();
        }

        double dist = totalFitness / this.poolNumber;
        double start = rng.nextDouble() * dist; // Start point acak

        double[] pointers = new double[this.poolNumber];
        for (int i = 0; i < this.poolNumber; i++) {
            pointers[i] = start + i * dist;
        }

        int index = 0;
        double currentSum = population.get(0).getFitness();

        for (double pointer : pointers) {
            while (currentSum < pointer && index < size - 1) {
                index++;
                currentSum += population.get(index).getFitness();
            }
            matingPool.add(population.get(index).copy());
        }

        return matingPool;
    }
}