package mosaic.genetic.selection;

import java.util.List;

import mosaic.puzzle.Individual;

/**
 * Interface untuk parent selection strategy
 * Setiap implementasi konkrit akan mengembalikan mating pool yang berisikan
 * jumlah parent untuk "dikawinkan"
 * 
 * @author Greg
 */
public interface SelectionStrategy {
    List<Individual> select(List<Individual> population);
}
