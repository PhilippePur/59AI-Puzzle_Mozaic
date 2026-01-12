package mosaic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
kelas ini tuh fungsinya untuk membuat kombinasi pola-pola 3x3 mana saja yang valid buat jika ada clue N  
kelas ini pakai pattern singleton karena hanya perlu 1 buah saja dan tidak perlu diinstansiasi ulang 

*/

public class PatternCache {
    private static PatternCache instance;
    private final Map<Integer, List<boolean[][]>> cache;

    private PatternCache() {
        cache = new HashMap<>();
        precomputePatterns();
    }

    public static PatternCache getInstance() {
        if (instance == null) {
            instance = new PatternCache();
        }
        return instance;
    }

    // generate pola untuk semua kemungkinan angka clue (0 - 9)
    // diprecompute biar kalau misal dipanggil-panggil lagi sama method lain jadinya
    // O(1), tinggal liat hashmap
    private void precomputePatterns() {
        for (int k = 0; k <= 9; k++) {
            List<boolean[][]> patterns = new ArrayList<>();
            generatePermutations(new boolean[9], 0, k, patterns);
            cache.put(k, patterns);
        }
    }

    private void generatePermutations(boolean[] current, int index, int targetCount, List<boolean[][]> result) {
        // base casenya kalau 3 x 3 nya udah keisi semua
        if (index == 9) {
            if (countTrue(current) == targetCount) {
                result.add(reshape3x3(current));
            }
            return;
        }

        int currentCount = countTrue(current, index);
        int remainingSlots = 9 - index;

        // kalau clue yang digenerate sudah ga mungkin untuk bisa memenuhi target count
        if (currentCount + remainingSlots < targetCount)
            return;
        // kalau yang udah diitung sebagian ternyata melebihi target
        if (currentCount > targetCount)
            return;

        current[index] = false;
        generatePermutations(current, index + 1, targetCount, result);

        current[index] = true;
        generatePermutations(current, index + 1, targetCount, result);
    }

    // untuk ngitung ada brp true di arr dengan limit (ngitungnya sebagian aja)
    private int countTrue(boolean[] arr, int limit) {
        int c = 0;
        for (int i = 0; i < limit; i++)
            if (arr[i])
                c++;
        return c;
    }

    // untuk ngitung ada brp true di arr
    private int countTrue(boolean[] arr) {
        int c = 0;
        for (boolean b : arr)
            if (b)
                c++;
        return c;
    }

    // convert dari array 1 dimensi ke 2 dimensi
    // tujuannya sih buat lebih gampang pas bikin patternya di 1dimensi baru convert
    // ke 2dimensi
    private boolean[][] reshape3x3(boolean[] flat) {
        boolean[][] grid = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(flat, i * 3, grid[i], 0, 3);
        }
        return grid;
    }

    // ini buat method lain manggil ini buat dapetin pattern yang valid
    public List<boolean[][]> getValidPatterns(int value) {
        if (value < 0 || value > 9)
            return new ArrayList<>();
        return cache.get(value);
    }
}