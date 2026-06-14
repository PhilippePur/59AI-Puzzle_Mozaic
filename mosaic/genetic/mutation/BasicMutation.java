package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import java.util.Random;

/**
 * Strategi mutasi dasar yang menerapkan prinsip Bit-Flip mutation.
 * Setiap sel pada individu memiliki peluang independen sebesar mutationRate
 * untuk dibalik nilainya (hitam menjadi putih atau sebaliknya), selama sel tersebut tidak terkunci.
 * @author Michael G
 */
public class BasicMutation implements MutationStrategy {

    private final double mutationRate;
    private final Random random;

    /**
     * Menginisialisasi strategi mutasi dasar.
     * @param mutationRate kemungkinan terjadinya mutasi per sel.
     * @param random generator angka acak.
     */
    public BasicMutation(double mutationRate, Random random) {
        this.mutationRate = mutationRate;
        this.random = random;
    }

    /**
     * Iterasi ke seluruh sel pada grid individu dan membalik nilai sel
     * jika angka acak yang dihasilkan kurang dari mutation rate.
     * 
     */
    @Override
    public void mutate(Individual individual, Puzzle puzzle) {
        if (individual == null || puzzle == null) {
            throw new IllegalArgumentException("Individual dan Puzzle tidak boleh null");
        }

        int rows = individual.getRows();
        int cols = individual.getCols();

        // flip cell dengan kemungkinan kecil 
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!individual.isFixed(r, c) && random.nextDouble() < mutationRate) {
                    individual.flipCell(r, c);
                }
            }
        }
    }

    @Override
    public String getStrategyName() {
        return "BasicMutation";
    }
}