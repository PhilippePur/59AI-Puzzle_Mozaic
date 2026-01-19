package mosaic.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Puzzle} merepresentasikan definisi Mosaic/Fill-a-Pix.
 * <p>
 * Kelas ini berfungsi untuk:
 * <ul>
 * <li><Menyimpan dimensi papan permainan (baris dan kolom).</li>
 * <li>Menyimpan daftar petunjuk ({@link Clue}).</li>
 * <li>Menyimpan status sel yang telah ditandai isFixed oleh proses heuristik awal.</li>
 * </ul>
 * <p>
 * Kelas ini dirancang agar {@code Individual} tidak perlu menyimpan
 * salinan status isFixed secara terpisah, melainkan cukup merujuk ke objek ini.
 * </p>
 * @author Michael G, Michael P
 */
public class Puzzle {
    
    /** Jumlah baris pada papan permainan. */
    private final int rows;

    /** Jumlah kolom pada papan permainan. */
    private final int cols;

    /** Daftar semua clue (angka petunjuk) yang ada pada puzzle. */
    private final List<Clue> clues;
    
    /** * Matriks penanda apakah sebuah sel dikunci. 
     * {@code true} berarti sel tersebut telah dipastikan nilainya oleh heuristik 
     * dan tidak boleh diubah oleh algoritma genetik.
     */
    private final boolean[][] isFixed;

    /** * Matriks yang menyimpan nilai warna (Hitam/Putih) untuk sel yang dikunci.
     * Hanya relevan jika {@code isFixed[r][c]} bernilai {@code true}.
     */
    private final boolean[][] fixedValues;

    /**
     * Konstruktor untuk membuat objek Puzzle kosong dengan dimensi tertentu.
     * * @param rows jumlah baris
     * @param cols jumlah kolom
     */
    public Puzzle(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.clues = new ArrayList<>();
        this.isFixed = new boolean[rows][cols];
        this.fixedValues = new boolean[rows][cols];
    }

    /**
     * Menambahkan clue baru ke dalam puzzle.
     * @param clue objek {@link Clue} yang akan ditambahkan
     */
    public void addClue(Clue clue) {
        this.clues.add(clue);
    }

    /**
     * Mengunci status sel tertentu dengan nilai warna yang pasti.
     * Metode ini biasanya dipanggil oleh proses <i>Heuristic Pre-processing</i>.
     * @param r indeks baris
     * @param c indeks kolom
     * @param value nilai warna yang benar ({@code true} = Hitam, {@code false} = Putih)
     */
    public void setFixedCell(int r, int c, boolean value) {
        if (isValidPosition(r, c)) {
            isFixed[r][c] = true;
            fixedValues[r][c] = value;
        }
    }

    /**
     * Memeriksa apakah sel pada posisi tertentu statusnya terkunci (Fixed).
     * @param r indeks baris
     * @param c indeks kolom
     * @return {@code true} jika sel terkunci, {@code false} jika bebas
     */
    public boolean isFixed(int r, int c) {
        if (!isValidPosition(r, c)) return false;
        return isFixed[r][c];
    }

    /**
     * Mendapatkan nilai warna yang benar untuk sel yang terkunci.
     * @param r indeks baris
     * @param c indeks kolom
     * @return {@code true} jika Hitam, {@code false} jika Putih
     */
    public boolean getFixedValue(int r, int c) {
        return fixedValues[r][c];
    }

    /**
     * @return jumlah baris papan
     */
    public int getRows() { return rows; }

    /**
     * @return jumlah kolom papan
     */
    public int getCols() { return cols; }

    /**
     * @return daftar seluruh clue dalam puzzle
     */
    public List<Clue> getClues() { return clues; }

    /**
     * Memeriksa apakah koordinat (r, c) berada di dalam batas papan permainan.
     * @param r indeks baris
     * @param c indeks kolom
     * @return {@code true} jika posisi valid
     */
    public boolean isValidPosition(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }
    
    /**
     * Mengambil objek {@link Clue} pada posisi tertentu.
     * @param r indeks baris
     * @param c indeks kolom
     * @return objek Clue jika ada, atau {@code null} jika tidak ada
     */
    public Clue getClueAt(int r, int c) {
        for (Clue clue : clues) {
            if (clue.getRow() == r && clue.getCol() == c) return clue;
        }
        return null;
    }

    
}
