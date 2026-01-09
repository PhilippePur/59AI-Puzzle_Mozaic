package mosaic.puzzle;


public class Individual {
    private boolean[][] grid;
    private boolean[][] isFixed;
    private int rows;
    private int cols;
    private Puzzle puzzle;
    private int fitness;
    private boolean fitnessDirty; // Flag untuk optimize recalc
    
    public Individual(Puzzle puzzle, boolean[][] fixedPositions) {
        this.puzzle = puzzle;
        this.rows = puzzle.getRows(); 
        this.cols = puzzle.getCols(); 
        this.grid = new boolean[rows][cols];
        this.isFixed = new boolean[rows][cols];
        this.fitness = -1;
        this.fitnessDirty = true;
        
        // Initialize fixed positions
        if (fixedPositions != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    isFixed[r][c] = fixedPositions[r][c];
                    if (isFixed[r][c]) {
                        // Jika fixed, set ke nilai tertentu atau random
                        grid[r][c] = false; // Default false (white)
                    } else {
                        // Random initialization untuk non-fixed
                        grid[r][c] = Math.random() > 0.5;
                    }
                }
            }
        }
    }
    
    // GETTERS
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public boolean getCell(int r, int c) { return grid[r][c]; }
    public boolean isFixed(int r, int c) { return isFixed[r][c]; }
    
    public int getFitness() {
        if (fitnessDirty) {
            calculateFitness();
        }
        return fitness;
    }
    
    public boolean[][] getGrid() {
        // Return defensive copy untuk safety
        boolean[][] copy = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }
    
    // SETTERS/MODIFIERS
    public void setCell(int r, int c, boolean value) { 
        if (!isFixed[r][c]) {
            grid[r][c] = value;
            markDirty();
        }
    }
    
    public void flipCell(int r, int c) { 
        if (!isFixed[r][c]) {
            grid[r][c] = !grid[r][c];
            markDirty();
        }
    }
    
    public void markDirty() {
        this.fitnessDirty = true;
    }
    
    public void calculateFitness() {
       
    }
    
    private int countBlackIn3x3Area(int centerR, int centerC) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = centerR + dr;
                int c = centerC + dc;
                if (r >= 0 && r < rows && c >= 0 && c < cols && grid[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }
    
    // COPY METHOD
    public Individual copy() {
        Individual copy = new Individual(this.puzzle, null);
        copy.rows = this.rows;
        copy.cols = this.cols;
        
        copy.grid = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.grid[i], 0, copy.grid[i], 0, cols);
        }
        
        copy.isFixed = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.isFixed[i], 0, copy.isFixed[i], 0, cols);
        }
        
        copy.fitness = this.fitness;
        copy.fitnessDirty = this.fitnessDirty;
        return copy;
    }
    

}