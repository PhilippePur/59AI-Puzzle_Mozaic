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
 * 
 * </p>
 * @author kelas ini digenerate oleh LLM Gemini 3 Pro
 */
public class MosaicExperiment {

    private static final int TRIALS_PER_CONFIG = 30; // Jumlah pengulangan per skenario (Statistik)
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    
    // Seed tetap untuk setiap trial agar adil antar konfigurasi
    private static final List<Long> TRIAL_SEEDS = new ArrayList<>();

    static {
        Random r = new Random(5555L); // Meta-seed
        for (int i = 0; i < TRIALS_PER_CONFIG; i++) {
            TRIAL_SEEDS.add(r.nextLong());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== MOSAIC EXPERIMENT: STAGE 2 (POPULATION SCALING) ===");
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
        System.out.println("\nApplying Heuristics to Puzzle...");
        int fixedCount = HeuristicSolver.applyHeuristics(puzzle, -1);
        System.out.println("Heuristics applied. Fixed cells: " + fixedCount);

        // 2. Run Stage 2 Experiments
        try {
            runStage2PopulationExperiment(puzzle);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nAll Experiments Finished.");
    }

    /**
     * Menjalankan Eksperimen Tahap 2.
     * Menguji 5 Strategi Terbaik dengan variasi Populasi: 100, 500, 1000, 5000.
     */
    private static void runStage2PopulationExperiment(Puzzle puzzle) throws IOException {
        System.out.println("\nRunning Stage 3: Hyperparameter Tuning on Top 5 Strategies...");
        String filename = "exp_stage3_hyperparameters.csv";
        List<ExperimentConfig> configs = new ArrayList<>();

        // --- DEFINISI RUANG PENCARIAN (GRID SEARCH) ---
        int[] populationSizes = { 50, 100, 200 };
        int[] maxGenerationsList = { 200, 500, 1000 };
        double[] mutationRates = { 0.001, 0.005, 0.01, 0.05 };
        double[] crossoverRates = { 0.6, 0.8, 0.9 };
        int[] eliteCounts = { 2, 5 };

        // --- DAFTAR 5 STRATEGI TERBAIK (Fix) ---
        // Format: {Name, Mut, Cross, Sel}
        String[][] topStrategies = {
            {"SingleBlock_Tourn", "constraint", "singleblock", "tournament"}, // Top 1 (Eksploratif)
            {"TwoPoint_Tourn", "constraint", "twopoint", "tournament"},       // Top 3 (Eksploratif)
            {"TwoPoint_Trunc", "constraint", "twopoint", "truncation"},       // Top 6 (Eksploratif)
            {"OnePoint_Tourn", "constraint", "onepoint", "tournament"},       // Top 2 (Eksploitatif)
            {"MultiBlock_Tourn", "constraint", "multiblock", "tournament"}    // Top 5 (Eksploitatif)
        };

        // --- GENERATE KOMBINASI ---
        for (String[] strat : topStrategies) {
            String stratName = strat[0];
            String mutType = strat[1];
            String crossType = strat[2];
            String selType = strat[3];

            for (int pop : populationSizes) {
                for (int maxGen : maxGenerationsList) {
                    for (double mutRate : mutationRates) {
                        for (double crossRate : crossoverRates) {
                            for (int elite : eliteCounts) {
                                
                                // Format Nama Config: StratName_P[Pop]_G[Gen]_M[Mut]_C[Cross]_E[Elite]
                                String configName = String.format("%s_P%d_G%d_M%.3f_C%.1f_E%d", 
                                        stratName, pop, maxGen, mutRate, crossRate, elite);

                                configs.add(new ExperimentConfig(configName, 
                                        pop, maxGen, crossRate, mutRate, elite, 
                                        mutType, crossType, selType));
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Total Configurations to Run: " + configs.size());
        System.out.println("Total Trials: " + (configs.size() * TRIALS_PER_CONFIG));
        System.out.println("Estimasi waktu: Sangat Lama. Silakan AFK.");
        
        // Jalankan Skenario
        runScenario(filename, puzzle, configs);
    }

    /**
     * Menjalankan satu set skenario eksperimen dan menyimpan hasilnya ke CSV.
     * Juga menampilkan Top Hasil di akhir.
     */
    private static void runScenario(String filename, Puzzle puzzle, List<ExperimentConfig> configs) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<ConfigSummary> summaries = new ArrayList<>();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Header CSV Updated: Menambahkan kolom detail hyperparameter
            writer.println("ConfigName,PopSize,MaxGen,CrossRate,MutRate,EliteCount,AvgFitness,AvgTimeMs,AvgGenerations,SuccessRate");

            int count = 1;
            for (ExperimentConfig cfg : configs) {
                System.out.printf("[%d/%d] Testing %-35s ", count++, configs.size(), cfg.name);
                
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // Calculate Averages
                double avgFit = totalFitness / TRIALS_PER_CONFIG;
                double avgTime = (double) totalTime / TRIALS_PER_CONFIG;
                double avgGen = (double) totalGens / TRIALS_PER_CONFIG;
                double successRate = (double) successCount / TRIALS_PER_CONFIG;

                System.out.printf("-> Success: %3.0f%% | Fit: %.5f | Time: %5.0f ms%n", 
                        successRate * 100, avgFit, avgTime);

                // Write to CSV Updated: Mencetak nilai hyperparameter ke kolom terpisah
                writer.printf("%s,%d,%d,%.2f,%.2f,%d,%.5f,%.2f,%.2f,%.2f%n", 
                    cfg.name, 
                    cfg.popSize, cfg.maxGen, cfg.crossRate, cfg.mutRate, cfg.eliteCount,
                    avgFit, avgTime, avgGen, successRate);
                writer.flush();

                summaries.add(new ConfigSummary(cfg.name, avgFit, avgTime, successRate));
            }
        } finally {
            executor.shutdown();
        }
        System.out.println("  Results saved to " + filename);

        printTopPerformers(summaries);
    }

    /**
     * Menjalankan satu kali percobaan (Single Trial).
     */
    private static RunResult runSingleTrial(Puzzle puzzle, ExperimentConfig cfg, long seed) {
        Random rng = new Random(seed);

        Map<String, Object> mutParams = new HashMap<>();
        mutParams.put("rate", cfg.mutRate);
        
        Map<String, Object> crossParams = new HashMap<>();
        crossParams.put("num_blocks", 3); 
        crossParams.put("block_size", 3);
        
        Map<String, Object> selParams = new HashMap<>();
        selParams.put("pool_size", cfg.popSize); 
        selParams.put("k", 5); 
        selParams.put("portion", 50); 
        selParams.put("pressure", 1.5); 

        MutationStrategy mutStrat = MutationStrategyFactory.createStrategy(cfg.mutStrat, rng, mutParams);
        CrossoverStrategy crossStrat = CrossoverStrategyFactory.createStrategy(cfg.crossStrat, crossParams);
        SelectionStrategy selStrat = SelectionStrategyFactory.createStrategy(cfg.selStrat, rng, selParams);

        GeneticAlgorithm ga = new GeneticAlgorithm(
            puzzle, rng, 
            cfg.popSize, cfg.maxGen, cfg.crossRate, cfg.mutRate, cfg.eliteCount,
            crossStrat, mutStrat, selStrat
        );

        long start = System.currentTimeMillis();
        mosaic.puzzle.Individual best = ga.run();
        long end = System.currentTimeMillis();

        return new RunResult(best.getFitness(), end - start, ga.getCurrentGeneration());
    }

    private static void printTopPerformers(List<ConfigSummary> summaries) {
        // Sort: Success Rate (Desc) -> Avg Fitness (Desc) -> Time (Asc)
        summaries.sort((a, b) -> {
            int cmpSuccess = Double.compare(b.successRate, a.successRate);
            if (cmpSuccess != 0) return cmpSuccess;
            int cmpFit = Double.compare(b.avgFitness, a.avgFitness);
            if (cmpFit != 0) return cmpFit;
            return Double.compare(a.avgTime, b.avgTime);
        });

        System.out.println("\n===== TOP 5 CONFIGURATIONS (STAGE 2) =====");
        System.out.printf("%-40s | %-10s | %-10s | %-10s%n", "Config", "Success", "Fitness", "Time");
        System.out.println("-----------------------------------------------------------------------------");
        
        for (int i = 0; i < Math.min(5, summaries.size()); i++) {
            ConfigSummary s = summaries.get(i);
            System.out.printf("%-40s | %-9.1f%% | %-10.5f | %-10.0f ms%n", 
                s.name, s.successRate * 100, s.avgFitness, s.avgTime);
        }
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

    static class ConfigSummary {
        String name;
        double avgFitness;
        double avgTime;
        double successRate;

        public ConfigSummary(String n, double f, double t, double s) {
            this.name = n; this.avgFitness = f; this.avgTime = t; this.successRate = s;
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