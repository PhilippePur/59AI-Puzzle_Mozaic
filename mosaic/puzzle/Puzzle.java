package mosaic.puzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Puzzle} merepresentasikan definisi soal Mosaic/Fill-a-Pix.
 * <p>
 * Kelas ini berfungsi untuk:
 * <ul>
 * <li><Menyimpan dimensi papan permainan (baris dan kolom).</li>
 * <li>Menyimpan daftar petunjuk ({@link Clue}).</li>
 * <li>Membuat deduksi menggunakan heuristic awal
 * <li>Menyimpan status sel yang telah ditandai isFixed oleh proses heuristik awal.</li>
 * </ul>
 * <p>
 * Kelas ini dirancang agar {@code Individual} tidak perlu menyimpan
 * salinan status isFixed secara terpisah, melainkan cukup merujuk ke objek ini.
 * </p>
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

    public void preprocess() {
        // untuk semua clue yang ada, dicek apakah clue nya memenuhi beberapa hard
        // constraints
        for (Clue clue : clues) {
            int r = clue.getRow();
            int c = clue.getCol();
            int v = clue.getValue();

            List<int[]> validNeighbor = getValidNeighbors(r, c);
            int valid = validNeighbor.size();

            // kalo ada clue 0, berarti sekitarnya semua putih
            if (v == 0) {
                for (int deltaR = -1; deltaR <= 1; deltaR++) {
                    for (int deltaC = -1; deltaC <= 1; deltaC++) {
                        int tempR = r + deltaR;
                        int tempC = c + deltaC;

                        if (!isValidPosition(tempR, tempC)) // kalo misal posisi nya ga valid, gausa di masukin fixed
                                                            // (kyknya harusnya gaperlu tapi gapapa dicek aja)
                            continue;

                        setFixedCell(tempR, tempC, false);
                    }
                }
            }

            // kalo clue nya 9, item semua brrti
            else if (v == 9) {
                for (int deltaR = -1; deltaR <= 1; deltaR++) {
                    for (int deltaC = -1; deltaC <= 1; deltaC++) {
                        int tempR = r + deltaR;
                        int tempC = c + deltaC;

                        if (!isValidPosition(tempR, tempC)) // kalo misal posisi nya ga valid, gausa di masukin fixed
                                                            // (kyknya harusnya gaperlu tapi gapapa dicek aja)
                            continue;

                        setFixedCell(tempR, tempC, true);
                    }
                }
            }

            // kalo clue nya 4 dan cuma ada 4 valid neighbor(di corner) set item semua, kalo
            // 6 di tepian juga sama, dst
            else if (v == valid) {
                for (int[] cell : validNeighbor) {
                    setFixedCell(cell[0], cell[1], true); // gabole diubah ubah

                }
            }

            // ini mau apply buat yang kalo ada clue sebelahan, terus selisih nya 3,
            // bakal jadi item putih, sesuai yang di docs
            // kalo misal nya ada di pojokan brrti ada kemungkinan apply yang beda 2,
            // tinggal cek aja
            // CEK KANAN
            if (getClueAt(r, c + 1)!= null) {
                Clue other = getClueAt(r, c + 1);
                if (other.getCol() == cols - 1)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }

            // CEK KIRI
            if (getClueAt(r, c - 1)!= null) {
                Clue other = getClueAt(r, c - 1);
                if (other.getCol() == 0)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }

            // CEK BAWAH
            if (getClueAt(r + 1, c) != null) {
                Clue other = getClueAt(r + 1, c);
                if (other.getRow() == rows - 1)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }

            // CEK ATAS
            if (getClueAt(r - 1, c) != null) {
                Clue other = getClueAt(r - 1, c);
                if (other.getRow() == 0)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }
        }
    }

    // ini buat ngecek ada berapa tetangga gtu, nanti dipake buat kasus kek yang 4
    // di pojokan ato 6 di tepian, tinggal cek dia ada berapa tetangga valid
    public List<int[]> getValidNeighbors(int row, int col) {
        // array of integer cuma buat nyimpen row sama col aja, koordinat mana aja yang
        // tetangga nya si cell tersebut
        List<int[]> cells = new ArrayList<>();

        for (int deltaR = -1; deltaR <= 1; deltaR++) {
            for (int deltaC = -1; deltaC <= 1; deltaC++) {
                int neighborR = row + deltaR;
                int neighborC = col + deltaC;

                if (isValidPosition(neighborR, neighborC)) {
                    cells.add(new int[] { neighborR, neighborC });
                }
            }
        }
        return cells;
    }

    // function yang isinya ngefix in kalo misal nya ada beda 3 di kotak nya
    // p.s. gatau ya harus di cek apa ngga, tapi kyknya gamungkin invalid si
    // kotaknya kalo di set constraint gini, nanti di update lagi
    private void diffXConstraint(Clue a, Clue b, int x) {
        int valueA = a.getValue();
        int valueB = b.getValue();

        if (Math.abs(valueA - valueB) != x) // kalo ga beda 2/3 ternyata, yauda skip aja
            return;

        // biar ga diproses dua kali (A-B dan B-A), misal yang kirinya udah pernah,
        // yauda yang kanan nya gamungkin di proses lg
        if (a.getRow() > b.getRow()) // vertikal
            return;
        if (a.getRow() == b.getRow() && a.getCol() > b.getCol()) // horizontal
            return;

        // cari mana yang lebih kecil mana yang lebih gede
        Clue small = valueA < valueB ? a : b;
        Clue big = valueA > valueB ? a : b;

        int rowBig = big.getRow();
        int colBig = big.getCol();
        int rowSmall = small.getRow();
        int colSmall = small.getCol();

        for (int i = -1; i <= 1; i++) {
            int row, col;
            // big di kanan, small di kirinya
            if (colBig > colSmall) {
                // set putih di kiri small
                row = rowSmall + i;
                col = colSmall - 1;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, false);
                }

                // set item di kanan big
                row = rowBig + i;
                col = colBig + 1;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, true);
                }
            }
            // big di kiri, small di kanannya
            else if (colBig < colSmall) {
                // set putih di kanan small
                row = rowSmall + i;
                col = colSmall + 1;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, false);
                }

                // set item di kiri big
                row = rowBig + i;
                col = colBig - 1;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, true);
                }
            }
            // big di bawah
            else if (rowBig > rowSmall) {
                // set putih di atas small
                row = rowSmall - 1;
                col = colSmall + i;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, false);
                }

                // set item di bawah big
                row = rowBig + 1;
                col = colBig + i;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, true);
                }
            }
            // big di atas
            else {
                // set putih di bawah small
                row = rowSmall + 1;
                col = colSmall + i;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, false);
                }

                // set item di atas big
                row = rowBig - 1;
                col = colBig + i;
                if (isValidPosition(row, col)) {
                    setFixedCell(row, col, true);
                }
            }
        }
    }
}
