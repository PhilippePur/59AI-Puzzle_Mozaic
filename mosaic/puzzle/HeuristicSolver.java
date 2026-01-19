package mosaic.puzzle;

/**
 * Kelas untuk menerapkan aturan heuristik logika pada @param Puzzle.
 * <p>
 * Kelas ini menerapkan aturan heurstic pasti untuk mengunci sel-sel yang
 * memiliki jawaban pasti sebelum Algoritma Genetik dimulai,
 * sehingga mengurangi ruang pencarian.
 * </p>
 * 
 * @author Michael P, Gregorius J membuat struktur dasar dan ide
 *         implementasi sebagian method dibuat dengan bantuan LLM Gemini 3 Pro
 * 
 */
public class HeuristicSolver {

    /**
     * Menjalankan seluruh aturan heuristik pada puzzle.
     * Metode ini hanya dipanggil satu kali sebelum inisialisasi populasi.
     * * @param puzzle Objek puzzle yang akan diproses
     * ;
     * 
     * @param limit batas maksimal iterasi pengisian inisialisasi awal, agar tidak
     *              terlalu lama jika -1 artinya tidak ada batasan. Secara default
     *              nilai ini diset -1 di main class, namun jika ingin digunakan
     *              nilai harus positif int
     * 
     * @return jumlah total sel yang dikunci oleh heuristik
     */

    public static int applyHeuristics(Puzzle puzzle, int limit) {
        int initialFixed = countFixedCells(puzzle);
        int counter = 0;
        if (limit == -1) {
            limit = Integer.MAX_VALUE;
        }
        boolean changed = true;
        /**
         * Lakukan loop terus menerus sampai tidak ada lagi sel baru yang bisa
         * dikunci,atau hingga batas iterasi
         */
        while (changed && counter < limit) {
            int fixedBefore = countFixedCells(puzzle);

            applyBasicAndCapacityRules(puzzle);
            applyOrthogonalRules(puzzle);
            applyDiagonalRules(puzzle);

            int fixedAfter = countFixedCells(puzzle);
            changed = fixedAfter > fixedBefore;
            counter++;
        }

        int totalFixed = countFixedCells(puzzle) - initialFixed;
        return totalFixed;
    }

    /**
     * Digunakan untuk menghitung berapa jumlah cell yang sudah di fixed pada puzzle
     * 
     * @param puzzle object puzzle yang akan diproses
     * @return jumlah fixed cell yang ada pada puzzle
     */
    private static int countFixedCells(Puzzle puzzle) {
        int count = 0;
        for (int r = 0; r < puzzle.getRows(); r++) {
            for (int c = 0; c < puzzle.getCols(); c++) {
                if (puzzle.isFixed(r, c))
                    count++;
            }
        }
        return count;
    }

    /**
     * Menerapkan Aturan Dasar dan Kapasitas Penuh.
     * A. Jika angka 0 maka semua tetangga PUTIH.
     * B. Jika angka == jumlah tetangga valid maka semua tetangga HITAM.
     * - Clue 9 di tengah (9 tetangga) maka semua Hitam
     * - Clue 6 di tepi (6 tetangga) maka Semua Hitam
     * - Clue 4 di pojok (4 tetangga) maka Semua Hitam
     * 
     * @param puzzle object puzzle yang akan diproses
     */
    private static void applyBasicAndCapacityRules(Puzzle puzzle) {
        for (Clue clue : puzzle.getClues()) {
            if (clue.getValue() == 0) {
                fillArea3x3(puzzle, clue.getRow(), clue.getCol(), false);
            } else {
                int validNeighbors = countValidNeighbors(puzzle, clue.getRow(), clue.getCol());

                // Jika nilai clue sama dengan jumlah tetangga, maka semuanya PASTI
                // HITAM
                if (clue.getValue() == validNeighbors) {
                    fillArea3x3(puzzle, clue.getRow(), clue.getCol(), true);
                }
            }
        }
    }

