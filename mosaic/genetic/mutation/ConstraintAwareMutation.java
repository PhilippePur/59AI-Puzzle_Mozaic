package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import mosaic.puzzle.Clue;
import mosaic.util.PatternCache;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class ConstraintAwareMutation implements MutationStrategy {

    private final Random random;
    private final PatternCache patternCache;

    public ConstraintAwareMutation(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random generator tidak boleh null");
        }
        this.random = random;
        this.patternCache = PatternCache.getInstance();
    }

    @Override
    public void mutate(Individual individual, Puzzle puzzle) {
        List<Clue> problematicClues = findProblematicClues(individual, puzzle);

        if (problematicClues.isEmpty()) {// ini gw pgnnya gausa di mutate lagi kan udh bnr smua tapi gatau boleh ga

            fallbackBasicMutation(individual);
            return;
        }

        Clue selectedClue = selectClueToFix(problematicClues);
        List<boolean[][]> validPatterns = patternCache.getValidPatterns(selectedClue.getValue());

        if (validPatterns.isEmpty()) {
            fallbackBasicMutation(individual);
            return;
        }

        // Pilih pattern yang berbeda dengan current state
        boolean[][] newPattern = selectNewPattern(validPatterns, individual, selectedClue);
        applyPatternToArea(individual, selectedClue, newPattern);

        individual.markDirty(); // Tandai bahwa fitness perlu dihitung ulang
    }

    private List<Clue> findProblematicClues(Individual individual, Puzzle puzzle) {
        List<Clue> allClues = puzzle.getClues();
        List<ClueWithError> cluesWithError = new ArrayList<>();

        // Hitung error untuk setiap clue sekaligus
        for (Clue clue : allClues) {
            int actualBlack = countBlackIn3x3Area(individual, clue.getRow(), clue.getCol());
            int error = Math.abs(actualBlack - clue.getValue());

            if (error > 0) {
                cluesWithError.add(new ClueWithError(clue, error));
            }
        }

        // Sort berdasarkan error (descending)
        cluesWithError.sort(Comparator.comparingInt(ClueWithError::getError).reversed());

        // Extract hanya clue-nya
        List<Clue> problematic = new ArrayList<>();
        for (ClueWithError cwe : cluesWithError) {
            problematic.add(cwe.clue);
        }

        return problematic;
    }

    // Helper class untuk simpan clue dan error-nya
    private static class ClueWithError {
        final Clue clue;
        final int error;

        ClueWithError(Clue clue, int error) {
            this.clue = clue;
            this.error = error;
        }

        int getError() {
            return error;
        }
    }

    private Clue selectClueToFix(List<Clue> problematicClues) {
        if (problematicClues.isEmpty())
            return null;

        if (problematicClues.size() <= 3) {
            return problematicClues.get(0);
        }

        // EXPERIMENT
        if (random.nextDouble() < 0.7) {
            return problematicClues.get(random.nextInt(Math.min(3, problematicClues.size())));
        } else {
            return problematicClues.get(random.nextInt(problematicClues.size()));
        }
    }

    private boolean[][] selectNewPattern(List<boolean[][]> validPatterns, Individual individual, Clue clue) {
        // Pilih random pattern
        return validPatterns.get(random.nextInt(validPatterns.size()));

        // bisa juga ambil yang paling beda sama current biar semakin diverse
    }

    private void applyPatternToArea(Individual individual, Clue clue, boolean[][] pattern) {
        int centerR = clue.getRow();
        int centerC = clue.getCol();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;

                if (r >= 0 && r < individual.getRows() &&
                        c >= 0 && c < individual.getCols() &&
                        !individual.isFixed(r, c)) {

                    individual.setCell(r, c, pattern[dr + 1][dc + 1]);
                }
            }
        }
    }

    private void fallbackBasicMutation(Individual individual) {
        // Mutation rate sangat rendah (0.1%) karena hanya fallback
        boolean changed = false;
        for (int r = 0; r < individual.getRows(); r++) {
            for (int c = 0; c < individual.getCols(); c++) {
                if (!individual.isFixed(r, c) && random.nextDouble() < 0.001) {
                    individual.flipCell(r, c);
                    changed = true;
                }
            }
        }

        if (changed) {
            individual.markDirty();
        }
    }

    private int countBlackIn3x3Area(Individual individual, int centerR, int centerC) {
        int count = 0;
        for (int r = centerR - 1; r <= centerR + 1; r++) {
            for (int c = centerC - 1; c <= centerC + 1; c++) {
                if (r >= 0 && r < individual.getRows() &&
                        c >= 0 && c < individual.getCols() &&
                        individual.getCell(r, c)) {
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