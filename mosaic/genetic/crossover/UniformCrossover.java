package mosaic.genetic.crossover;

import java.util.Random;
import mosaic.puzzle.Individual;

/**
 * Implementasi konkrit dari CrossoverStrategy menggunakan crossover uniform
 * Pada strategi ini, setiap sel pada papan anak dipilih secara acak dari salah
 * satu orang tua
 * dengan probabilitas 50-50
 * 
 * @author Andrew
 */
public class UniformCrossover implements CrossoverStrategy {

    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        // Membuat copy dari parent1 sebagai anak (yang nantinya beberapa sel akan
        // diganti dari parent2)
        Individual child = parent1.copy();
        int rows = child.getRows();
        int cols = child.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Dengan peluang 50%, ganti value sel dengan value dari sel dengan koordinat
                // yang sama pada parent2 jika tidak fixed
                if (!child.isFixed(r, c)) {
                    if (rng.nextBoolean()) {
                        boolean valueP2 = parent2.getCell(r, c);
                        child.setCell(r, c, valueP2);
                    }
                    // Jika tidak mendapat peluang 50%, maka tetap menggunakan value parent1
                }
            }
        }
        return child;
    }

}
