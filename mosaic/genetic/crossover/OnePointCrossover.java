package mosaic.genetic.crossover;

import java.util.Random;

import mosaic.puzzle.Individual;

/**
 * Implementasi konkrit dari CrossoverStrategy menggunakan One Point Crossover
 * 2D
 * <p>
 * Pada Strategi ini, dipilih sebuah baris/kolom secara random, dengan jumlah x
 * (0 < x < n(panjang/lebar puzzle)) secara random, kemudian hasil crossover
 * akan mendapat baris/kolom 1 hingga x dari parent1, dan x+1 hingga n dari
 * parent2
 * </p>
 * * @author Greg
 */
public class OnePointCrossover implements CrossoverStrategy {

    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        int rows = parent1.getRows(); // panjang rows parent1
        int cols = parent1.getCols(); // panjang cols parent1

        Individual child = parent1.copy(); // ambil base untuk child

        // Menggunakan rng lokal, bukan GlobalRandom agar thread-safe
        boolean slice = rng.nextBoolean();

        // kalo true, maka potong row, kalo false, potong cols
        if (slice) {
            int cut = rng.nextInt(rows - 1) + 1;

            for (int start = cut; start < rows; start++) {
                for (int c = 0; c < cols; c++) {
                    if (!child.isFixed(start, c)) {
                        child.setCell(start, c, parent2.getCell(start, c));
                    }
                }
            }
        } else {
            int cut = rng.nextInt(cols - 1) + 1;

            for (int c = cut; c < cols; c++) {
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