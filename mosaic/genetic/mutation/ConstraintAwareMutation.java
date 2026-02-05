package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import mosaic.puzzle.Clue;
import mosaic.util.PatternCache;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

/**
 * Strategi mutasi cerdas yang mencoba memperbaiki area di sekitar clue yang memiliki error.
 * <p>
 * Strategi ini menggunakan {@link PatternCache} untuk menemukan pola 3x3 yang valid secara lokal
 * dan menyuntikkannya ke dalam individu. Mutasi hanya dijalankan berdasarkan probabilitas
 * {@code mutationRate}.
 * </p>
 */
public class ConstraintAwareMutation implements MutationStrategy {

    private final Random random;
    private final PatternCache patternCache;
    private final double mutationRate;

    /**
     * Konstruktor untuk inisialisasi strategi.
     * @param random Generator angka acak.
     * @param mutationRate Probabilitas terjadinya mutasi (0.0 - 1.0).
     */
    public ConstraintAwareMutation(Random random, double mutationRate) {
        this.random = random;
        this.patternCache = PatternCache.getInstance();
        this.mutationRate = mutationRate;
    }

    /**
     * Melakukan mutasi pada individu dengan memperbaiki area clue yang bermasalah.
     * <p>
     * Proses:
     * 1. Cek probabilitas berdasarkan mutationRate.
     * 2. Cari clue yang memiliki error (jumlah tetangga hitam tidak sesuai).
     * 3. Pilih salah satu clue yang bermasalah.
     * 4. Cari pola 3x3 yang valid untuk clue tersebut dan kompatibel dengan sel fixed di sekitarnya.
     * 5. Terapkan pola baru ke grid individu.
     * 
     * PENTING: Jika tidak ada compatible pattern ditemukan, throw exception untuk discard individual.
     * Ini menunjukkan individual invalid dan harus dihilangkan dari evolusi.
     * </p>
     * @throws IllegalStateException jika incompatibility detected (individual harus di-discard)
     */
    @Override
    public void mutate(Individual individual, Puzzle puzzle) {
        // 1. CEK PROBABILITAS (Gatekeeper)
        if (random.nextDouble() > mutationRate) {
            return;
        }

        // 2. Logika Mutasi
        List<Clue> problematicClues = findProblematicClues(individual, puzzle);

        if (problematicClues.isEmpty()) {
            // Tidak ada clue dengan error, tidak perlu mutasi
            return;
        }

        Clue selectedClue = selectClueToFix(problematicClues);
        if (selectedClue == null) return;

        int centerR = selectedClue.getRow();
        int centerC = selectedClue.getCol();

        // Get current state & fixed mask
        boolean[][] currentState = get3x3State(individual, centerR, centerC);
        boolean[][] isFixed = get3x3FixedMask(individual, centerR, centerC);

        // Get COMPATIBLE patterns
        List<boolean[][]> compatiblePatterns = patternCache.getCompatiblePatterns(
            selectedClue.getValue(), currentState, isFixed);

        if (compatiblePatterns.isEmpty()) {
            // INCOMPATIBILITY DETECTED!
            // Individual ini invalid, harus di-discard
            throw new IllegalStateException(
                "Constraint incompatibility at clue (" + centerR + "," + centerC + ") value=" + selectedClue.getValue() + 
                ". Individual is invalid and must be discarded."
            );
        }

        boolean[][] newPattern = selectNewPattern(compatiblePatterns, individual, selectedClue);
        applyPatternToArea(individual, selectedClue, newPattern);

        individual.markDirty(); 
    }

    // --- Helper Methods ---

    /** Mengambil snapshot area 3x3 dari grid individu saat ini. */
    private boolean[][] get3x3State(Individual individual, int centerR, int centerC) {
        boolean[][] state = new boolean[3][3];
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;
                if (r >= 0 && r < individual.getRows() && c >= 0 && c < individual.getCols()) {
                    state[dr + 1][dc + 1] = individual.getCell(r, c);
                }
            }
        }
        return state;
    }

    /** Mengambil mask boolean yang menandakan sel mana yang 'fixed' di area 3x3. */
    private boolean[][] get3x3FixedMask(Individual individual, int centerR, int centerC) {
        boolean[][] isFixed = new boolean[3][3];
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;
                if (r >= 0 && r < individual.getRows() && c >= 0 && c < individual.getCols()) {
                    isFixed[dr + 1][dc + 1] = individual.isFixed(r, c);
                }
            }
        }
        return isFixed;
    }

    /** Mencari semua clue yang saat ini belum terpenuhi (jumlah tetangga hitam salah). */
    private List<Clue> findProblematicClues(Individual individual, Puzzle puzzle) {
        List<Clue> allClues = puzzle.getClues();
        List<ClueWithError> cluesWithError = new ArrayList<>();

        for (Clue clue : allClues) {
            int actualBlack = countBlackIn3x3Area(individual, clue.getRow(), clue.getCol());
            int error = Math.abs(actualBlack - clue.getValue());
            if (error > 0) {
                cluesWithError.add(new ClueWithError(clue, error));
            }
        }

        cluesWithError.sort(Comparator.comparingInt(ClueWithError::getError).reversed());

        List<Clue> problematic = new ArrayList<>();
        for (ClueWithError cwe : cluesWithError) {
            problematic.add(cwe.clue);
        }
        return problematic;
    }

    /** Helper class internal untuk menyimpan clue beserta besaran errornya. */
    private static class ClueWithError {
        final Clue clue;
        final int error;
        ClueWithError(Clue clue, int error) { this.clue = clue; this.error = error; }
        int getError() { return error; }
    }

    /** Memilih satu clue untuk diperbaiki, memprioritaskan yang errornya paling besar. */
    private Clue selectClueToFix(List<Clue> problematicClues) {
        if (problematicClues.isEmpty()) return null;
        if (problematicClues.size() <= 3) return problematicClues.get(0);
        
        if (random.nextDouble() < 0.7) {
            return problematicClues.get(random.nextInt(Math.min(3, problematicClues.size())));
        } else {
            return problematicClues.get(random.nextInt(problematicClues.size()));
        }
    }

    /** Memilih pola baru secara acak dari daftar pola yang valid. */
    private boolean[][] selectNewPattern(List<boolean[][]> validPatterns, Individual individual, Clue clue) {
        return validPatterns.get(random.nextInt(validPatterns.size()));
    }

    /** Menerapkan pola 3x3 ke grid individu pada lokasi clue. */
    private void applyPatternToArea(Individual individual, Clue clue, boolean[][] pattern) {
        int centerR = clue.getRow();
        int centerC = clue.getCol();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;
                if (r >= 0 && r < individual.getRows() && c >= 0 && c < individual.getCols() && !individual.isFixed(r, c)) {
                    individual.setCell(r, c, pattern[dr + 1][dc + 1]);
                }
            }
        }
    }

    /** Menghitung jumlah sel hitam di area 3x3. */
    private int countBlackIn3x3Area(Individual individual, int centerR, int centerC) {
        int count = 0;
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            for (int c = centerC - 1; c <= centerC + 1; c++) {
                if (r >= 0 && r < individual.getRows() && c >= 0 && c < individual.getCols() && individual.getCell(r, c)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public String getStrategyName() {
        return "ConstraintAwareMutation";
    }
}