package mosaic.puzzle;

import java.util.List;
import java.util.Random;

/**
 * Kelas yang merepresentasikan satu individu (solusi kandidat)
 * Menggunakan representasi matriks 2D sebagai encoding
 * Struktur gen berbentuk matriks 2D dengan ukuran yang sama dengan papan mosaic
 * Setiap gen secara langsung merepresentasikan koordinat (baris, kolom) dari papan mosaic
 * @author 
 */
public class Individual {
    /**
     * Matriks yang menyimpan warna sel (true = hitam, false = putih)
     */
    private boolean[][] grid;

    /**
     * Matriks yang menandai sel mana yang tidak boleh diubah (hasil deduksi aturan)
     */
    private boolean[][] isFixed;

    /**
     * Dimensi papan
     */
    private int rows;
    private int cols;

    /**
     * Referensi objek puzzle
     */
    private Puzzle puzzle;

    /**
     * Nilai fitness dari individu
     */
    private double fitness;

    /**
     * Flag penanda apakah nilai fitness perlu dihitung ulang setelah gen diubah
     * true = perlu dihitung ulang, false = tidak perlu dihitung ulang
     */
    private boolean fitnessDirty; 


    /**
     * Konstruktor utama untuk inisialisasi individu baru dengan random
     * @param puzzle objek context masalah mosaic
     * @param rng generator angka acak global 
     */
    public Individual(Puzzle puzzle, Random rng) {
        this.puzzle = puzzle;
        this.rows = puzzle.getRows();
        this.cols = puzzle.getCols();
        this.grid = new boolean[rows][cols];

        // Mengambil copy matriks sel fixed dari puzzle yg sudah ditentukan dari deduksi aturan
        this.isFixed = puzzle.getFixedCells();

        // Mengisi nilai fitness awal dan fitnessDirty sebagai perlu dihitung
        this.fitness = -1.0;
        this.fitnessDirty = true;

        // Membuat individu acak dengan tetap memperhatikan sel yang sudah fix
        initializeRandomGrid(rng);
    }

    /**
     * Konstruktor khusus untuk copy
     * Digunakan agar individu baru tidak melakukan inisialisasi rnadom grid ulang
     * @param puzzle referensi objek puzzle
     */
    private Individual(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.rows = puzzle.getRows();
        this.cols = puzzle.getCols();
    }

