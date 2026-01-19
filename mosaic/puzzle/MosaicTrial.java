package mosaic.puzzle;

import mosaic.genetic.GeneticAlgorithm;
import mosaic.genetic.crossover.*;
import mosaic.genetic.mutation.*;
import mosaic.genetic.selection.*; // Added Selection Strategy
import mosaic.util.GlobalRandom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Kelas utama (Main Class) untuk eksekusi tunggal Solver Mosaic Puzzle.
 * <p>
 * Kelas ini bertanggung jawab untuk:
 * <ol>
 * <li>Membaca file input yang berisi definisi puzzle.</li>
 * <li>Menginisialisasi objek {@link Puzzle} dan mengisi Clue.</li>
 * <li>Menjalankan {@link HeuristicSolver} untuk deduksi awal
 * (pre-processing).</li>
 * <li>Mengonfigurasi dan menjalankan {@link GeneticAlgorithm}.</li>
 * <li>Menampilkan hasil akhir ke konsol.</li>
 * </ol>
 * </p>
 */
public class MosaicTrial {

    public static void main(String[] args) throws Exception {

        String folderPath = "mosaic/testcase/10x10";
        String outputFolder = "mosaic/exp_trial/10x10/";
        String summaryPath = outputFolder + "/summary.txt";

        new File(outputFolder).mkdirs();

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.err.println("Folder kosong atau tidak ditemukan.");
            return;
        }

        // =======================
        // GRID SEARCH PARAMETERS
        // =======================

        int[] populationSizes = { 50, 100, 200 };
        int[] maxGenerationsList = { 1000 };
        double[] mutationRates = { 0.001 };
        double[] crossoverRates = { 0.85 };
        int[] eliteCounts = { 5 };

        String[] mutationTypes = { "constraint" };
        String[] crossoverTypes = { "twopoint" };
        String[] selectionTypes = { "tournament" };

        // =======================

        int bestSolvedCount = -1;
        String bestSolvedConfig = "";
        double bestSolvedAvgFitness = 0;

        double bestAvgFitness = -1;
        String bestFitnessConfig = "";
        int bestFitnessSolvedCount = 0;

        int configCount = 0;

