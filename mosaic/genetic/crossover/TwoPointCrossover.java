package mosaic.genetic.crossover;

import java.util.Random;

import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

/**
 * Implementasi konkrit dari CrossoverStrategy menggunakan Two Point Crossover
 * 2D
 * <p>
 * Pada Strategi ini, dipilih sebuah baris/kolom secara random, lalu dipilih dua
 * titik potong secara acak (cutStart dan cutEnd) secara random. Block diantara
 * 2 titik potong itu akan diwarisi ke child hasil crossover
 * </p>
 * 
 * @author Greg
 */
public class TwoPointCrossover implements CrossoverStrategy {
    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        int rows = parent1.getRows(); // panjang rows parent1
        int cols = parent1.getCols(); // panjang cols parent1

        Individual child = parent1.copy(); // ambil base untuk child

        boolean slice = GlobalRandom.rdm.nextBoolean();

        // kalo true, maka potong row, kalo false, potong cols
        if (slice) {
            int cutStart = GlobalRandom.rdm.nextInt(rows / 2);
            int cutEnd = GlobalRandom.rdm.nextInt(cutStart + 1, rows - 1);

            for (int start = cutStart; start <= cutEnd; start++) {
                for (int c = 0; c < cols; c++) {
                    if (!child.isFixed(start, c)) {
                        child.setCell(start, c, parent2.getCell(start, c));
                    }
                }
            }
        } else {
            int cutStart = GlobalRandom.rdm.nextInt(cols / 2);
            int cutEnd = GlobalRandom.rdm.nextInt(cutStart + 1, cols - 1);

            for (int c = cutStart; c <= cutEnd; c++) {
                for (int r = 0; r < rows; r++) {
                    if (!child.isFixed(r, c)) {
                        child.setCell(r, c, parent2.getCell(r, c));
                    }
                }
            }
        }

        return child;
    }
}