    /**
     * Inisialisasi awal individu dengan tidak mengubah sel yang sudah fix
     * @param rng objek Random global
     */
    private void initializeRandomGrid(Random rng) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!isFixed[r][c]) {
                    grid[r][c] = rng.nextBoolean();
                }
            }
        }
    }

    /**
     * Getter untuk variabel rows
     * @return jumlah baris papan
     */
    public int getRows() {
        return rows;
    }

    /**
     * Getter untuk variabel cols
     * @return jumlah kolom papan
     */
    public int getCols() {
        return cols;
    }

    /**
     * Mendapatkan warna pada sel tertentu
     * @param r indeks baris sel
     * @param c indeks kolom sel
     * @return true jika hitam, false jika putih
     */
    public boolean getCell(int r, int c) {
        return grid[r][c];
    }

    /**
     * Cek apakah sel tertentu sudah fix
     * @param r indeks baris sel
     * @param c indeks kolom sel
     * @return true jika fix, false jika tidak fix
     */
    public boolean isFixed(int r, int c) {
        return isFixed[r][c];
    }

    /**
     * Getter untuk nilai fitness
     * Jika status gen berubah dan nilai fitness belum paling update (dirty), metode ini akan menghitung ulang sebelum mengembalikan nilai
     * @return nilai fitness
     */
    public double getFitness() {
        // Jika status gen berubah dan nilai fitness belum paling update (dirty), hitung ulang
        if (fitnessDirty) {
            calculateFitness();
        }
        return fitness;
    }

    /**
     * Getter untuk variabel grid (untuk keperluan eksternal)
     * @return matriks boolean 2d yang identik dengan gen individu ini 
     */
    public boolean[][] getGrid() {
        // Return defensive copy untuk safety
        boolean[][] copy = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    /**
     * Mengatur nilai pada satu sel jika sel tidak fix
     * @param r indeks baris sel
     * @param c indeks kolom sel
     * @param value nilai baru
     */
    public void setCell(int r, int c, boolean value) {
        // Jika sel tidak fix, set nilai baru dan tandai fitness menjadi dirty
        if (!isFixed[r][c]) {
            grid[r][c] = value;
            markDirty();
        }
    }

    /**
     * Membalik nilai pada sel jika sel tidak terkunci (true jadi false, false jadi true)
     * @param r indeks baris sel
     * @param c indeks kolom sel
     */
    public void flipCell(int r, int c) {
        // Jika sel tidak fix, balik nilai dan tandai fitness menjadi dirty
        if (!isFixed[r][c]) {
            grid[r][c] = !grid[r][c];
            markDirty();
        }
    }

    /**
     * Menandai fitness menjadi dirty
     * Fitness akan dihitung ulang ketika diperlukan (pada getFitness())
     */
    public void markDirty() {
        this.fitnessDirty = true;
    }

    /**
     * Metode untuk menghitung nilai fitness dari individu
     * Menggunakan pendekatan Linear Error Sum, di mana setiap perbedaan antara
     * nilai petunjuk dan jumlah tetangga hitam dihitung menggunakan nilai absolut
     * Hasil akhir dinormalisasi ke dalam rentang 0.0 - 1.0
     * Nilai 1.0 menandakan solusi sempurna, dan semakin dekat ke 0.0 menandakan
     * solusi semakin buruk
     */
    public void calculateFitness() {
        // Inisialisasi total error
        double totalError = 0.0;

        // Mengambil daftar seluruh clue yang ada pada puzzle
        List<Clue> clues = puzzle.getClues();

        // Iterasi setiap clue satu per satu
        for (int i = 0; i < clues.size(); i++) {
            Clue currentClue = clues.get(i);

            // Mendapatkan koordinat dan value (angka) dari clue saat ini
            int row = currentClue.getRow();
            int col = currentClue.getCol();
            int clueValue = currentClue.getValue();

            // Menghitung berapa banyak sel hitam yang ada di area 3x3 sekeliling clue
            int blackCount = countBlackIn3x3Area(row, col);

            // Menambahkan selisih absolut ke total error
            totalError += Math.abs(blackCount - clueValue);
        }

        // Melakukan normalisasi untuk menghitung fitness,
        // Penambahan 1.0 pada penyebut dilakukan untuk menghindari pembagian dengan 0 saat totalError = 0
        this.fitness = 1.0 / (1.0 + totalError);

        // Menandai bahwa fitness sudah paling update (sehingga tidak perlu dihitung ulang lagi)
        this.fitnessDirty = false;
    }

    /**
     * Metode untuk menghitung sel hitam dalam area 3x3 yang pusatnya pada koordinat (centerR, centerC)
     * @param centerR koordinat baris pusat area
     * @param centerC koordinat kolom pusat area
     * @return jumlah sel hitam dalam area 3x3
     */
    private int countBlackIn3x3Area(int centerR, int centerC) {
        int count = 0;

        // Iterasi area 3x3 sekitar clue
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;

                // Memastikan koordinat tetap dalam batas papan
                if (r >= 0 && r < rows && c >= 0 && c < cols && grid[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Menciptakan copy an dari individu ini
     * Penting untuk menjamin objek individu orang tua dan anak tidak terkait selama GA berjalan
     * @return objek Individual baru yang identik namun berbeda referensi memori
     */
    public Individual copy() {
        // Menggunakan konstruktor khusus copy
        Individual copy = new Individual(this.puzzle);

        // Copy matriks grid
        copy.grid = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.grid[i], 0, copy.grid[i], 0, cols);
        }

        // Copy matriks isFixed
        copy.isFixed = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.isFixed[i], 0, copy.isFixed[i], 0, cols);
        }

        // Copy fitness dan flag fitnessDirty
        copy.fitness = this.fitness;
        copy.fitnessDirty = this.fitnessDirty;

        return copy;
    }
}