        for (int popSize : populationSizes) {
            for (int maxGen : maxGenerationsList) {
                for (double mutRate : mutationRates) {
                    for (double crossRate : crossoverRates) {
                        for (int elite : eliteCounts) {
                            for (String mutType : mutationTypes) {
                                for (String crossType : crossoverTypes) {
                                    for (String selType : selectionTypes) {
                                        GlobalRandom.rdm.setSeed(12345);

                                        configCount++;

                                        String configName = String.format(
                                                "POP%d_GEN%d_MUT%.3f_CROSS%.2f_ELITE%d_M%s_C%s_S%s",
                                                popSize, maxGen, mutRate, crossRate, elite,
                                                mutType, crossType, selType);

                                        String outputPath = outputFolder + configName + ".txt";

                                        System.out.println("\n======================================");
                                        System.out.println("Running Config: " + configName);
                                        System.out.println("======================================");

                                        long seed = 12345L;

                                        GlobalRandom.rdm.setSeed(seed);

                                        double[] result = runConfig(files, outputPath,
                                                popSize, maxGen, mutRate, crossRate, elite,
                                                mutType, crossType, selType);

                                        int solvedCount = (int) result[0];
                                        double avgFitness = result[1];

                                        // Best solved
                                        if (solvedCount > bestSolvedCount) {
                                            bestSolvedCount = solvedCount;
                                            bestSolvedConfig = configName;
                                            bestSolvedAvgFitness = avgFitness;
                                        }

                                        // Best avg fitness
                                        if (avgFitness > bestAvgFitness) {
                                            bestAvgFitness = avgFitness;
                                            bestFitnessConfig = configName;
                                            bestFitnessSolvedCount = solvedCount;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("\nTOTAL CONFIG TESTED: " + configCount);

        System.out.println("\n======================================");
        System.out.println("GRID SEARCH SUMMARY");
        System.out.println("======================================");

        System.out.println("\nBEST BY SOLVED COUNT:");
        System.out.println("Config : " + bestSolvedConfig);
        System.out.println("Solved : " + bestSolvedCount);
        System.out.println("AvgFit : " + bestSolvedAvgFitness);

        System.out.println("\nBEST BY AVERAGE FITNESS:");
        System.out.println("Config : " + bestFitnessConfig);
        System.out.println("Solved : " + bestFitnessSolvedCount);
        System.out.println("AvgFit : " + bestAvgFitness);

        try (PrintWriter summaryWriter = new PrintWriter(new FileWriter(summaryPath))) {

            summaryWriter.println("======================================");
            summaryWriter.println("GRID SEARCH SUMMARY");
            summaryWriter.println("======================================");

            summaryWriter.println();
            summaryWriter.println("BEST BY SOLVED COUNT:");
            summaryWriter.println("Config : " + bestSolvedConfig);
            summaryWriter.println("Solved : " + bestSolvedCount);
            summaryWriter.println("AvgFit : " + bestSolvedAvgFitness);

            summaryWriter.println();
            summaryWriter.println("BEST BY AVERAGE FITNESS:");
            summaryWriter.println("Config : " + bestFitnessConfig);
            summaryWriter.println("Solved : " + bestFitnessSolvedCount);
            summaryWriter.println("AvgFit : " + bestAvgFitness);
        }

    }

    private static double[] runConfig(File[] files, String outputPath,
            int populationSize,
            int maxGenerations,
            double mutationRate,
            double crossoverRate,
            int eliteCount,
            String mutationType,
            String crossoverType,
            String selectionType) throws Exception {

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {

            double totalFitnessAll = 0;
            int totalTestcase = 0;

            int solvedCount = 0;

            for (File file : files) {
                String filename = file.getPath();
                System.out.println("  Running test case: " + file.getName());

                long startTime = System.currentTimeMillis();

                Individual best = runSolver(filename,
                        populationSize, maxGenerations,
                        mutationRate, crossoverRate, eliteCount,
                        mutationType, crossoverType, selectionType);

                long endTime = System.currentTimeMillis();

                double fitness = best.getFitness();
                totalFitnessAll += fitness;
                totalTestcase++;

                if (fitness == 1.0) {
                    solvedCount++;
                }

                writer.println("===== TEST CASE: " + file.getName() + " =====");
                writer.println("Execution time: " + (endTime - startTime) + " ms");
                writer.println("Best Fitness: " + fitness);
                writer.println(best.toString());
                writer.println();
            }

            double avgFitness = totalFitnessAll / totalTestcase;

            writer.println("==================================");
            writer.println("TOTAL TEST CASE: " + totalTestcase);
            writer.println("AVERAGE FITNESS: " + avgFitness);
            writer.println("==================================");

            System.out.println("  Average Fitness: " + avgFitness);

            return new double[] { solvedCount, avgFitness };
        }
    }

    public static Individual runSolver(
            String filename,
            int populationSize,
            int maxGenerations,
            double mutationRate,
            double crossoverRate,
            int eliteCount,
            String mutationType,
            String crossoverType,
            String selectionType) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String[] dims = parseLine(br);
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);

        Puzzle puzzle = new Puzzle(rows, cols);

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

        // heuristic
        HeuristicSolver.applyHeuristics(puzzle, -1);

        Map<String, Object> mutationParams = new HashMap<>();
        mutationParams.put("rate", mutationRate);

        MutationStrategy mutationStrategy = MutationStrategyFactory.createStrategy(
                mutationType, GlobalRandom.rdm, mutationParams);

        Map<String, Object> crossoverParams = new HashMap<>();
        CrossoverStrategy crossoverStrategy = CrossoverStrategyFactory.createStrategy(
                crossoverType, crossoverParams);

        Map<String, Object> selectionParams = new HashMap<>();
        selectionParams.put("pool_size", populationSize);
        selectionParams.put("k", 5);

        SelectionStrategy selectionStrategy = SelectionStrategyFactory.createStrategy(
                selectionType, GlobalRandom.rdm, selectionParams);

        GeneticAlgorithm ga = new GeneticAlgorithm(
                puzzle,
                GlobalRandom.rdm,
                populationSize,
                maxGenerations,
                crossoverRate,
                mutationRate,
                eliteCount,
                crossoverStrategy,
                mutationStrategy,
                selectionStrategy);

        return ga.run();
    }

    /**
     * Metode bantu untuk membaca baris dari file input txt.
     * Untuk string yang ada di suatu baris, dipecah menjadi array berdasarkan
     * spasi.
     * Contohnya: baris yang berisi "10 5" akan dipecah menjadi array ["10", "5"].
     * 
     * @param br objek BufferedReader untuk membaca file
     * @return array string yang dipecah dari baris yang dibaca
     * @throws IOException jika terjadi kesalahan saat membaca file
     */
    private static String[] parseLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            return line.split("\\s+");
        }
        throw new IOException("Unexpected end of file");
    }
}