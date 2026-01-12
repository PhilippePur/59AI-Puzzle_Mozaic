package mosaic.puzzle;

import java.util.List;
import java.util.ArrayList;

public class Puzzle {
    private List<Clue> clues = new ArrayList<>();
    private int rows;
    private int cols;
    private boolean[][] fixedValues;
    private boolean[][] fixedCells;

    public Puzzle(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.fixedCells = new boolean[rows][cols]; // menandai apakah si cell itu boleh disentuh/diubah atau ngga
        this.fixedValues = new boolean[rows][cols]; // menandai warna dari si cells nya
    }

    // Constructor untuk puzzle dengan fixed cells
    public Puzzle(int rows, int cols, boolean[][] fixedCells) {
        this(rows, cols);
        if (fixedCells != null)
            this.fixedCells = fixedCells;
    }

    public List<Clue> getClues() {
        return new ArrayList<>(clues); // Return copy untuk encapsulation
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean[][] getFixedCells() {
        boolean[][] copy = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(fixedCells[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    public boolean isFixed(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        return fixedCells[row][col];
    }

    public void setFixedCell(int row, int col, boolean fixed) {
        if (isValidPosition(row, col)) {
            fixedCells[row][col] = fixed;
        }
    }

    // VALIDATION METHODS
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public boolean hasClueAt(int row, int col) {
        for (Clue clue : clues) {
            if (clue.getRow() == row && clue.getCol() == col) {
                return true;
            }
        }
        return false;
    }

    public Clue getClueAt(int row, int col) {
        for (Clue clue : clues) {
            if (clue.getRow() == row && clue.getCol() == col) {
                return clue;
            }
        }
        return null;
    }

    public boolean getFixedValue(int r, int c) {
        return fixedValues[r][c];
    }

    // ini function buat ngefixin dia item ato putih
    public void setFixedValue(int row, int col, boolean value) {
        fixedValues[row][col] = value;
    }

    // UTILITY METHODS
    public int getTotalClues() {
        return clues.size();
    }

    public double getClueDensity() {
        if (rows * cols == 0)
            return 0.0;
        return (double) clues.size() / (rows * cols);
    }

    // Untuk debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Puzzle %d x %d (%d clues, density: %.2f)\n",
                rows, cols, clues.size(), getClueDensity()));

        sb.append("Clues:\n");
        for (Clue clue : clues) {
            sb.append(String.format("  (%d, %d) = %d\n",
                    clue.getRow(), clue.getCol(), clue.getValue()));
        }

        sb.append("Fixed cells:\n");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                sb.append(fixedCells[r][c] ? "X" : ".");
            }
            sb.append("\n");
        }

        return sb.toString();

    }

    // method buat preprocessing, apply semua hard constraint yang ada ditemukan
    // secara intuitif untuk menghasilkan initial population yang lebih baik
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

                        setFixedValue(tempR, tempC, false);
                        setFixedCell(tempR, tempC, true);
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

                        setFixedValue(tempR, tempC, true);
                        setFixedCell(tempR, tempC, true);
                    }
                }
            }

            // kalo clue nya 4 dan cuma ada 4 valid neighbor(di corner) set item semua, kalo
            // 6 di tepian juga sama, dst
            else if (v == valid) {
                for (int[] cell : validNeighbor) {
                    setFixedValue(cell[0], cell[1], true); // hitam
                    setFixedCell(cell[0], cell[1], true); // gabole diubah ubah

                }
            }

            // ini mau apply buat yang kalo ada clue sebelahan, terus selisih nya 3,
            // bakal jadi item putih, sesuai yang di docs
            // kalo misal nya ada di pojokan brrti ada kemungkinan apply yang beda 2,
            // tinggal cek aja
            // CEK KANAN
            if (hasClueAt(r, c + 1)) {
                Clue other = getClueAt(r, c + 1);
                if (other.getCol() == cols - 1)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }

            // CEK KIRI
            if (hasClueAt(r, c - 1)) {
                Clue other = getClueAt(r, c - 1);
                if (other.getCol() == 0)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }

            // CEK BAWAH
            if (hasClueAt(r + 1, c)) {
                Clue other = getClueAt(r + 1, c);
                if (other.getRow() == rows - 1)
                    diffXConstraint(other, clue, 2);
                diffXConstraint(other, clue, 3);
            }

            // CEK ATAS
            if (hasClueAt(r - 1, c)) {
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
                    setFixedValue(row, col, false);
                    setFixedCell(row, col, true);
                }

                // set item di kanan big
                row = rowBig + i;
                col = colBig + 1;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, true);
                    setFixedCell(row, col, true);
                }
            }
            // big di kiri, small di kanannya
            else if (colBig < colSmall) {
                // set putih di kanan small
                row = rowSmall + i;
                col = colSmall + 1;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, false);
                    setFixedCell(row, col, true);
                }

                // set item di kiri big
                row = rowBig + i;
                col = colBig - 1;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, true);
                    setFixedCell(row, col, true);
                }
            }
            // big di bawah
            else if (rowBig > rowSmall) {
                // set putih di atas small
                row = rowSmall - 1;
                col = colSmall + i;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, false);
                    setFixedCell(row, col, true);
                }

                // set item di bawah big
                row = rowBig + 1;
                col = colBig + i;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, true);
                    setFixedCell(row, col, true);
                }
            }
            // big di atas
            else {
                // set putih di bawah small
                row = rowSmall + 1;
                col = colSmall + i;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, false);
                    setFixedCell(row, col, true);
                }

                // set item di atas big
                row = rowBig - 1;
                col = colBig + i;
                if (isValidPosition(row, col)) {
                    setFixedValue(row, col, true);
                    setFixedCell(row, col, true);
                }
            }
        }
    }
}
