package mosaic.puzzle;

/**
 * Objek yang merepresentasikan angka petunjuk (clue) pada papan permainan Mosaic.
 * <p>
 * Sebuah clue memiliki posisi koordinat (baris dan kolom) dan nilai angka yang menunjukkan berapa banyak sel hitam di sekitarnya (termasuk dirinya sendiri).
 * </p>
 * @author Michael G
 */
public class Clue {
    /** Indeks baris. */
    private int row;

    /** Indeks kolom. */
    private int col;

    /** Nilai angka clue (0-9). */
    private int value;

    /**
     * Konstruktor untuk membuat objek Clue baru.
     *
     * @param row   indeks baris
     * @param col   indeks kolom
     * @param value nilai clue
     */
    public Clue(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    /**
     * @return indeks baris
     */
    public int getRow() {
        return row;
    }

    /**
     * @return indeks kolom
     */
    public int getCol() {
        return col;
    }

    /**
     * @return nilai angka clue
     */
    public int getValue() {
        return value;
    }
}