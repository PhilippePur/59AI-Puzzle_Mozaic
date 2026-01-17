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
 * Kelas utama (Main Class) untuk eksekusi tunggal Solver Mosaic Puzzle.
 * <p>
 * Bertanggung jawab untuk:
 * <ul>
 * <li>Membaca konfigurasi puzzle dan parameter GA dari file input.</li>
 * <li>Menjalankan heuristik awal (pre-processing) untuk deduksi logika.</li>
 * <li>Menginisialisasi dan menjalankan {@link GeneticAlgorithm}.</li>
 * <li>Menampilkan metrik hasil akhir ke konsol.</li>
 * </ul>
 */
public class Mosaic {

    /**
     * Membaca file, melakukan seeding random global, menjalankan heuristik, dan memicu evolusi GA.
     *
     * @param args argumen baris perintah; args[0] adalah path file input (opsional).
     */
    public static void main(String[] args) {
        String filename = (args.length > 0) ? args[0] : "puzzle_input.txt";
        System.out.println("=== MOSAIC PUZZLE SOLVER ===");
        System.out.println("Membaca file input: " + filename);

        try {
            // Parsing File Input & Inisialisasi Puzzle
            SolverConfig config = parseInputFile(filename);
            Puzzle puzzle = config.puzzle;

            // Set Seed Global agar hasil eksekusi ini deterministik (dapat direproduksi)
            GlobalRandom.rdm.setSeed(config.seed);
            System.out.println("Seed diset ke: " + config.seed);

            System.out.printf("Puzzle dimuat: %dx%d dengan %d clues.\n",
                    puzzle.getRows(), puzzle.getCols(), puzzle.getClues().size());

            // Deduksi Awal (Heuristic Pre-processing)
            System.out.println("\n--- TAHAP 1: HEURISTIC SOLVER ---");
            long startHeuristic = System.currentTimeMillis();
            
            // Mengunci sel-sel yang solusinya sudah pasti secara logika
            int fixedCells = HeuristicSolver.applyHeuristics(puzzle, -1);
            
            long endHeuristic = System.currentTimeMillis();
            System.out.printf("Heuristik selesai dalam %d ms.\n", (endHeuristic - startHeuristic));
            System.out.printf("Status: %d sel berhasil dikunci (%.2f%% dari total papan).\n", 
                    fixedCells, (double) fixedCells / (puzzle.getRows() * puzzle.getCols()) * 100);

            // Persiapan Algoritma Genetik
            System.out.println("\n--- TAHAP 2: GENETIC ALGORITHM ---");
            
            Map<String, Object> mutationParams = new HashMap<>();
            mutationParams.put("rate", 0.05);
            
            MutationStrategy mutationStrategy = MutationStrategyFactory.createStrategy(
                    config.mutationType, 
                    GlobalRandom.rdm, 
                    mutationParams
            );
            System.out.println("Strategi Mutasi: " + mutationStrategy.getStrategyName());

            // Menggunakan GlobalRandom untuk single run
            GeneticAlgorithm ga = new GeneticAlgorithm(
                    config.populationSize,
                    config.maxGenerations,
                    config.crossoverRate,
                    config.eliteCount,
                    mutationStrategy,
                    puzzle,
                    GlobalRandom.rdm 
            );

            // Jalankan GA
            System.out.println("Memulai proses evolusi...");
            long startGA = System.currentTimeMillis();
            
            ga.run(); 
            
            long endGA = System.currentTimeMillis();
            System.out.println("Evolusi selesai.");
            System.out.printf("Total Waktu GA: %d ms\n", (endGA - startGA));
            System.out.printf("Total Waktu Eksekusi: %d ms\n", (endGA - startHeuristic));

        } catch (IOException e) {
            System.err.println("Gagal membaca file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Terjadi kesalahan sistem:");
            e.printStackTrace();
        }
    }

    /**
     * Memparsing file teks input menjadi objek konfigurasi dan puzzle.
     *
     * @param filename lokasi file input
     * @return konfigurasi solver berisi parameter dan objek puzzle awal
     * @throws IOException jika terjadi kesalahan IO saat membaca file
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
     * Membaca baris berikutnya yang valid (mengabaikan baris kosong atau komentar).
     */
    private static String[] parseLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            return line.split("\\s+");
        }
        throw new IOException("Unexpected end of file");
    }

    /**
     * Struktur data sederhana untuk menampung hasil parsing konfigurasi.
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