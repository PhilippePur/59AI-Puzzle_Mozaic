package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import mosaic.puzzle.Individual;

/**
 * Implementasi linear rank selection
 * <p>
 * Linear rank selection dilakukan dengan membuat ranking dari setiap individu
 * di populasi berdasarkan nilai fitness nya, kemudian di konversi menjadi
 * fitness baru dengan selective pressure tertentu. Selective pressure
 * mengontrol tingkat dominasi individu terbaik dalam proses seleksi. Semakin
 * besar nilai selective pressure, semakin besar peluang individu
 * dengan ranking tinggi untuk terpilih.
 * </p>
 * <p>
 * Setelah fitness ranking baru telah dihhitung, maka akan dilakukan selection
 * dengan Stochastic Selection
 * </p>
 * 
 * Selective pressure berada pada rentang [1.0 - 2.0], dimana (secara umum):
 * <ul>
 * <li>1.0 : seleksi bersifat acak</li>
 * <li>1.5 : tekanan seleksi seimbang</li>
 * <li>2.0 : tekanan seleksi maksimum (individu dengan fitness terbesar akan
 * diberikan kesempatan terpilih lebih besar</li>
 * </ul>
 * 
 * <p>
 * Kelebihan:
 * <ul>
 * <li>Mengatasi masalah dominasi fitness tinggi, karna fitness berdasarkan rank
 * <li>Bisa mengatur selection pressure
 * </ul>
 * 
 * Kekurangan:
 * <ul>
 * <li>Perlu Sorting
 * </ul>
 * </p>
 * 
 * @author Greg
 */
public class LinearRankSelection implements SelectionStrategy {

    private final double selectivePressure;
    private final int poolNumber;
    private final Random rng;

    public LinearRankSelection(double selectivePressure, int poolNumber, Random rng) {
        this.selectivePressure = selectivePressure;
        this.poolNumber = poolNumber;
        this.rng = rng;
    }

    @Override
    public List<Individual> select(List<Individual> population) {
        List<Individual> sorted = new ArrayList<>(population);
        sorted.sort((a, b) -> Double.compare(a.getFitness(), b.getFitness()));

        List<Individual> matingPool = new ArrayList<>();
        int size = sorted.size();

        double[] rankFitnesses = new double[size];
        double totalRankFitness = 0;

        for (int i = 0; i < size; i++) {
            double pos = i + 1;
            double rankVal = (2 - selectivePressure) / size +
                    (2 * (selectivePressure - 1) * (pos - 1)) / (size * (size - 1));
            rankFitnesses[i] = rankVal;
            totalRankFitness += rankVal;
        }

        double dist = totalRankFitness / this.poolNumber;
        double start = rng.nextDouble() * dist;

        int index = 0;
        double currentSum = rankFitnesses[0];

        for (int i = 0; i < poolNumber; i++) {
            double pointer = start + i * dist;
            while (currentSum < pointer && index < size - 1) {
                index++;
                currentSum += rankFitnesses[index];
            }
            matingPool.add(sorted.get(index).copy());
        }

        return matingPool;
    }
}