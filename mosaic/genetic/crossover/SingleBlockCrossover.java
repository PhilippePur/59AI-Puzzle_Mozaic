package mosaic.genetic.crossover;

import java.util.Random;
import mosaic.puzzle.Individual;

/**
 * Implementasi konkrit dari CrossoverStrategy menggunakan crossover single block
 * Pada strategi ini, dipilih satu area persegi panjang (block) secara acak di dalam papan
 * Child akan mendapatkan gen area tersebut dari parent2, sedangkan sisanya dari parent1. Namun tetap memperhatikan sel yang sudah fix
 * @author Andrew
 */
public class SingleBlockCrossover implements CrossoverStrategy {

    @Override
    public Individual crossover(Individual parent1, Individual parent2, Random rng) {
        // Membuat copy dari parent1 sebagai anak (yang nantinya ada block yang akan diganti dari parent2)
        Individual child = parent1.copy();
        
        // Pilih 4 titik ujung persegi panjang secara acak. 
        // Tujuannya adalah mencari 4 titik yaitu (startRow, startCol), (startRow, endCol), (endRow, startCol), (endRow, endCol)
        // yang akan digunakan untuk memasukkan block dari parent2 ke dalam child

        // 1. Memilih 2 index baris, 1 sebagai baris start dan 1 sebagai baris end
        int r1 = rng.nextInt(parent1.getRows());
        int r2 = rng.nextInt(parent1.getRows());

        // Baris yang lebih kecil menjadi start dan yang lebih besar menjadi end
        int startRow = Math.min(r1, r2);
        int endRow = Math.max(r1, r2);

        // 2. Memilih 2 index kolom, 1 sebagai kolom start dan 1 sebagai kolom end
        int c1 = rng.nextInt(parent1.getCols());
        int c2 = rng.nextInt(parent1.getCols());

        // Kolom yang lebih kecil menjadi start dan yang lebih besar menjadi end
        int startCol = Math.min(c1, c2);
        int endCol = Math.max(c1, c2);

        // Mengganti sel sel di block child dengan sel sel dari block parent2
        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                // Jika sel sudah fix, maka tidak perlu diganti 
                if (!child.isFixed(r, c)) {
                    child.setCell(r, c, parent2.getCell(r, c));
                }
            }
        }

        return child;
    }
    

}
