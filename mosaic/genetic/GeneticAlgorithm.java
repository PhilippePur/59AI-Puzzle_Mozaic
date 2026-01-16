package mosaic.genetic;

import mosaic.genetic.crossover.CrossoverStrategy;
import mosaic.genetic.elitism.Elitism;
import mosaic.genetic.mutation.*;
import mosaic.puzzle.*;
import mosaic.util.GlobalRandom;
import java.util.*;

/**
 * Kelas yang menjalankan proses Genetic Algorithm untuk menyelesaikan puzzle
 * Kelas ini memungkinkan algoritma menggunakan metode dan parameter yang berbeda untuk eksperimen
 * @author Andrew, Michael G, 
 */
public class GeneticAlgorithm {

    /**
     * Konfigurasi parameter
     */
    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final double mutationRate;
    private final int eliteCount;

    /**
     * Metode metode yang digunakan untuk mutasi
     */
    private final MutationStrategy mutationStrategy;
    private final CrossoverStrategy crossoverStrategy;

    /**
     * variable yang digunakan untuk menyimpan state evolusi
     * seperti population yg menyimpan individu dalam populasi saat ini, 
     * currentGeneration yg mencatat saat ini sedang berada di proses generasi keberapa, dll
     */
    private List<Individual> population;
    private final Puzzle puzzle;
    private final Random rng;
    private int currentGeneration;
    private Individual bestIndividual;
    private double bestFitness;
    private double avgFitness;

    /**
     * Konstruktor untuk inisialisasi Genetic Algorithm. 
     * Dapat dipanggil melalui kelas main / kelas experimen dengan memasukkan parameter konfigurasi yang beragam
     * @param puzzle menyimpan informasi context masalah Mosaic
     * @param rng generator angka acak global
     * @param popSize ukuran populasi
     * @param maxGen jumlah maksimal generasi
     * @param crossoverRate probabilitas crossover
     * @param mutationRate probabilitas mutasi
     * @param eliteCount jumlah individu elit yg akan dipilih untuk dipertahankan
     * @param crossover metode crossover konkrit yang digunakan
     * @param mutation metode mutasi konkrit yang digunakan
     */
    public GeneticAlgorithm(Puzzle puzzle, Random rng, int popSize, int maxGen, 
        double crossoverRate, double mutationRate, int eliteCount, CrossoverStrategy crossover,
        MutationStrategy mutation) {
        this.puzzle = puzzle;
        this.rng = rng;
        this.populationSize = popSize;
        this.maxGenerations = maxGen;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.eliteCount = eliteCount;
        this.crossoverStrategy = crossover;
        this.mutationStrategy = mutation;
       
        // Inisialisasi array list kosong untuk menyimpan populasi
        this.population = new ArrayList<>();
        this.currentGeneration = 0;
        this.bestFitness = 0.0;
    }


    private void applyMutation(Individual individual) {
        // Jika mutationStrategy adalah AdaptiveMutation, update info
        if (mutationStrategy instanceof AdaptiveMutation) {
            AdaptiveMutation adaptive = (AdaptiveMutation) mutationStrategy;
            adaptive.updatePopulationInfo(currentGeneration, avgFitness, calculateDiversity());
        }

        // pake mutationStrategy.mutate() dari interface
        mutationStrategy.mutate(individual, puzzle);
        individual.calculateFitness();
    }

    private double calculateDiversity() {
        // Cara sederhana: hitung persentase gen yang berbeda
        if (population.size() < 2)
            return 1.0;
        int totalNonFixedCells = 0;
        int differences = 0;
        
        // Bandingkan individu pertama dengan kedua
        Individual first = population.get(0);
        Individual second = population.get(1);

        for (int r = 0; r < first.getRows(); r++) {
            for (int c = 0; c < first.getCols(); c++) {
                if (!first.isFixed(r, c)) {
                    totalNonFixedCells++;
                    if (first.getGrid()[r][c] != second.getGrid()[r][c]) {
                        differences++;
                    }
                }
            }
        }

        if (totalNonFixedCells == 0)
            return 0.0;
        return (double) differences / totalNonFixedCells;
    }

    /**
     * Metode utama yang menjalankan proses GA dari awal sampai akhir
     * @return individu terbaik yang ditemukan
     */
    public Individual run() {
        // Inisialisasi populasi awal
        initializePopulation();

        // Looping untuk setiap generasi proses evolusi
        // Berhenti jika mencapai maxGenerations atau ketika sudah ditemukan solusi sempurna (fitness = 1.0)
        while (currentGeneration < maxGenerations || bestFitness != 1.0) {
            // Sorting individu dalam populasi berdasarkan fitness individu (mulai dari terbesar ke terkecil)
            Collections.sort(population, (a, b) ->
                Double.compare(b.getFitness(), a.getFitness()));

            // Cek apakah solusi sempurna ditemukan (fitness = 1.0), jika ya maka langsung keluar dari loop
            if (population.get(0).getFitness() == 1.0) break;

            // Membuat generasi baru
            List<Individual> newPopulation = new ArrayList<>();

            // Pilih individu elit dari populasi saat ini dan ditambahkan ke newPopulation
            List<Individual> elit = Elitism.selectElite(population, eliteCount);
            newPopulation.addAll(elit);

            // Pembuatan anak sampai newPopulation = populationSize
            while (newPopulation.size() < populationSize) {
                // Parent Selection (pilih 2 parent) -- Kalo selection udah fix isi bagian ini
                // ....

                // Crossover
                Individual child;
                if (rng.nextDouble() < crossoverRate) {
                    child = crossoverStrategy.crossover(parent1, parent2, rng);
                } else {
                    child = parent1.copy();
                }

                // Mutation
                applyMutation(child);

                // Memasukkan anak yang sudah di mutasi ke dalam newPopulation
                newPopulation.add(child);
            }

            population = newPopulation;
            currentGeneration++;
        }

        Collections.sort(population, (a, b) ->
                Double.compare(b.getFitness(), a.getFitness()));
        return population.get(0);
    }

    /**
     * Membentuk populasi awal dengan individu acak yang memperhatikan fixed cells
     */
    private void initializePopulation() {
        // Memastikan populasi benar benar kosong
        population.clear();

        // Looping untuk buat setiap individu
        for (int i = 0; i < populationSize; i++) {
            // Inisialisasi individu baru (proses pembentukannya secara random namun tetap memperhatikan fixed cells - logicnya ada di constructor Individual)
            Individual individual = new Individual(puzzle, rng);
            population.add(individual);
        }
    }
}
