package mosaic.puzzle;

import mosaic.genetic.GeneticAlgorithm;
import mosaic.genetic.mutation.MutationStrategy;
import mosaic.genetic.mutation.MutationStrategyFactory;
import mosaic.util.GlobalRandom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Kelas utama (Main Class) untuk menjalankan Solver Mosaic Puzzle.
 * <p>
 * Kelas ini bertanggung jawab untuk:
 * <ol>
 * <li>Membaca file input yang berisi definisi puzzle dan hiperparameter Algoritma Genetik.</li>
 * <li>Menginisialisasi objek {@link Puzzle} dan mengisi Clue.</li>
 * <li>Menjalankan {@link HeuristicSolver} untuk deduksi awal (pre-processing).</li>
 * <li>Mengonfigurasi dan menjalankan {@link GeneticAlgorithm}.</li>
 * <li>Menampilkan hasil akhir ke konsol.</li>
 * </ol>
 * </p>
 *
 * <h3>Spesifikasi Format File Input (.txt):</h3>
 * <pre>
 * Baris 1: [Rows] [Cols]
 * Baris 2: [Seed]
 * Baris 3: [PopulationSize] [MaxGenerations] [CrossoverRate] [EliteCount] [MutationType]
 * Baris 4+: [Grid Data...]
 * </pre>
 *
 * <p><b>Keterangan Parameter:</b></p>
 * <ul>
 * <li><b>Rows, Cols:</b> Dimensi papan permainan (Integer).</li>
 * <li><b>Seed:</b> Angka untuk Random Number Generator (Long).</li>
 * <li><b>MutationType:</b> String (contoh: "basic", "adaptive", "constraint").</li>
 * <li><b>Grid Data:</b> Matriks angka dipisahkan spasi. Gunakan -1 atau '.' untuk sel kosong, dan 0-9 untuk clue.</li>
 * </ul>
 *
 * <p><b>Contoh Input:</b></p>
 * <pre>
 * 5 5
 * 12345
 * 200 1000 0.9 4 adaptive
 * -1 -1 4 -1 -1
 * -1 6 -1 6 -1
 * -1 -1 9 -1 -1
 * -1 6 -1 6 -1
 * -1 -1 4 -1 -1
 * </pre>
 */
public class Mosaic {

