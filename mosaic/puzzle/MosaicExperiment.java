package mosaic.puzzle;

import mosaic.genetic.GeneticAlgorithm;
import mosaic.genetic.crossover.CrossoverStrategy;
import mosaic.genetic.crossover.CrossoverStrategyFactory;
import mosaic.genetic.mutation.MutationStrategy;
import mosaic.genetic.mutation.MutationStrategyFactory;
import mosaic.genetic.selection.SelectionStrategy;
import mosaic.genetic.selection.SelectionStrategyFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Kelas utama untuk menjalankan eksperimen otomatis pada algoritma genetik Mosaic Puzzle.
 * <p>
 * Kelas ini memfasilitasi pengujian berbagai konfigurasi hyperparameter (Grid Search)
 * secara paralel (multithreading) dengan jaminan hasil yang deterministik (fairness).
 * </p>
 */
public class MosaicExperiment {

    private static final int TRIALS_PER_CONFIG = 30; // Jumlah pengulangan per skenario
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    
    // Seed tetap untuk setiap trial agar adil antar konfigurasi
    private static final List<Long> TRIAL_SEEDS = new ArrayList<>();

    static {
        Random r = new Random(5555L); // Meta-seed
        for (int i = 0; i < TRIALS_PER_CONFIG; i++) {
            TRIAL_SEEDS.add(r.nextLong());
        }
    }

