package mosaic.genetic.crossover;

import java.util.Random;
import mosaic.puzzle.Individual;

/**
 * Implementasi dari CrossoverStrategy 
 * Strategi ini merupakan improvement dari Single Block, di mana block yang dipilih bisa beberapa (lebih dari satu)
 * Ukuran block juga bisa bervariasi tergantung konfigurasinya (misal mau 3x3 semua, 4x4 semua, dll)
 * Child akan mendapatkan gen block block tersebut dari parent2, dan sisanya didapat dari parent1
 * @author Andrew
 
 */
public class MultiBlockCrossover implements CrossoverStrategy {
    
    /**
     * Jumlah block yang akan diambil dari parent2
     */
    private final int numberOfBlocks;
    
    /**
     * Ukuran block yang akan diambil (untuk setiap numberOfBlocks, ukuran nya akan sama semua)
     */
    private final int blockSize;

    
    /**
     * Constructor yang menentukan parameter crossover multi block
     * @param numberOfBlocks jumlah block yang ingin ditukar
     * @param blockSize ukuran block yang ingin ditukar
     */
    public MultiBlockCrossover(int numberOfBlocks, int blockSize) {
        this.numberOfBlocks = numberOfBlocks;
        this.blockSize = blockSize;
    }


    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        Individual child = parent1.copy();
        int rows = child.getRows();
        int cols = child.getCols();
        // Looping sebanyak jumlah block yang diinginkan
        for (int i = 0; i < numberOfBlocks; i++) {
            // Untuk setiap block, dipilih areanya sesuai dengan ukurannya

            //  Pilih titik pojok kiri atas block secara random
            int startRow = rng.nextInt(rows);
            int startCol = rng.nextInt(cols);

            // Tukar gen untuk area sebesar ukuran block yang dimulai dari titik pojok kiri atas yang sudah dipilih
            for (int r = startRow; r < startRow + blockSize; r++) {
                for (int c = startCol; c < startCol + blockSize; c++) {
                    // Cek apakah masih dalam batas papan dan apakah gen fix, jika aman maka lanjut tukar
                    // Batas papan tidak perlu di cek untuk < 0, karena pada langkah 1, dipilih row dan col secara random yang tidak akan < 0, jadi sudah pasti hasilnya akan > 0
                    if (r < rows && c < cols && !child.isFixed(r, c)) {
                        child.setCell(r, c, parent2.getCell(r, c));
                    }
                }
            }
        }

        return child;
    }

}
