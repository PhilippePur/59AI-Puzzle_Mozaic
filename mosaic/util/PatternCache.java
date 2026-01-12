package mosaic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code PatternCache} adalah kelas utilitas yang bertugas untuk
 * menghasilkan dan menyimpan semua kombinasi pola 3x3 yang valid
 * berdasarkan nilai clue (0–9).
 * <p>
 * Setiap pola direpresentasikan sebagai array boolean 2 dimensi 3x3, di mana
 * {@code true} berarti sel yang ditandai warna hitam dan {@code false} berarti kosong atau sel berwarna putih
 *  <p>
 * Kelas ini menerapkan Singleton Pattern karena hanya dibutuhkan
 * satu instance saja selama program berjalan. Semua pola akan
 * diprekomputasi di awal agar akses berikutnya bersifat O(1).
 */
public class PatternCache {

    /**
     * Instance tunggal dari {@code PatternCache}.
     */
    private static PatternCache instance;

    /**
     * cache yang memetakan nilai clue ke daftar pola 3x3 yang valid.
     * Key berisi nilai clue (0–9)  
     * Value berisi list pola boolean 3x3 yang jumlah {@code true}-nya sesuai clue
     */
    private final Map<Integer, List<boolean[][]>> cache;

    /**
     * Konstruktor private untuk mencegah instansiasi langsung dan langsung melakukan precompute pola.
     */
    private PatternCache() {
        cache = new HashMap<>();
        precomputePatterns();
    }

    /**
     * Mengembalikan instance tunggal dari {@code PatternCache}.
     * Jika instance belum ada, maka akan dibuat terlebih dahulu.
     *
     * @return instance {@code PatternCache}
     */
    public static PatternCache getInstance() {
        if (instance == null) {
            instance = new PatternCache();
        }
        return instance;
    }

    /**
     * Melakukan precompute seluruh kombinasi pola 3x3
     * untuk semua kemungkinan nilai clue (0 sampai 9).
     * <p>
     * Hasilnya disimpan ke dalam cache agar pemanggilan
     * selanjutnya tidak perlu melakukan perhitungan ulang.
     */
    private void precomputePatterns() {
        for (int k = 0; k <= 9; k++) {
            List<boolean[][]> patterns = new ArrayList<>();
            generatePermutations(new boolean[9], 0, k, patterns);
            cache.put(k, patterns);
        }
    }

    /**
     * Menghasilkan seluruh kombinasi boolean 1D sepanjang 9 elemen
     * yang memiliki jumlah {@code true} sesuai dengan targetCount
     * <p>
     * Metode ini menggunakan pendekatan rekursif dengan pruning
     * untuk menghentikan cabang yang sudah tidak mungkin memenuhi target.
     *
     * @param current      array boolean sementara (1D)
     * @param index        posisi yang sedang diproses
     * @param targetCount  jumlah {@code true} yang diinginkan
     * @param result       list hasil pola 3x3 yang valid
     */
    private void generatePermutations(boolean[] current, int index, int targetCount, List<boolean[][]> result) {
        // base casenya adalah ketika seluruh sel sudah diproses
        if (index == 9) {
            if (countTrue(current) == targetCount) {
                result.add(reshape3x3(current));
            }
            return;
        }

        int currentCount = countTrue(current, index);
        int remainingSlots = 9 - index;

        // ketika tidak mungkin mencapai target maka akan dilakukan pruning 
        if (currentCount + remainingSlots < targetCount)
            return;

        // ketika jumlah true sudah melebihi target maka akan dilakukan pruning
        if (currentCount > targetCount)
            return;

        current[index] = false;
        generatePermutations(current, index + 1, targetCount, result);

        current[index] = true;
        generatePermutations(current, index + 1, targetCount, result);
    }

    /**
     * Menghitung jumlah nilai {@code true} pada array boolean
     * hingga batas indeks tertentu.
     *
     * @param arr   array boolean
     * @param limit batas indeks (eksklusif)
     * @return jumlah elemen {@code true}
     */
    private int countTrue(boolean[] arr, int limit) {
        int c = 0;
        for (int i = 0; i < limit; i++)
            if (arr[i])
                c++;
        return c;
    }

    /**
     * Menghitung jumlah nilai {@code true} pada seluruh array boolean.
     *
     * @param arr array boolean
     * @return jumlah elemen {@code true}
     */
    private int countTrue(boolean[] arr) {
        int c = 0;
        for (boolean b : arr)
            if (b)
                c++;
        return c;
    }

    /**
     * Mengonversi array boolean 1 dimensi (panjang 9)
     * menjadi array boolean 2 dimensi berukuran 3x3.
     *
     * @param flat array boolean 1D
     * @return array boolean 3x3
     */
    private boolean[][] reshape3x3(boolean[] flat) {
        boolean[][] grid = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(flat, i * 3, grid[i], 0, 3);
        }
        return grid;
    }

    /**
     * Mengembalikan daftar pola 3x3 yang valid
     * berdasarkan nilai clue tertentu.
     *
     * @param value nilai clue (0–9)
     * @return list pola boolean 3x3 yang valid,
     *         atau list kosong jika nilai tidak valid
     */
    public List<boolean[][]> getValidPatterns(int value) {
        if (value < 0 || value > 9)
            return new ArrayList<>();
        return cache.get(value);
    }

    /**
     * Mengambil daftar pola yang valid untuk nilai clue tertentu, namun difilter
     * berdasarkan kondisi sel yang sudah diketaui yang ditandai oleh isFixed.
     * <p>
     * Metode ini berfungsi untuk mutasi yang akan mengubah area 3x3 tetapi harus tetap menghormati sel yang sudah
     * ditentukan oleh heuristik yang ditandai oleh isFixed.
     * </p>
     *
     * @param value      nilai clue (target jumlah hitam)
     * @param knownState array 3x3 yang berisi warna saat ini (hitam/putih)
     * @param isKnown    array 3x3 mask, {@code true} jika sel tersebut fixed ,
     * {@code false} jika sel tersebut bebas (boleh berbeda)
     * @return List pola yang sesuai dengan clue DAN cocok dengan knownState pada posisi isKnown
     */
    public List<boolean[][]> getCompatiblePatterns(int value, boolean[][] knownState, boolean[][] isKnown) {
        List<boolean[][]> allPatterns = getValidPatterns(value);
        
        if (allPatterns.isEmpty()) {
            return new ArrayList<>();
        }

        List<boolean[][]> compatible = new ArrayList<>();

        for (boolean[][] pattern : allPatterns) {
            if (isPatternCompatible(pattern, knownState, isKnown)) {
                compatible.add(pattern);
            }
        }

        return compatible;
    }

    /**
     * Memeriksa apakah sebuah pola kandidat cocok dengan state yang sudah diketahui.
     *
     * @param candidate  pola 3x3 yang sedang diperiksa
     * @param knownState pola 3x3 referensi (state saat ini)
     * @param isKnown    mask 3x3, true berarti posisi tersebut harus sama persis
     * @return {@code true} jika pola kompatibel, {@code false} jika ada konflik pada posisi known
     */
    private boolean isPatternCompatible(boolean[][] candidate, boolean[][] knownState, boolean[][] isKnown) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (isKnown[r][c]) {
                    if (candidate[r][c] != knownState[r][c]) {
                        return false; 
                    }
                }
            }
        }
        return true;
    }
}
