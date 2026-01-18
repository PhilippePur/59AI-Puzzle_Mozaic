package mosaic.puzzle;

import mosaic.genetic.GeneticAlgorithm;
import mosaic.genetic.crossover.CrossoverStrategy;
import mosaic.genetic.crossover.UniformCrossover;
import mosaic.genetic.mutation.MutationStrategy;
import mosaic.genetic.mutation.MutationStrategyFactory;
import mosaic.util.GlobalRandom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.*;

/**
 * Kelas utama untuk menjalankan eksperimen otomatis pada algoritma genetik
 * Mosaic Puzzle.
 * <p>
 * Kelas ini menggabungkan konfigurasi eksperimen, eksekusi, dan logging hasil
 * ke dalam satu unit.
 * </p>
 */
public class Experiment {

    private static final int TRIALS_PER_CONFIG = 30;
    private static final long PUZZLE_SEED = 999L;

    // Fixed seeds for trials to ensure fairness across configs
    private static final List<Long> TRIAL_SEEDS = new ArrayList<>();

    static {
        // Pre-generate seeds
        Random r = new Random(5555L); // Meta-seed
        for (int i = 0; i < TRIALS_PER_CONFIG; i++) {
            TRIAL_SEEDS.add(r.nextLong());
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Automated Experiments...");

        Puzzle puzzle = null;

        // Terima input dari file
        File file = new File("experiment_input.txt");
        if (file.exists()) {
            System.out.println("Loading puzzle from experiment_input.txt...");
            try {
                puzzle = parsePuzzleFromFile("experiment_input.txt");
            } catch (IOException e) {
                System.err.println("Failed to load puzzle file: " + e.getMessage());
                return;
            }
        } 

        System.out.printf("Puzzle loaded: %dx%d with %d clues.\n",
                puzzle.getRows(), puzzle.getCols(), puzzle.getClues().size());

        /** 
        // 2. Run Heuristic Analysis Experiment
        try {
            runHeuristicExperiment(puzzle);
        } catch (IOException e) {
            System.err.println("Failed to run heuristic experiment: " + e.getMessage());
        } */

        // 3. Apply Heuristics (Pre-processing for GA)
        // IMPORTANT: We must apply heuristics to the puzzle object BEFORE running GA
        // experiments
        // so that GA benefits from the reduced search space.
        System.out.println("\nApplying Heuristics to Puzzle for GA Experiments...");
        int fixedCount = HeuristicSolver.applyHeuristics(puzzle, -1);
        System.out.println("Heuristics applied. Fixed cells: " + fixedCount);

        // 4. Run GA Experiments
        try {
            runPopulationSizeExperiment(puzzle);
            runMutationRateExperiment(puzzle);
            runMutationStrategyExperiment(puzzle);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nAll Experiments Finished.");
    }

    /**
     * Menjalankan eksperimen untuk menganalisis performa Heuristic Solver.
     * Mengukur waktu eksekusi dan seberapa banyak ruang pencarian berkurang.
     */
    private static void runHeuristicExperiment(Puzzle originalPuzzle) throws IOException {
        System.out.println("\nRunning Experiment: Heuristic Analysis...");
        String filename = "exp_heuristic_analysis.csv";

        // Create a COPY of the puzzle for analysis to avoids modifying the original
        // object yet
        // (Though in valid flow we eventually want to modify it, but for measurement
        // let's be clean)
        // Since Puzzle class doesn't have deep copy, we reload or re-generate.
        // For simplicity, we will assume we can apply heuristics on the main object
        // later.
        // Here we just want to measure "from scratch".

        // We need a fresh puzzle instance specifically for this measurement
        // Re-parsing or re-generating is safest.
        Puzzle testPuzzle;
        File file = new File("experiment_input.txt");
        if (file.exists()) {
            testPuzzle = parsePuzzleFromFile("experiment_input.txt");
        } else {
            testPuzzle = generatePuzzle(originalPuzzle.getRows(), originalPuzzle.getCols(), PUZZLE_SEED);
        }

        long startTime = System.nanoTime();
        int fixedCells = HeuristicSolver.applyHeuristics(testPuzzle, -1);
        long endTime = System.nanoTime();

        double durationMs = (endTime - startTime) / 1_000_000.0;
        int totalCells = testPuzzle.getRows() * testPuzzle.getCols();
        double reductionPercent = (double) fixedCells / totalCells * 100.0;

        // Remaining search space = 2^(total - fixed)
        // We log the exponent to avoid overflow
        int searchSpaceExponent = totalCells - fixedCells;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Metric,Value");
            writer.println("PuzzleSize," + totalCells);
            writer.println("ExecutionTimeMs," + String.format("%.4f", durationMs));
            writer.println("FixedCells," + fixedCells);
            writer.println("ReductionPercent," + String.format("%.2f", reductionPercent));
            writer.println("RemainingSearchSpaceExponent," + searchSpaceExponent); // 2^x
            writer.flush();
        }

        System.out.printf("  Heuristic Analysis: Fixed %d/%d cells (%.2f%%) in %.4f ms.\n",
                fixedCells, totalCells, reductionPercent, durationMs);
        System.out.println("  Saved results to " + filename);
    }

    private static void runPopulationSizeExperiment(Puzzle puzzle) throws IOException {
        System.out.println("\nRunning Experiment: Population Size...");
        String filename = "exp_population_size.csv";

        List<ExperimentConfig> configs = new ArrayList<>();
        // Vary PopSize: 50, 100, 200, 500
        // Fixed: Gen=100, Cross=0.8, Mut=0.05, Elite=2, Strat=Basic
        int[] pops = { 50, 100, 200, 500 };
        for (int p : pops) {
            configs.add(new ExperimentConfig("PopulationSize", String.valueOf(p), p, 200, 0.8, 0.05, 2, "basic"));
        }

        runScenario(filename, puzzle, configs);
    }

    private static void runMutationRateExperiment(Puzzle puzzle) throws IOException {
        System.out.println("\nRunning Experiment: Mutation Rate...");
        String filename = "exp_mutation_rate.csv";

        List<ExperimentConfig> configs = new ArrayList<>();
        // Vary MutRate: 0.01, 0.05, 0.1, 0.2
        // Fixed: Pop=100, Gen=200, Cross=0.8, Elite=2, Strat=Basic
        double[] rates = { 0.01, 0.05, 0.1, 0.2 };
        for (double r : rates) {
            configs.add(new ExperimentConfig("MutationRate", String.valueOf(r), 100, 200, 0.8, r, 2, "basic"));
        }

        runScenario(filename, puzzle, configs);
    }

    private static void runMutationStrategyExperiment(Puzzle puzzle) throws IOException {
        System.out.println("\nRunning Experiment: Mutation Strategy...");
        String filename = "exp_mutation_strategy.csv";

        List<ExperimentConfig> configs = new ArrayList<>();
        // Vary Strategy: basic, adaptive (skipping constraint due to known bug)
        // Fixed: Pop=100, Gen=200, Cross=0.8, Mut=0.05, Elite=2
        String[] strats = { "basic", "adaptive" };
        for (String s : strats) {
            configs.add(new ExperimentConfig("MutationStrategy", s, 100, 200, 0.8, 0.05, 2, s));
        }

        runScenario(filename, puzzle, configs);
    }

    private static void runScenario(String filename, Puzzle puzzle, List<ExperimentConfig> configs) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Header
            writer.println("Parameter,Value,AvgFitness,AvgTimeMs,AvgGenerations,SuccessRate");

            for (ExperimentConfig cfg : configs) {
                double totalFitness = 0;
                long totalTime = 0;
                int totalGens = 0;
                int successCount = 0;

                System.out.print("  Testing " + cfg.getParamName() + "=" + cfg.getParamValueStr() + " ");

                // Run Trials
                for (int i = 0; i < TRIALS_PER_CONFIG; i++) {
                    long seed = TRIAL_SEEDS.get(i);
                    RunResult result = runSingleTrial(puzzle, cfg, seed);

                    totalFitness += result.getBestFitness();
                    totalTime += result.getTimeMs();
                    totalGens += result.getGenerations();
                    // Using >= just in case floating point weirdness
                    if (result.getBestFitness() >= 1.0) {
                        successCount++;
                    }
                    if (i % 5 == 0)
                        System.out.print(".");
                }
                System.out.println(" Done.");

                // Calculate Averages
                double avgFitness = totalFitness / TRIALS_PER_CONFIG;
                double avgTime = (double) totalTime / TRIALS_PER_CONFIG;
                double avgGens = (double) totalGens / TRIALS_PER_CONFIG;
                double successRate = (double) successCount / TRIALS_PER_CONFIG;

                // Write to CSV
                writer.printf("%s,%s,%.4f,%.2f,%.2f,%.2f%n",
                        cfg.getParamName(), cfg.getParamValueStr(),
                        avgFitness, avgTime, avgGens, successRate);
                writer.flush();
            }
        }
        System.out.println("  Saved results to " + filename);
    }

    private static RunResult runSingleTrial(Puzzle puzzle, ExperimentConfig cfg, long seed) {
        // Setup RNG
        Random rng = new Random(seed);

        // Setup Mutation Strategy
        Map<String, Object> mutParams = new HashMap<>();
        mutParams.put("rate", cfg.getMutRate());
        MutationStrategy mutStrategy = MutationStrategyFactory.createStrategy(cfg.getMutStrategyName(), rng, mutParams);

        // Setup Crossover Strategy (Fixed to Uniform for now)
        CrossoverStrategy crossStrategy = new UniformCrossover();

        // Init GA
        GeneticAlgorithm ga = new GeneticAlgorithm(
                puzzle, rng,
                cfg.getPopSize(), cfg.getMaxGen(), cfg.getCrossRate(), cfg.getMutRate(), cfg.getEliteCount(),
                crossStrategy, mutStrategy);

        long start = System.currentTimeMillis();
        Individual best = ga.run();
        long end = System.currentTimeMillis();

        return new RunResult(best.getFitness(), end - start, 0);
    }

    // --- Parser & Puzzle Generator ---

    private static Puzzle parsePuzzleFromFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        // Line 1: Dims
        String[] dims = parseLine(br);
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);
        Puzzle puzzle = new Puzzle(rows, cols);

        // Line 2+: Grid (Previously Lines 4+)
        for (int r = 0; r < rows; r++) {
            String[] line = parseLine(br);
            for (int c = 0; c < cols; c++) {
                String valStr = line[c];
                if (!valStr.equals(".") && !valStr.equals("-1")) {
                    int val = Integer.parseInt(valStr);
                    puzzle.addClue(new Clue(r, c, val));
                }
            }
        }
        br.close();
        return puzzle;
    }

    private static String[] parseLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;
            return line.split("\\s+");
        }
        throw new IOException("Unexpected end of file");
    }

    private static Puzzle generatePuzzle(int rows, int cols, long seed) {
        Random r = new Random(seed);
        Puzzle p = new Puzzle(rows, cols);
        boolean[][] answer = new boolean[rows][cols];

        // 1. Generate Answer Grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // ~50% fill rate usually creates balanced puzzles
                answer[i][j] = r.nextBoolean();
            }
        }

        // 2. Generate Clues based on Answer
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int count = 0;
                // Count neighbors (including self)
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = i + dr;
                        int nc = j + dc;
                        if (p.isValidPosition(nr, nc) && answer[nr][nc]) {
                            count++;
                        }
                    }
                }
                // Add clue
                p.addClue(new Clue(i, j, count));
            }
        }
        return p;
    }

    // ==========================================
    // INNER CLASSES
    // ==========================================

    /**
     * Konfigurasi untuk satu skenario eksperimen.
     */
    static class ExperimentConfig {
        private final String paramName;
        private final String paramValueStr;
        private final int popSize;
        private final int maxGen;
        private final double crossRate;
        private final double mutRate;
        private final int eliteCount;
        private final String mutStrategyName;

        public ExperimentConfig(String pName, String pVal, int pop, int gen, double cross, double mut, int elite,
                String mutStrat) {
            this.paramName = pName;
            this.paramValueStr = pVal;
            this.popSize = pop;
            this.maxGen = gen;
            this.crossRate = cross;
            this.mutRate = mut;
            this.eliteCount = elite;
            this.mutStrategyName = mutStrat;
        }

        public String getParamName() {
            return paramName;
        }

        public String getParamValueStr() {
            return paramValueStr;
        }

        public int getPopSize() {
            return popSize;
        }

        public int getMaxGen() {
            return maxGen;
        }

        public double getCrossRate() {
            return crossRate;
        }

        public double getMutRate() {
            return mutRate;
        }

        public int getEliteCount() {
            return eliteCount;
        }

        public String getMutStrategyName() {
            return mutStrategyName;
        }
    }

    /**
     * Menyimpan hasil eksekusi dari satu kali percobaan (trial).
     */
    static class RunResult {
        private final double bestFitness;
        private final long timeMs;
        private final int generations;

        public RunResult(double f, long t, int g) {
            this.bestFitness = f;
            this.timeMs = t;
            this.generations = g;
        }

        public double getBestFitness() {
            return bestFitness;
        }

        public long getTimeMs() {
            return timeMs;
        }

        public int getGenerations() {
            return generations;
        }
    }
}