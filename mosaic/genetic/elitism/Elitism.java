package mosaic.genetic.elitism;
import java.util.*;

import mosaic.puzzle.Individual;

/**
 * Kelas yang mengimplementasikan elitism
 * <p>
 * Memilih individu terbaik dari populasi sebanyak eliteCount dan di return ke dalam bentuk array.
 * Hasil array ini akan dibaca oleh {@link GeneticAlgorithm} dan akan dimasukkan ke dalam populasi baru.
 * </p>
 */
public class Elitism {
    /**
     * Mengambil individu terbaik dari populasi sebanyak eliteCount
     * @param population Populasi yang akan diambil individu terbaiknya
     * @param eliteCount Jumlah individu terbaik yang akan diambil
     * @return List individu terbaik
     */
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