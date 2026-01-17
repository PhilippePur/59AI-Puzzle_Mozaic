package mosaic.puzzle;

import mosaic.genetic.GeneticAlgorithm;
import mosaic.genetic.crossover.CrossoverStrategy;
import mosaic.genetic.crossover.UniformCrossover;
import mosaic.genetic.mutation.MutationStrategy;
import mosaic.genetic.mutation.MutationStrategyFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Runner untuk menjalankan eksperimen batch/tuning secara paralel.
 * <p>
 * Kelas ini memfasilitasi pengujian berbagai konfigurasi hyperparameter secara bersamaan
 * (multithreading) sambil menjamin keadilan eksperimen (fairness) dengan menggunakan
 * seed generator acak yang identik untuk setiap skenario.
 */
public class MosaicExperiment {

    private static final String PUZZLE_FILE = "puzzle_input.txt"; 
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * Entry point eksperimen.
     * Memuat data, mendefinisikan skenario, dan mendistribusikan tugas ke thread pool.
     */
    public static void main(String[] args) {
        System.out.println("=== MOSAIC EXPERIMENT RUNNER (DETERMINISTIC) ===");
        System.out.println("CPU Cores: " + NUM_THREADS);
        System.out.println("Target File: " + PUZZLE_FILE);

        try {
            // 1. Load Puzzle & Base Seed (Satu kali baca)
            ExperimentData data = loadExperimentData(PUZZLE_FILE);
            Puzzle basePuzzle = data.puzzle;
            long baseSeed = data.seed; 
            
            System.out.println("Base Seed loaded: " + baseSeed);

            // Terapkan heuristik sekali di awal pada objek puzzle bersama
            System.out.println("Applying Heuristics Pre-processing...");
            int fixed = HeuristicSolver.applyHeuristics(basePuzzle, -1);
            System.out.printf("Heuristics Fixed: %d cells.\n\n", fixed);

            // 2. Definisi Skenario Eksperimen (Grid Search)
            List<ExperimentConfig> scenarios = new ArrayList<>();
            scenarios.add(new ExperimentConfig("Scenario A (Basic)", 200, 1000, 0.9, 4, "basic", 0.05));
            scenarios.add(new ExperimentConfig("Scenario B (Constraint)", 200, 1000, 0.9, 4, "constraint", 0.05));
            scenarios.add(new ExperimentConfig("Scenario C (Adaptive)", 200, 1000, 0.9, 4, "adaptive", 0.0));
            scenarios.add(new ExperimentConfig("Scenario D (High Pop)", 500, 1000, 0.9, 10, "basic", 0.05));
            scenarios.add(new ExperimentConfig("Scenario E (Low Cross)", 200, 1000, 0.6, 4, "basic", 0.05));

            // 3. Eksekusi Paralel
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            List<Future<ExperimentResult>> futures = new ArrayList<>();
            long startTime = System.currentTimeMillis();

            System.out.println("Running " + scenarios.size() + " scenarios...");
            System.out.println("NOTE: Semua skenario menggunakan Seed yang SAMA (" + baseSeed + ") untuk fairness.");
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.printf("%-25s | %-10s | %-10s | %-10s | %-15s\n", 
                    "Scenario Name", "Time (ms)", "Gens", "Fitness", "Status");
            System.out.println("-----------------------------------------------------------------------------------------");

            for (ExperimentConfig config : scenarios) {
                // Mengirim seed yang sama ke setiap worker thread
                Callable<ExperimentResult> task = () -> runSingleExperiment(basePuzzle, config, baseSeed);
                futures.add(executor.submit(task));
            }

            // 4. Agregasi Hasil
            for (Future<ExperimentResult> future : futures) {
                try {
                    ExperimentResult res = future.get(); 
                    printResultRow(res);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Experiment Failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.println("Total Wall-Clock Time: " + totalTime + " ms");
            
            executor.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Menjalankan satu eksperimen secara terisolasi.
     * Menggunakan instance Random baru dengan seed yang dipaksa sama untuk deterministik.
     *
     * @param sharedPuzzle referensi objek puzzle (thread-safe untuk baca)
     * @param config konfigurasi hyperparameter untuk run ini
     * @param seed nilai seed acak yang harus digunakan
     * @return hasil eksekusi eksperimen
     */
    private static ExperimentResult runSingleExperiment(Puzzle sharedPuzzle, ExperimentConfig config, long seed) {
        long start = System.currentTimeMillis();
        
        // Membuat Random lokal dengan seed yang ditentukan (Deterministik antar thread)
        Random threadRng = new Random(seed);

        Map<String, Object> mParams = new HashMap<>();
        mParams.put("rate", config.mutationRate);
        
        MutationStrategy strategy = MutationStrategyFactory.createStrategy(
                config.mutationType, threadRng, mParams);

        CrossoverStrategy crossStrategy = new UniformCrossover();

        GeneticAlgorithm ga = new GeneticAlgorithm(
                config.popSize,
                config.maxGen,
                config.crossRate,
                config.eliteCount,
                strategy,
                crossStrategy,
                sharedPuzzle,
                threadRng 
        );

        mosaic.puzzle.Individual solution = ga.run();
        
        long end = System.currentTimeMillis();
        boolean isSolved = (solution != null && Math.abs(solution.getFitness() - 1.0) < 0.000001);
        
        return new ExperimentResult(
                config.name, 
                (end - start), 
                ga.getCurrentGeneration(), 
                ga.getBestFitness(), 
                isSolved
        );
    }

    private static void printResultRow(ExperimentResult res) {
        System.out.printf("%-25s | %-10d | %-10d | %-10.5f | %-15s\n", 
                res.name, res.durationMs, res.generations, res.fitness, 
                (res.solved ? "SOLVED" : "Not Solved"));
    }

    /**
     * Membaca struktur Grid Puzzle dan Seed dari file input.
     * Mengabaikan parameter lain karena parameter eksperimen ditentukan di kode.
     */
    private static ExperimentData loadExperimentData(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        
        String[] dims = br.readLine().trim().split("\\s+");
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);
        Puzzle puzzle = new Puzzle(rows, cols);

        String[] seedLine = br.readLine().trim().split("\\s+");
        long seed = Long.parseLong(seedLine[0]);

        br.readLine(); // Skip parameter baris ke-3

        String line;
        for (int r = 0; r < rows; r++) {
            line = br.readLine();
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
        return new ExperimentData(puzzle, seed);
    }

    // --- Data Transfer Objects (DTO) ---

    static class ExperimentData {
        Puzzle puzzle;
        long seed;
        public ExperimentData(Puzzle p, long s) { this.puzzle = p; this.seed = s; }
    }

    static class ExperimentConfig {
        String name;
        int popSize, maxGen, eliteCount;
        double crossRate, mutationRate;
        String mutationType;

        public ExperimentConfig(String name, int popSize, int maxGen, double crossRate, 
                                int eliteCount, String mutationType, double mutationRate) {
            this.name = name;
            this.popSize = popSize;
            this.maxGen = maxGen;
            this.crossRate = crossRate;
            this.eliteCount = eliteCount;
            this.mutationType = mutationType;
            this.mutationRate = mutationRate;
        }
    }

    static class ExperimentResult {
        String name;
        long durationMs;
        int generations;
        double fitness;
        boolean solved;

        public ExperimentResult(String name, long durationMs, int generations, double fitness, boolean solved) {
            this.name = name;
            this.durationMs = durationMs;
            this.generations = generations;
            this.fitness = fitness;
            this.solved = solved;
        }
    }
}