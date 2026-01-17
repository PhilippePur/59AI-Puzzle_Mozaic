package mosaic.puzzle;

import java.util.List;
import java.util.Random;

/**
 * {@code Individual} merepresentasikan satu individu (solusi kandidat) dalam
 * populasi Algoritma Genetik.
 * <p>
 * Kelas ini menggunakan representasi matriks 2D sebagai encoding genotipe,
 * di mana struktur gen memiliki ukuran yang sama persis dengan papan Mosaic.
 * Setiap gen secara langsung merepresentasikan koordinat (baris, kolom) dari
 * papan.
 * </p>
 * <p>
 * Individu memiliki referensi ke objek {@link Puzzle} untuk mengakses batasan
 * (constraint)
 * seperti sel yang terkunci ({@code isFixed}), sehingga memori lebih efisien
 * karena
 * tidak menduplikasi data soal di setiap individu.
 * </p>
 */
public class Individual {

    /**
     * Matriks boolean yang menyimpan warna sel (Genotipe).
     * <ul>
     * <li>{@code true} = Hitam (Filled)</li>
     * <li>{@code false} = Putih (Empty)</li>
     * </ul>
     */
    private boolean[][] grid;

    /**
     * Referensi ke objek {@link Puzzle} yang merupakan konteks masalah (soal).
     * Digunakan untuk mengecek clue dan status sel yang terkunci (fixed).
     */
    private final Puzzle puzzle;

    /**
     * Nilai kebugaran (fitness) dari individu ini.
     * Rentang nilai dinormalisasi antara 0.0 hingga 1.0.
     */
    private double fitness;

    /**
     * Flag penanda apakah nilai fitness perlu dihitung ulang (dirty).
     * <ul>
     * <li>{@code true} = Genotipe telah berubah, fitness harus dihitung ulang.</li>
     * <li>{@code false} = Nilai fitness saat ini masih valid.</li>
     * </ul>
     * Digunakan untuk optimasi agar tidak menghitung fitness berulang kali jika
     * tidak perlu.
     */
    private boolean fitnessDirty;

    /**
     * Konstruktor utama untuk menginisialisasi individu baru secara acak.
     * <p>
     * Inisialisasi akan menghormati sel yang sudah berstatus <b>Fixed</b> di dalam
     * {@code Puzzle}.
     * Sel yang fixed akan mengambil nilai pasti dari puzzle, sedangkan sel sisanya
     * akan diisi acak.
     * </p>
     * * @param puzzle referensi objek puzzle (soal)
     * 
     * @param rng generator angka acak global (untuk deterministik seed)
     */
    public Individual(Puzzle puzzle, Random rng) {
        this.puzzle = puzzle;
        this.grid = new boolean[puzzle.getRows()][puzzle.getCols()];

        // Menandai fitness awal perlu dihitung
        this.fitness = -1.0;
        this.fitnessDirty = true;

        // Inisialisasi genotipe
        initializeGrid(rng);
    }