    /**
     * Menerapkan Aturan Angka Bersebelahan (Horizontal & Vertikal).
     * Menangani Aturan Selisih 3 (Tengah) dan Selisih 2 (tepi).
     * 
     * @param puzzle object puzzle yang akan diproses
     */
    private static void applyOrthogonalRules(Puzzle puzzle) {
        for (Clue clueA : puzzle.getClues()) {
            int r = clueA.getRow();
            int c = clueA.getCol();

            // apply aturan angka bersebelahan untuk horizontal
            // cek tetangga kanan
            Clue clueB = puzzle.getClueAt(r, c + 1);
            if (clueB != null) {
                int diff = clueA.getValue() - clueB.getValue();

                // area Unik adalah area yang tidak beririsan terhadap suatu clue lain
                // area Unik A terhadap B ada di kolom kiri (c - 1)
                // area Unik B terhadap A ada di kolom kanan (c + 2)

                // hitung kapasitas valid area unik
                int validCellsA = countValidCellsInColumn(puzzle, r, c - 1);
                int validCellsB = countValidCellsInColumn(puzzle, r, c + 2);

                // Jika Diff == +KapasitasA maka Ujung A Hitam, Ujung B Putih (A > B)
                if (diff == validCellsA) {
                    fillColumn3(puzzle, r, c - 1, true);
                    fillColumn3(puzzle, r, c + 2, false);
                }
                // Jika Diff == -KapasitasB maka Ujung A Putih, Ujung B Hitam (A < B)
                else if (diff == -validCellsB) {
                    fillColumn3(puzzle, r, c - 1, false);
                    fillColumn3(puzzle, r, c + 2, true);
                }
            }

            // apply aturan angka bersebelahan untuk vertikal
            // cek tetangga bawah
            Clue clueBottom = puzzle.getClueAt(r + 1, c);
            if (clueBottom != null) {
                int diff = clueA.getValue() - clueBottom.getValue();

                // area Unik adalah area yang tidak beririsan terhadap suatu clue lain
                // area Unik A terhadap B ada di baris atas (r - 1)
                // area Unik B terhadap A ada di baris bawah (r + 2)

                int validCellsA = countValidCellsInRow(puzzle, r - 1, c);
                int validCellsB = countValidCellsInRow(puzzle, r + 2, c);

                // Jika Diff == +KapasitasA maka Ujung A Hitam, Ujung B Putih (A > B)
                if (diff == validCellsA) {
                    fillRow3(puzzle, r - 1, c, true);
                    fillRow3(puzzle, r + 2, c, false);
                    // Jika Diff == -KapasitasB maka Ujung A Putih, Ujung B Hitam (A < B)
                } else if (diff == -validCellsB) {
                    fillRow3(puzzle, r - 1, c, false);
                    fillRow3(puzzle, r + 2, c, true);
                }
            }
        }
    }

