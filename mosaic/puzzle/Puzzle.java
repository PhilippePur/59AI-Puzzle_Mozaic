package mosaic.puzzle;

import java.util.List;
import java.util.ArrayList;

public class Puzzle {
    private List<Clue> clues = new ArrayList<>();
    private int rows;
    private int cols;
    private boolean[][] fixedCells; 
    
    public Puzzle(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.fixedCells = new boolean[rows][cols]; // Default semua false (bisa diubah)
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
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
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
    
    // UTILITY METHODS
    public int getTotalClues() {
        return clues.size();
    }
    
    public double getClueDensity() {
        if (rows * cols == 0) return 0.0;
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
}
