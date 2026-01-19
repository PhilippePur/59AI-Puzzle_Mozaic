package mosaic.genetic.crossover;

import java.util.Random;

import mosaic.puzzle.Individual;

/**
 * Implementasi konkrit dari CrossoverStrategy menggunakan Two Point Crossover
 * 2D
 * <p>
 * Pada Strategi ini, dipilih sebuah baris/kolom secara random, lalu dipilih dua
 * titik potong secara acak (cutStart dan cutEnd) secara random. Block diantara
 * 2 titik potong itu akan diwarisi ke child hasil crossover
 * </p>
 * * @author Greg
 */
public class TwoPointCrossover implements CrossoverStrategy {
    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        int rows = parent1.getRows(); // panjang rows parent1
        int cols = parent1.getCols(); // panjang cols parent1

        Individual child = parent1.copy(); // ambil base untuk child

        boolean slice = rng.nextBoolean();

        // kalo true, maka potong row, kalo false, potong cols
        if (slice) {
            // pilih titik potong pertama di setengah bagian awal
            int cutStart = rng.nextInt(rows / 2);
            // pilih titik potong kedua dari sisa bagian setelah pemotongan pertama 
            int cutEnd = rng.nextInt(rows - 1 - (cutStart + 1)) + (cutStart + 1);
            
            // tukar dengan parent 2 jika tidak fixed
            for (int start = cutStart; start <= cutEnd; start++) {
                for (int c = 0; c < cols; c++) {
                    if (!child.isFixed(start, c)) {
                        child.setCell(start, c, parent2.getCell(start, c));
                    }
                }
            }
        } else {
            // pilih titik potong pertama di setengah bagian awal
            int cutStart = rng.nextInt(cols / 2);
            // pilih titik potong kedua dari sisa bagian setelah pemotongan pertama 
            int cutEnd = rng.nextInt(cols - 1 - (cutStart + 1)) + (cutStart + 1);
            
            // tukar dengan parent 2 jika tidak fixed
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