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
 * Kelas utama untuk menjalankan eksperimen otomatis pada algoritma genetik
 * Mosaic Puzzle.
 * <p>
 * Kelas ini memfasilitasi pengujian berbagai konfigurasi hyperparameter (Grid
 * Search)
 * secara paralel (multithreading) dengan jaminan hasil yang deterministik
 * (fairness).
 * </p>
 */
public class MosaicExperiment {

    private static final int TRIALS_PER_CONFIG = 1; // Jumlah pengulangan per skenario
    private static final int NUM_THREADS = 3;

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
     * 
     * @param args argumen baris perintah (opsional path file input)
     */
    public static void main(String[] args) {
        System.out.println("=== STARTING AUTOMATED EXPERIMENTS ===");
        System.out.println("CPU Cores: " + NUM_THREADS);

        // Folder input puzzle
        String inputFolder = "mosaic/testcase/10x10/";

        // Folder output hasil
        String outputFolder = "mosaic/experiment_results/10x10/";
        new File(outputFolder).mkdirs();

        List<File> puzzleFiles;

        try {
            puzzleFiles = loadPuzzleFiles(inputFolder);
        } catch (IOException e) {
            System.err.println("Failed to load puzzle folder: " + e.getMessage());
            return;
        }

        System.out.println("Loaded " + puzzleFiles.size() + " puzzle files.");

        // Jalankan eksperimen
        try {
            runComparisonExperiment(puzzleFiles, outputFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nAll Experiments Finished.");
    }

    /**
     * Menjalankan skenario perbandingan beberapa konfigurasi.
     */
    private static void runComparisonExperiment(List<File> puzzleFiles, String outputFolder) throws IOException {

        System.out.println("\nRunning Experiment: Strategy Comparison...");

        String csvFilename = outputFolder + "10x10_roulette_pool200_exp.csv";

        List<ExperimentConfig> configs = new ArrayList<>();

        // Definisikan Skenario
        // Config: Name, Pop, MaxGen, CrossRate, MutRate, Elite, MutStrat, CrossStrat,
        // SelectStrat
        // MUTATION FACTORY OPTIONS : "basic", "constraint", "adaptive", "hybrid",
        // "random"
        // CROSSOVER FACTORY OPTIONS : "uniform", "onepoint", "twopoint", "singleblock",
        // "multiblock"
        // SELECTION FACTORY OPTIONS : "tournament", "routlette", "stochastic", "rank",
        // "truncation"
        // Opsi Factory
        // Hyperparameter search space
        int[] populationSizes = { 200 };
        int[] generations = { 1000 };
        double[] crossoverRates = { 0.85 };
        double[] mutationRates = { 0.001 };
        int[] eliteCounts = { 5 };

        String[] mutations = { "constraint" };
        String[] crossovers = { "twopoint" };
        String[] selections = { "roulette" };

        // Grid Search
        for (int pop : populationSizes) {
            for (int gen : generations) {
                for (double mutRate : mutationRates) {
                    for (double crossRate : crossoverRates) {
                        for (int elite : eliteCounts) {

                            for (String mut : mutations) {
                                for (String cross : crossovers) {
                                    for (String sel : selections) {

                                        String name = String.format(
                                                "POP%d_GEN%d_MUT%.4f_CROSS%.3f_ELITE%d_M%s_C%s_S%s",
                                                pop, gen, mutRate, crossRate, elite,
                                                mut, cross, sel);

                                        configs.add(new ExperimentConfig(
                                                name,
                                                pop,
                                                gen,
                                                crossRate,
                                                mutRate,
                                                elite,
                                                mut,
                                                cross,
                                                sel));
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        runScenario(csvFilename, puzzleFiles, configs);
    }

    private static List<File> loadPuzzleFiles(String folderPath) throws IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            throw new IOException("Folder kosong atau tidak ditemukan!");
        }

        // Biar urutan konsisten
        Arrays.sort(files, Comparator.comparing(File::getName));

        return Arrays.asList(files);
    }

    /**
     * Menjalankan satu set skenario eksperimen dan menyimpan hasilnya ke CSV.
     */
    private static void runScenario(String csvFilename,
            List<File> puzzleFiles,
            List<ExperimentConfig> configs) throws IOException {

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {

            // Header CSV
            writer.println("ConfigName,AvgFitness,AvgTimeMs,AvgGenerations,SuccessRate");

            for (ExperimentConfig cfg : configs) {
                System.out.print("  Testing " + cfg.name + " ");

                List<Future<RunResult>> futures = new ArrayList<>();

                for (int i = 0; i < TRIALS_PER_CONFIG; i++) {
                    long seed = TRIAL_SEEDS.get(i);

                    Callable<RunResult> task = () -> {

                        double totalFitness = 0;
                        long totalTime = 0;
                        int totalGen = 0;
                        int success = 0;

                        for (File puzzleFile : puzzleFiles) {
                            Puzzle puzzle = parsePuzzleFromFile(puzzleFile.getPath());
                            HeuristicSolver.applyHeuristics(puzzle, -1);

                            RunResult r = runSingleTrial(puzzle, cfg, seed);

                            totalFitness += r.bestFitness;
                            totalTime += r.timeMs;
                            totalGen += r.generations;

                            if (r.bestFitness >= 1.0)
                                success++;
                        }

                        int n = puzzleFiles.size();
                        return new RunResult(
                                totalFitness / n,
                                totalTime / n,
                                totalGen / n,
                                success / (double) n);
                    };

                    futures.add(executor.submit(task));
                }

                double totalFitness = 0;
                double totalTime = 0;
                double totalGen = 0;
                double totalSuccess = 0;

                for (Future<RunResult> f : futures) {
                    try {
                        RunResult r = f.get();
                        totalFitness += r.bestFitness;
                        totalTime += r.timeMs;
                        totalGen += r.generations;
                        totalSuccess += r.successRate;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                double avgFit = totalFitness / TRIALS_PER_CONFIG;
                double avgTime = totalTime / TRIALS_PER_CONFIG;
                double avgGen = totalGen / TRIALS_PER_CONFIG;
                double successRate = totalSuccess / TRIALS_PER_CONFIG;

                writer.printf("%s,%.4f,%.2f,%.2f,%.2f%n",
                        cfg.name, avgFit, avgTime, avgGen, successRate);
                writer.flush();

                System.out.println(" Done.");
            }

        } finally {
            executor.shutdown();
        }

        System.out.println("Results saved to " + csvFilename);
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
        selParams.put("k", 2); // Tournament k
        selParams.put("pressure", 1.0); // selective pressure
        selParams.put("portion", 20); // portion %

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
            if (line == null)
                break;
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
        double successRate;

        public RunResult(double f, long t, int g) {
            this.bestFitness = f;
            this.timeMs = t;
            this.generations = g;
        }

        public RunResult(double f, long t, int g, double s) {
            this.bestFitness = f;
            this.timeMs = t;
            this.generations = g;
            this.successRate = s;
        }
    }

}