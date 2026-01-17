package mosaic.genetic.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private final int selectivePressure;
    private final int poolNumber;

    public LinearRankSelection(int selectivePressure, int poolNumber) {
        this.selectivePressure = selectivePressure;
        this.poolNumber = poolNumber;
    }

    public List<Individual> select(List<Individual> population) {

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

            fitnessRank = 2 - this.selectivePressure + 2 * (this.selectivePressure - 1) * (pos - 1) / (size - 1);
            sorted.get(i).setFitness(fitnessRank); // overwrite fitness
        }

        StochasticSelection selector = new StochasticSelection(this.poolNumber);

        return selector.select(sorted);
    }
}