    /**
     * Menerapkan Aturan Angka Bersebelahan Diagonal (Selisih 5).
     * 
     * @param puzzle object puzzle yang akan diproses
     */
    private static void applyDiagonalRules(Puzzle puzzle) {
        for (Clue clueA : puzzle.getClues()) {
            int r = clueA.getRow();
            int c = clueA.getCol();

            // apply aturan angka bersebelahan diagonal untuk miring
            // dengan kemiringan (gradien) negatif
            // Cek Diagonal Kanan-Bawah
            Clue clueB = puzzle.getClueAt(r + 1, c + 1);
            if (clueB != null) {
                int diff = clueA.getValue() - clueB.getValue();

                // area Unik adalah area yang tidak beririsan terhadap suatu clue lain
                // area Unik A terhadap B: Bentuk L terbalik di kiri-atas
                // area Unik B terhadap A: Bentuk L di kanan-bawah

                // Validasi kapasitas (maksimal 5, bisa kurang kalau di pojok)
                int validA = countValidLShape(puzzle, r, c, -1, -1); // L di kiri-atas relative thd A
                int validB = countValidLShape(puzzle, r + 1, c + 1, 1, 1); // L di kanan-bawah relative thd B

                // jika selisihnya adalah jumlah cell valid A maka fill area unik A terhadap B
                // (kalau A > B)
                if (diff == validA) {
                    fillLShape(puzzle, r, c, -1, -1, true);
                    fillLShape(puzzle, r + 1, c + 1, 1, 1, false);
                    // jika selisihnya adalah jumlah cell valid B maka fill area unik B terhadap A
                    // (kalau A < B)
                } else if (diff == -validB) {
                    fillLShape(puzzle, r, c, -1, -1, false);
                    fillLShape(puzzle, r + 1, c + 1, 1, 1, true);
                }
            }

            // apply aturan angka bersebelahan diagonal untuk miring
            // dengan kemiringan(gradien) negatif
            // Cek Diagonal Kiri-Bawah
            Clue clueBL = puzzle.getClueAt(r + 1, c - 1);
            if (clueBL != null) {
                int diff = clueA.getValue() - clueBL.getValue();
                int validA = countValidLShape(puzzle, r, c, -1, 1); // L di kanan-atas relative thd A
                int validB = countValidLShape(puzzle, r + 1, c - 1, 1, -1); // L di kiri-bawah relative thd B

                // jika selisihnya adalah jumlah cell valid A maka fill area unik A terhadap B
                // (kalau A > B)
                if (diff == validA) {
                    fillLShape(puzzle, r, c, -1, 1, true);
                    fillLShape(puzzle, r + 1, c - 1, 1, -1, false);
                    // jika selisihnya adalah jumlah cell valid B maka fill area unik B terhadap A
                    // (kalau A < B)
                } else if (diff == -validB) {
                    fillLShape(puzzle, r, c, -1, 1, false);
                    fillLShape(puzzle, r + 1, c - 1, 1, -1, true);
                }
            }
        }
    }

    /**
     * Menghitung jumlah tetangga yang valid (berada di dalam papan) untuk suatu
     * koordinat clue.
     * Digunakan untuk mengecek kapasitas maksimal sebuah clue.
     * 
     * @param puzzle  object puzzle yang akan diproses
     * @param centerR letak baris clue
     * @param centerC letak kolom clue
     */
    private static int countValidNeighbors(Puzzle puzzle, int centerR, int centerC) {
        int count = 0;
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            for (int c = centerC - 1; c <= centerC + 1; c++) {
                if (puzzle.isValidPosition(r, c)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Digunakan untuk mengisi area 3 x 3 yang valid dengan {@param value}
     * sudah menangani kondisi di sisi atau di pojok
     * 
     * @param puzzle  Objek puzzle yang akan diproses
     * @param centerR letak baris clue
     * @param centerC letak kolom clue
     * @param value   {@code true} = Hitam {@code false} = Putih
     */
    private static void fillArea3x3(Puzzle puzzle, int centerR, int centerC, boolean value) {
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            for (int c = centerC - 1; c <= centerC + 1; c++) {
                if (puzzle.isValidPosition(r, c) && !puzzle.isFixed(r, c)) {
                    puzzle.setFixedCell(r, c, value);
                }
            }
        }
    }

    /**
     * Digunakan untuk menghitung berapa total cell yang valid (dalam papan) pada
     * suatu kolom 3 x 1 (atas, tengah, bawah)
     * 
     * @param puzzle
     * @param centerR
     * @param targetC
     * @return total kolom yang valid
     */
    private static int countValidCellsInColumn(Puzzle puzzle, int centerR, int targetC) {
        int count = 0;
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            if (puzzle.isValidPosition(r, targetC))
                count++;
        }
        return count;
    }

    /**
     * Digunakan untuk mengisi suatu kolom 3 x 1 (atas, tengah, bawah)
     * 
     * @param puzzle
     * @param centerR
     * @param targetC
     * @param value
     */
    private static void fillColumn3(Puzzle puzzle, int centerR, int targetC, boolean value) {
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            if (puzzle.isValidPosition(r, targetC) && !puzzle.isFixed(r, targetC)) {
                puzzle.setFixedCell(r, targetC, value);
            }
        }
    }

