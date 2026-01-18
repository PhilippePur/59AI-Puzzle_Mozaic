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
public class Mosaic {

    /**
     * Main method untuk menjalankan solver mosaic puzzle.
     * @param args argumen baris perintah. argumen pertama diharapkan adalah path ke file input. 
     * File yang bisa dipilih antara lain:
     * */
    public static void main(String[] args) {
        String filename = (args.length > 0) ? args[0] : "experiment_input.txt";

        try {
            // Membaca input dari file txt & Inisialisasi Puzzle (Object yang menyimpan informasi tentang context problem)
            BufferedReader br = new BufferedReader(new FileReader(filename));

            // Membaca dimensi puzzle
            String[] dims = parseLine(br);
            int rows = Integer.parseInt(dims[0]);
            int cols = Integer.parseInt(dims[1]);

            Puzzle puzzle = new Puzzle(rows, cols);

            // Membaca isi puzzle (kotak kosong & angka petunjuknya)
            for (int r = 0; r < rows; r++) {
                String[] line = parseLine(br);
                for (int c = 0; c < cols; c++) {
                    String valStr = line[c];
                    // Support input angka, -1, atau '.'
                    if (!valStr.equals(".") && !valStr.equals("-1")) {
                        int val = Integer.parseInt(valStr);
                        puzzle.addClue(new Clue(r, c, val));
                    }
                }
            }

            br.close(); // Membaca input selesai sampai sini

            // Mulai menghitung waktu eksekusi untuk ditampilkan di hasil
            long startTime = System.currentTimeMillis();

            // Deduksi awal menggunakan heuristic untuk mempersempit solution space
            HeuristicSolver.applyHeuristics(puzzle, -1); // Hasil langsung tercatat di variabel pada object puzzle

            // Persiapan Algoritma Genetik
            // Parameter & Operator pilihan (sudah di eksperimen dan dipilih yang terbaik)
            int populationSize = 100;
            int maxGenerations = 100;
            double mutationRate = 0.05;
            double crossoverRate = 0.7;
            int eliteCount = 5;
            
            // Konfigurasi tipe strategi (String)
            String mutationType = "basic";
            String crossoverType = "singleblock"; // Default value sesuai kode teman
            String selectionType = "tournament"; // Default value

            // Setup Strategi Mutasi menggunakan Factory
            Map<String, Object> mutationParams = new HashMap<>();
            mutationParams.put("rate", 0.05); // Default mutation rate jika basic

            MutationStrategy mutationStrategy = MutationStrategyFactory.createStrategy(
                    mutationType,
                    GlobalRandom.rdm,
                    mutationParams);

            // Setup Strategi Crossover menggunakan Factory
            Map<String, Object> crossoverParams = new HashMap<>();
            // Bisa tambahkan parameter khusus crossover jika ada (misal num_blocks untuk multiblock)
            CrossoverStrategy crossoverStrategy = CrossoverStrategyFactory.createStrategy(
                    crossoverType, 
                    crossoverParams);

            // Setup Strategi Seleksi menggunakan Factory
            Map<String, Object> selectionParams = new HashMap<>();
            selectionParams.put("pool_size", populationSize); // Pool size biasanya sama dengan ukuran populasi
            selectionParams.put("k", 5); // Default tournament size
            
            SelectionStrategy selectionStrategy = SelectionStrategyFactory.createStrategy(
                    selectionType, 
                    GlobalRandom.rdm, 
                    selectionParams);

            // Inisialisasi GA
            // Update konstruktor untuk menerima SelectionStrategy
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
                    selectionStrategy); // Added selectionStrategy

            // Run GA 
            Individual best = ga.run(); 

            // Mencatat waktu berakhirnya eksekusi
            long endTime = System.currentTimeMillis();

            // Laporan output
            System.out.println("===== MOSAIC PUZZLE SOLVER RESULT =====");
            System.out.printf("Execution time: %d ms\n", (endTime - startTime));
            System.out.println("Best Fitness: " + best.getFitness());
            System.out.println("Best Individual: \n" + best.toString());
        } catch (IOException e) {
            System.err.println("Gagal membaca file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Terjadi kesalahan sistem:");
            e.printStackTrace();
        }
    }

    /**
     * Metode bantu untuk membaca baris dari file input txt. 
     * Untuk string yang ada di suatu baris, dipecah menjadi array berdasarkan spasi.
     * Contohnya: baris yang berisi "10 5" akan dipecah menjadi array ["10", "5"].
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