    /**
     * Konstruktor khusus untuk penyalinan (Copy Constructor).
     * <p>
     * Digunakan untuk membuat salinan mendalam (deep copy) dari individu,
     * sehingga perubahan pada anak tidak mempengaruhi orang tua.
     * </p>
     * * @param puzzle referensi objek puzzle
     */
    private Individual(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    /**
     * Melakukan inisialisasi grid.
     * Sel yang {@code isFixed} diambil dari Puzzle, sisanya diacak.
     * * @param rng objek Random global
     */
    private void initializeGrid(Random rng) {
        for (int r = 0; r < puzzle.getRows(); r++) {
            for (int c = 0; c < puzzle.getCols(); c++) {
                if (puzzle.isFixed(r, c)) {
                    // Ambil kunci jawaban pasti dari Puzzle
                    grid[r][c] = puzzle.getFixedValue(r, c);
                } else {
                    // Acak untuk sel yang belum pasti
                    grid[r][c] = rng.nextBoolean();
                }
            }
        }
    }

    /**
     * Mengembalikan jumlah baris papan permainan.
     * * @return jumlah baris
     */
    public int getRows() {
        return puzzle.getRows();
    }

    /**
     * Mengembalikan jumlah kolom papan permainan.
     * * @return jumlah kolom
     */
    public int getCols() {
        return puzzle.getCols();
    }

    /**
     * Mendapatkan status warna pada sel tertentu.
     * * @param r indeks baris sel
     * 
     * @param c indeks kolom sel
     * @return {@code true} jika Hitam, {@code false} jika Putih
     */
    public boolean getCell(int r, int c) {
        return grid[r][c];
    }

    /**
     * Memeriksa apakah sel tertentu bersifat permanen (Fixed) berdasarkan aturan
     * Puzzle.
     * Sel yang fixed tidak boleh dimutasi.
     * * @param r indeks baris sel
     * 
     * @param c indeks kolom sel
     * @return {@code true} jika sel terkunci, {@code false} jika bebas
     */
    public boolean isFixed(int r, int c) {
        return puzzle.isFixed(r, c);
    }

    /**
     * Mengembalikan nilai fitness individu.
     * <p>
     * Jika genotipe telah berubah (dirty), metode ini akan memicu perhitungan ulang
     * {@link #calculateFitness()} secara otomatis sebelum mengembalikan nilai.
     * </p>
     * * @return nilai fitness (0.0 - 1.0)
     */
    public double getFitness() {
        if (fitnessDirty) {
            calculateFitness();
        }
        return fitness;
    }

    /**
     * Mengembalikan salinan matriks grid (genotipe).
     * <p>
     * Mengembalikan <i>defensive copy</i> agar array internal tidak dapat
     * dimanipulasi
     * secara langsung dari luar kelas.
     * </p>
     * * @return matriks boolean 2D baru yang berisi salinan warna sel
     */
    public boolean[][] getGrid() {
        boolean[][] copy = new boolean[puzzle.getRows()][puzzle.getCols()];
        for (int i = 0; i < puzzle.getRows(); i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, puzzle.getCols());
        }
        return copy;
    }

    /**
     * Mendapatkan referensi ke objek Puzzle.
     * Berguna untuk komponen lain yang membutuhkan akses ke metadata soal.
     * * @return objek Puzzle
     */
    public Puzzle getPuzzle() {
        return puzzle;
    }

    /**
     * Mengubah nilai warna pada sel tertentu secara aman.
     * <p>
     * Perubahan hanya akan diterapkan jika sel tersebut <b>TIDAK</b> berstatus
     * fixed.
     * Jika terjadi perubahan, flag {@code fitnessDirty} akan diset ke true.
     * </p>
     * * @param r indeks baris sel
     * 
     * @param c     indeks kolom sel
     * @param value nilai warna baru (true/false)
     */
    public void setCell(int r, int c, boolean value) {
        // Cek ke Puzzle, jika dikunci jangan ubah
        if (puzzle.isFixed(r, c)) {
            return;
        }

        // Hanya update jika nilai berbeda (optimasi)
        if (grid[r][c] != value) {
            grid[r][c] = value;
            markDirty();
        }
    }

    /**
     * Membalik (flip) nilai warna pada sel tertentu.
     * Hitam menjadi Putih, dan sebaliknya.
     * <p>
     * Operasi ini diabaikan jika sel berstatus fixed.
     * </p>
     * * @param r indeks baris sel
     * 
     * @param c indeks kolom sel
     */
    public void flipCell(int r, int c) {
        if (puzzle.isFixed(r, c)) {
            return;
        }
        grid[r][c] = !grid[r][c];
        markDirty();
    }