    /**
     * Digunakan untuk menghitung berapa jumlah cell yang valid (dalam papan) pada
     * suatu baris 1 x 3 (kiri, tengah, kanan)
     * 
     * @param puzzle
     * @param targetR
     * @param centerC
     * @return
     */
    private static int countValidCellsInRow(Puzzle puzzle, int targetR, int centerC) {
        int count = 0;
        for (int c = centerC - 1; c <= centerC + 1; c++) {
            if (puzzle.isValidPosition(targetR, c))
                count++;
        }
        return count;
    }

    /**
     * Digunakan untuk mengisi 3 buah cell (kiri, tengah, kanan) dalam suatu baris
     * jika valid
     * 
     * @param puzzle
     * @param targetR
     * @param centerC
     * @param value
     */
    private static void fillRow3(Puzzle puzzle, int targetR, int centerC, boolean value) {
        for (int c = centerC - 1; c <= centerC + 1; c++) {
            if (puzzle.isValidPosition(targetR, c) && !puzzle.isFixed(targetR, c)) {
                puzzle.setFixedCell(targetR, c, value);
            }
        }
    }

    /**
     * Digunakan untuk menghitung berapa cell yang valid pada cell dengan L shape
     * 
     * @param puzzle  object puzzle yang akan diproses
     * @param centerR letak baris clue
     * @param centerC letak kolom clue
     * @param dr      arah row (1 atau -1) 1 artinya cek L yang di bawah, -1 artinya
     *                cek L yang di atas
     * @param dc      arah column (1 atau -1) 1 artinya cek L yang di kanan, -1
     *                artinya cek L yang di kiri
     * @return total cell yang valid dalam L shape
     */
    private static int countValidLShape(Puzzle puzzle, int centerR, int centerC, int dr, int dc) {
        int count = 0;
        // Cek baris horizontal dari L
        for (int c = centerC - 1; c <= centerC + 1; c++) {
            if (puzzle.isValidPosition(centerR + dr, c))
                count++;
        }
        // Cek kolom vertikal dari L
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            if (r == centerR + dr)
                continue; // Jangan hitung pojok dua kali
            if (puzzle.isValidPosition(r, centerC + dc))
                count++;
        }
        return count;
    }

    /**
     * Digunakan untuk menghitung berapa cell yang valid pada cell dengan L shape
     * 
     * @param puzzle  object puzzle yang akan diproses
     * @param centerR letak baris clue
     * @param centerC letak kolom clue
     * @param dr      arah row (1 atau -1) 1 artinya cek L yang di bawah, -1 artinya
     *                cek L yang di atas
     * @param dc      arah column (1 atau -1) 1 artinya cek L yang di kanan, -1
     *                artinya cek L yang di kiri
     * @param value   {@code true} artinya cell diwarnai hitam dan {@code false}
     *                artinya cell diwarnai putih
     */
    private static void fillLShape(Puzzle puzzle, int centerR, int centerC, int dr, int dc, boolean value) {
        // Isi baris horizontal
        for (int c = centerC - 1; c <= centerC + 1; c++) {
            if (puzzle.isValidPosition(centerR + dr, c) && !puzzle.isFixed(centerR + dr, c)) {
                puzzle.setFixedCell(centerR + dr, c, value);
            }
        }
        // Isi kolom vertikal
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            if (r == centerR + dr)
                continue; // Jangan isi pojok dua kali
            if (puzzle.isValidPosition(r, centerC + dc) && !puzzle.isFixed(r, centerC + dc)) {
                puzzle.setFixedCell(r, centerC + dc, value);
            }
        }
    }
}