    /**
     * Entry point eksperimen.
     * @param args argumen baris perintah (opsional path file input)
     */
    public static void main(String[] args) {
        System.out.println("=== STARTING AUTOMATED EXPERIMENTS ===");
        System.out.println("CPU Cores: " + NUM_THREADS);

        Puzzle puzzle = null;
        File file = new File("experiment_input.txt");
        
        if (file.exists()) {
            System.out.println("Loading puzzle from experiment_input.txt...");
            try {
                puzzle = parsePuzzleFromFile("experiment_input.txt");
            } catch (IOException e) {
                System.err.println("Failed to load puzzle file: " + e.getMessage());
                return;
            }
        } else {
            System.err.println("File experiment_input.txt not found!");
            return;
        }

        System.out.printf("Puzzle loaded: %dx%d with %d clues.\n",
                puzzle.getRows(), puzzle.getCols(), puzzle.getClues().size());

        // 1. Apply Heuristics (Pre-processing)
        System.out.println("\nApplying Heuristics to Puzzle for GA Experiments...");
        // Menggunakan limit -1 (tanpa batas iterasi)
        int fixedCount = HeuristicSolver.applyHeuristics(puzzle, -1);
        System.out.println("Heuristics applied. Fixed cells: " + fixedCount);

        // 2. Run GA Experiments
        try {
            // Contoh pemanggilan eksperimen (Anda bisa uncomment sesuai kebutuhan)
            runComparisonExperiment(puzzle);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nAll Experiments Finished.");
    }

    /**
     * Menjalankan skenario perbandingan beberapa konfigurasi.
     */
    private static void runComparisonExperiment(Puzzle puzzle) throws IOException {
        System.out.println("\nRunning Experiment: Strategy Comparison...");
        String filename = "exp_strategy_comparison.csv";

        List<ExperimentConfig> configs = new ArrayList<>();
        
        // Definisikan Skenario
        // Config: Name, Pop, MaxGen, CrossRate, MutRate, Elite, MutStrat, CrossStrat, SelectStrat
        
        configs.add(new ExperimentConfig("Baseline", 100, 500, 0.8, 0.05, 2, "basic", "uniform", "tournament"));
        configs.add(new ExperimentConfig("Adaptive-MultiBlock", 100, 500, 0.8, 0.0, 2, "adaptive", "multiblock", "tournament"));
        configs.add(new ExperimentConfig("Constraint-Rank", 100, 500, 0.8, 0.0, 2, "constraint", "uniform", "rank"));
        configs.add(new ExperimentConfig("HighPop-Roulette", 300, 500, 0.8, 0.05, 5, "basic", "uniform", "roulette"));

        runScenario(filename, puzzle, configs);
    }

    /**
     * Menjalankan satu set skenario eksperimen dan menyimpan hasilnya ke CSV.
     */
    private static void runScenario(String filename, Puzzle puzzle, List<ExperimentConfig> configs) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Header CSV
            writer.println("ConfigName,AvgFitness,AvgTimeMs,AvgGenerations,SuccessRate");

            for (ExperimentConfig cfg : configs) {
                System.out.print("  Testing " + cfg.name + " ");
                
                List<Future<RunResult>> futures = new ArrayList<>();

                // Submit jobs
                for (int i = 0; i < TRIALS_PER_CONFIG; i++) {
                    long seed = TRIAL_SEEDS.get(i);
                    Callable<RunResult> task = () -> runSingleTrial(puzzle, cfg, seed);
                    futures.add(executor.submit(task));
                }

                // Collect results
                double totalFitness = 0;
                long totalTime = 0;
                int totalGens = 0;
                int successCount = 0;
                int done = 0;

                for (Future<RunResult> f : futures) {
                    try {
                        RunResult res = f.get();
                        totalFitness += res.bestFitness;
                        totalTime += res.timeMs;
                        totalGens += res.generations;
                        if (res.bestFitness >= 1.0) successCount++;
                        
                        done++;
                        if (done % 5 == 0) System.out.print(".");
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(" Done.");

                // Averages
                double avgFit = totalFitness / TRIALS_PER_CONFIG;
                double avgTime = (double) totalTime / TRIALS_PER_CONFIG;
                double avgGen = (double) totalGens / TRIALS_PER_CONFIG;
                double successRate = (double) successCount / TRIALS_PER_CONFIG;

                writer.printf("%s,%.4f,%.2f,%.2f,%.2f%n", 
                    cfg.name, avgFit, avgTime, avgGen, successRate);
                writer.flush();
            }
        } finally {
            executor.shutdown();
        }
        System.out.println("  Results saved to " + filename);
    }

    /**
     * Menjalankan satu kali percobaan (Single Trial) dengan konfigurasi tertentu.
     */
    private static RunResult runSingleTrial(Puzzle puzzle, ExperimentConfig cfg, long seed) {
        Random rng = new Random(seed);

        // 1. Setup Parameter Maps
        Map<String, Object> mutParams = new HashMap<>();
        mutParams.put("rate", cfg.mutRate);
        
        Map<String, Object> crossParams = new HashMap<>();
        crossParams.put("num_blocks", 3); // Default params for multiblock if used
        
        Map<String, Object> selParams = new HashMap<>();
        selParams.put("pool_size", cfg.popSize); // Pool size biasanya = pop size
        selParams.put("k", 5); // Tournament k

        // 2. Create Strategies via Factories
        MutationStrategy mutStrat = MutationStrategyFactory.createStrategy(cfg.mutStrat, rng, mutParams);
        CrossoverStrategy crossStrat = CrossoverStrategyFactory.createStrategy(cfg.crossStrat, crossParams);
        SelectionStrategy selStrat = SelectionStrategyFactory.createStrategy(cfg.selStrat, rng, selParams);

        // 3. Init GA
        GeneticAlgorithm ga = new GeneticAlgorithm(
            puzzle, rng, 
            cfg.popSize, cfg.maxGen, cfg.crossRate, cfg.mutRate, cfg.eliteCount,
            crossStrat, mutStrat, selStrat // Inject semua strategi
        );

        long start = System.currentTimeMillis();
        mosaic.puzzle.Individual best = ga.run();
        long end = System.currentTimeMillis();

        return new RunResult(best.getFitness(), end - start, ga.getCurrentGeneration());
    }

    // --- Helper Classes ---

    private static Puzzle parsePuzzleFromFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String[] dims = br.readLine().trim().split("\\s+");
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);
        Puzzle puzzle = new Puzzle(rows, cols);

        for (int r = 0; r < rows; r++) {
            String line = br.readLine();
            if (line == null) break;
            String[] tokens = line.trim().split("\\s+");
            for (int c = 0; c < cols; c++) {
                String valStr = tokens[c];
                if (!valStr.equals(".") && !valStr.equals("-1")) {
                    puzzle.addClue(new Clue(r, c, Integer.parseInt(valStr)));
                }
            }
        }
        br.close();
        return puzzle;
    }

    static class ExperimentConfig {
        String name;
        int popSize, maxGen, eliteCount;
        double crossRate, mutRate;
        String mutStrat, crossStrat, selStrat;

        public ExperimentConfig(String name, int pop, int gen, double cross, double mut, int elite, 
                                String mutS, String crossS, String selS) {
            this.name = name;
            this.popSize = pop;
            this.maxGen = gen;
            this.crossRate = cross;
            this.mutRate = mut;
            this.eliteCount = elite;
            this.mutStrat = mutS;
            this.crossStrat = crossS;
            this.selStrat = selS;
        }
    }

    static class RunResult {
        double bestFitness;
        long timeMs;
        int generations;

        public RunResult(double f, long t, int g) {
            this.bestFitness = f;
            this.timeMs = t;
            this.generations = g;
        }
    }
}