    /**
     * Metode utama aplikasi.
     *
     * @param args argumen baris perintah. argumen pertama diharapkan adalah path ke file input.
     * Jika kosong, akan default ke "puzzle_input.txt".
     */
    public static void main(String[] args) {
        String filename = (args.length > 0) ? args[0] : "puzzle_input.txt";
        System.out.println("=== MOSAIC PUZZLE SOLVER ===");
        System.out.println("Membaca file input: " + filename);

        try {
            // 1. Parsing File Input & Inisialisasi Puzzle
            SolverConfig config = parseInputFile(filename);
            Puzzle puzzle = config.puzzle;

            // Set Seed Global untuk Reproducibility
            // PENTING: Seed ini menjamin bahwa seluruh proses di bawah ini dapat diulang persis sama.
            GlobalRandom.rdm.setSeed(config.seed);
            System.out.println("Seed diset ke: " + config.seed);

            System.out.printf("Puzzle dimuat: %dx%d dengan %d clues.\n",
                    puzzle.getRows(), puzzle.getCols(), puzzle.getClues().size());

            // 2. Deduksi Awal (Heuristic Pre-processing)
            System.out.println("\n--- TAHAP 1: HEURISTIC SOLVER ---");
            long startHeuristic = System.currentTimeMillis();
            
            // Memanggil HeuristicSolver untuk mengunci sel-sel yang pasti
            // Menggunakan limit -1 (tanpa batas iterasi)
            int fixedCells = HeuristicSolver.applyHeuristics(puzzle, -1);
            
            long endHeuristic = System.currentTimeMillis();
            System.out.printf("Heuristik selesai dalam %d ms.\n", (endHeuristic - startHeuristic));
            System.out.printf("Status: %d sel berhasil dikunci (%.2f%% dari total papan).\n", 
                    fixedCells, (double) fixedCells / (puzzle.getRows() * puzzle.getCols()) * 100);

            // 3. Persiapan Algoritma Genetik
            System.out.println("\n--- TAHAP 2: GENETIC ALGORITHM ---");
            
            // Setup Strategi Mutasi menggunakan Factory
            Map<String, Object> mutationParams = new HashMap<>();
            mutationParams.put("rate", 0.05); // Default mutation rate jika basic
            
            MutationStrategy mutationStrategy = MutationStrategyFactory.createStrategy(
                    config.mutationType, 
                    GlobalRandom.rdm, // Menggunakan GlobalRandom agar mutasi juga deterministik
                    mutationParams
            );
            System.out.println("Strategi Mutasi: " + mutationStrategy.getStrategyName());

            // Inisialisasi GA
            // PENTING: Kita mengoper GlobalRandom.rdm ke dalam GA.
            // Ini memastikan GA menggunakan generator acak yang sama dengan yang kita seed di awal.
            GeneticAlgorithm ga = new GeneticAlgorithm(
                    config.populationSize,
                    config.maxGenerations,
                    config.crossoverRate,
                    config.eliteCount,
                    mutationStrategy,
                    puzzle,
                    GlobalRandom.rdm // Inject GlobalRandom
            );

            // 4. Jalankan GA
            System.out.println("Memulai proses evolusi...");
            long startGA = System.currentTimeMillis();
            
            ga.run(); // Menjalankan loop evolusi
            
            long endGA = System.currentTimeMillis();
            System.out.println("Evolusi selesai.");
            System.out.printf("Total Waktu GA: %d ms\n", (endGA - startGA));
            System.out.printf("Total Waktu Eksekusi: %d ms\n", (endGA - startHeuristic));

        } catch (IOException e) {
            System.err.println("Gagal membaca file: " + e.getMessage());
            System.err.println("Pastikan file ada dan formatnya sesuai spesifikasi.");
        } catch (Exception e) {
            System.err.println("Terjadi kesalahan sistem:");
            e.printStackTrace();
        }
    }

    /**
     * Membaca file teks dan mengubahnya menjadi konfigurasi solver.
     *
     * @param filename path ke file input
     * @return objek {@link SolverConfig} yang berisi puzzle dan hyperparameter
     * @throws IOException jika terjadi kesalahan I/O
     */
    private static SolverConfig parseInputFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        
        // Baris 1: Dimensi
        String[] dims = parseLine(br);
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);
        
        Puzzle puzzle = new Puzzle(rows, cols);

        // Baris 2: Seed
        String[] seedLine = parseLine(br);
        long seed = Long.parseLong(seedLine[0]);

        // Baris 3: Hyperparameter GA
        String[] params = parseLine(br);
        int popSize = Integer.parseInt(params[0]);
        int maxGen = Integer.parseInt(params[1]);
        double crossRate = Double.parseDouble(params[2]);
        int eliteCount = Integer.parseInt(params[3]);
        String mutType = params[4];

        // Baris 4 dst: Grid Data
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
        
        br.close();
        return new SolverConfig(puzzle, seed, popSize, maxGen, crossRate, eliteCount, mutType);
    }

    /**
     * Helper untuk membaca baris non-kosong dan memecahnya berdasarkan spasi/tab.
     */
    private static String[] parseLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue; // Skip kosong atau komentar
            return line.split("\\s+");
        }
        throw new IOException("Unexpected end of file");
    }

    /**
     * Inner class sederhana untuk menampung hasil parsing.
     * Hanya digunakan sebagai Data Transfer Object (DTO).
     */
    private static class SolverConfig {
        Puzzle puzzle;
        long seed;
        int populationSize;
        int maxGenerations;
        double crossoverRate;
        int eliteCount;
        String mutationType;

        public SolverConfig(Puzzle puzzle, long seed, int popSize, int maxGen, double crossRate, int eliteCount, String mutType) {
            this.puzzle = puzzle;
            this.seed = seed;
            this.populationSize = popSize;
            this.maxGenerations = maxGen;
            this.crossoverRate = crossRate;
            this.eliteCount = eliteCount;
            this.mutationType = mutType;
        }
    }
}