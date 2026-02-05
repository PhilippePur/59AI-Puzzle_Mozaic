
package mosaic.genetic;

import mosaic.genetic.crossover.CrossoverStrategy;
import mosaic.genetic.crossover.UniformCrossover;
import mosaic.genetic.elitism.Elitism;
import mosaic.genetic.mutation.*;
import mosaic.genetic.selection.SelectionStrategy;
import mosaic.genetic.selection.TournamentSelection;
import mosaic.puzzle.*;
import mosaic.util.GlobalRandom;
import java.util.*;

/**
 * Kelas yang menjalankan proses Genetic Algorithm untuk menyelesaikan puzzle
 * Kelas ini memungkinkan algoritma menggunakan metode dan parameter yang
 * berbeda untuk eksperimen
 * 
 * @author Andrew, Michael G, Michael Philippe
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
    private final SelectionStrategy selectionStrategy;

    /**
     * variable yang digunakan untuk menyimpan state evolusi
     * seperti population yg menyimpan individu dalam populasi saat ini,
     * currentGeneration yg mencatat saat ini sedang berada di proses generasi
     * keberapa, dll
     */
    private final Random rng;
    private List<Individual> population;
    private final Puzzle puzzle;
    private int currentGeneration;
    private Individual bestIndividual;
    private double bestFitness;
    private double avgFitness;

    /**
     * Konstruktor untuk inisialisasi Genetic Algorithm.
     * Dapat dipanggil melalui kelas main / kelas experimen dengan memasukkan
     * parameter konfigurasi yang beragam
     * 
     * @param puzzle        menyimpan informasi context masalah Mosaic
     * @param rng           generator angka acak global (untuk deterministik)
     * @param popSize       ukuran populasi
     * @param maxGen        jumlah maksimal generasi
     * @param crossoverRate probabilitas crossover
     * @param mutationRate  probabilitas mutasi
     * @param eliteCount    jumlah individu elit yg akan dipilih untuk dipertahankan
     * @param crossover     metode crossover konkrit yang digunakan
     * @param mutation      metode mutasi konkrit yang digunakan
     * @param selection     metode seleksi konkrit yang digunakan
     */
    public GeneticAlgorithm(Puzzle puzzle, Random rng, int popSize, int maxGen,
            double crossoverRate, double mutationRate, int eliteCount,
            CrossoverStrategy crossover, MutationStrategy mutation, SelectionStrategy selection) {

        this.puzzle = puzzle;
        this.rng = rng;
        this.populationSize = popSize;
        this.maxGenerations = maxGen;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.eliteCount = eliteCount;
        this.crossoverStrategy = crossover;
        this.mutationStrategy = mutation;
        this.selectionStrategy = selection;

        this.population = new ArrayList<>();
        this.currentGeneration = 0;
        this.bestFitness = 0.0;
    }

    /**
     * Menerapkan mutasi pada individu berdasarkan strategi yang dipilih.
     * Juga memperbarui statistik populasi jika menggunakan Adaptive Mutation.
     */
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

    /**
     * Menghitung diversitas populasi berdasarkan perbedaan gen antara dua individu
     * sampel.
     */
    private double calculateDiversity() {
        if (population.size() < 2)
            return 1.0;

        int totalNonFixedCells = 0;
        int differences = 0;

        // Bandingkan individu juara pertama dengan kedua
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
     * 
     * @return individu terbaik yang ditemukan
     */
    public Individual run() {
        // Inisialisasi populasi awal
        initializePopulation();

        // Evaluasi awal
        Collections.sort(population, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        bestIndividual = population.get(0).copy();
        bestFitness = bestIndividual.getFitness();

        // Looping untuk setiap generasi proses evolusi
        // Berhenti jika mencapai maxGenerations atau ketika sudah ditemukan solusi
        // sempurna (fitness = 1.0)
        while (currentGeneration < maxGenerations && bestFitness < 1.0) {

            // Membuat generasi baru
            List<Individual> newPopulation = new ArrayList<>();

            // Pilih individu elit dari populasi saat ini dan ditambahkan ke newPopulation
            List<Individual> elit = Elitism.selectElite(population, eliteCount);
            newPopulation.addAll(elit);

            // Pembuatan anak sampai newPopulation = populationSize
            while (newPopulation.size() < populationSize) {
                List<Individual> parents = selectionStrategy.select(population);

                Individual parent1 = parents.get(rng.nextInt(parents.size()));
                Individual parent2 = parents.get(rng.nextInt(parents.size()));

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

            // Evaluasi populasi baru
            Collections.sort(population, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
            Individual currentBest = population.get(0);

            // Update Global Best jika ditemukan yang lebih baik
            if (currentBest.getFitness() > bestFitness) {
                bestFitness = currentBest.getFitness();
                bestIndividual = currentBest.copy();
            }
        }

        // Final sorting dan check
        Collections.sort(population, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        if (population.get(0).getFitness() > bestFitness) {
            bestIndividual = population.get(0).copy();
        }
        calculateAvgFitness();
        return bestIndividual;
    }

    
    /**
     * Membentuk populasi awal dengan individu acak yang memperhatikan fixed cells
    */
   private void initializePopulation() {
        // Memastikan populasi benar benar kosong
        population.clear();

        // Looping untuk buat setiap individu
        for (int i = 0; i < populationSize; i++) {
            // Inisialisasi individu baru (proses pembentukannya secara random namun tetap
            // memperhatikan fixed cells - logicnya ada di constructor Individual)
            Individual individual = new Individual(puzzle, rng);
            population.add(individual);
        }
    }
    
    private void calculateAvgFitness() {
        double total = 0;
        for (Individual ind : population) {
            total += ind.getFitness();
        }
        this.avgFitness = total / population.size();
    }
    
    public int getCurrentGeneration() {
        return currentGeneration;
    }
    
    public double getBestFitness() {
        return bestFitness;
    }
}