    /**
     * Menandai bahwa genotipe telah berubah dan nilai fitness saat ini sudah tidak
     * valid (kadaluarsa).
     * Fitness akan dihitung ulang pada pemanggilan {@link #getFitness()}
     * berikutnya.
     */
    public void markDirty() {
        this.fitnessDirty = true;
    }

    /**
     * Menghitung nilai kebugaran (fitness) dari individu.
     * <p>
     * Menggunakan pendekatan <b>Normalized Linear Fitness</b>.
     * Rumus: {@code Fitness = 1.0 - (TotalError / MaxPossibleError)}
     * </p>
     * <p>
     * Pendekatan ini dipilih untuk mengatasi masalah <i>Vanishing Gradient</i> pada
     * rumus
     * {@code 1/(1+Error)} yang menyebabkan perbedaan fitness menjadi terlalu kecil
     * ketika error masih besar, sehingga memperlambat konvergensi di awal.
     * </p>
     */
    public void calculateFitness() {
        double totalError = 0.0;
        List<Clue> clues = puzzle.getClues();

        // Hitung akumulasi error dari seluruh clue
        for (Clue currentClue : clues) {
            int row = currentClue.getRow();
            int col = currentClue.getCol();
            int clueValue = currentClue.getValue();

            // Hitung jumlah sel hitam aktual di sekitar clue
            int blackCount = countBlackIn3x3Area(row, col);

            // Tambahkan selisih absolut ke total error
            totalError += Math.abs(blackCount - clueValue);
        }

        // Hitung Estimasi Maksimal Error yang mungkin terjadi (Worst Case)
        // Asumsi terburuk adalah setiap clue meleset maksimal 9 poin (misal clue 0 tapi ada 9
        // hitam)
        double maxPossibleError = clues.size() * 9.0;

        if (maxPossibleError == 0) {
            this.fitness = 1.0;
        } else {
            this.fitness = 1.0 - (totalError / maxPossibleError);
        }

        this.fitnessDirty = false;
    }

    /**
     * Menghitung jumlah sel hitam dalam area 3x3 yang berpusat pada (centerR,
     * centerC).
     * * @param centerR koordinat baris pusat
     * 
     * @param centerC koordinat kolom pusat
     * @return jumlah sel hitam (range 0-9)
     */
    public int countBlackIn3x3Area(int centerR, int centerC) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;
                // Pastikan koordinat valid dan sel berwarna hitam
                if (puzzle.isValidPosition(r, c) && grid[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Membuat salinan (deep copy) dari individu ini.
     * <p>
     * Metode ini penting dalam proses evolusi (seperti seleksi dan crossover)
     * untuk memastikan manipulasi pada individu baru tidak merusak data individu
     * lama.
     * </p>
     * * @return objek Individual baru yang identik secara data
     */
    public Individual copy() {
        Individual copy = new Individual(this.puzzle);

        copy.grid = new boolean[puzzle.getRows()][puzzle.getCols()];
        for (int i = 0; i < puzzle.getRows(); i++) {
            System.arraycopy(this.grid[i], 0, copy.grid[i], 0, puzzle.getCols());
        }

        copy.fitness = this.fitness;
        copy.fitnessDirty = this.fitnessDirty;

        return copy;
    }

    /**
     * Representasi String dari grid individu untuk keperluan debugging.
     * <p>
     * Menggunakan simbol '#' untuk sel hitam dan '.' untuk sel putih.
     * Simbol '█' digunakan untuk sel yang dikunci (fixed) agar mudah dibedakan.
     * </p>
     * * @return String visualisasi grid
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < puzzle.getRows(); r++) {
            for (int c = 0; c < puzzle.getCols(); c++) {
                if (puzzle.isFixed(r, c)) {
                    // Tanda visual untuk sel yang dikunci heuristik
                    sb.append(grid[r][c] ? "█" : "·");
                } else {
                    sb.append(grid[r][c] ? "#" : ".");
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Mengubah nilai fitness pada individu.
     * * @param fitness nilai fitness baru yang akan menggantikan
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}