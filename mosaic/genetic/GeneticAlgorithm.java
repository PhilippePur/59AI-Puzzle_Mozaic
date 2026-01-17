package mosaic.genetic;

import mosaic.genetic.elitism.*;
import mosaic.genetic.crossover.CrossoverStrategy;
import mosaic.genetic.crossover.UniformCrossover;
import mosaic.genetic.mutation.*;
import mosaic.puzzle.*;
import mosaic.genetic.selection.*;
import java.util.*;

/**
 * Mesin utama Algoritma Genetik.
 * <p>
 * Kelas ini mengelola siklus hidup evolusi populasi mulai dari inisialisasi,
 * evaluasi fitness, seleksi, crossover, hingga mutasi.
 * Menggunakan instance {@link Random} yang diinjeksi untuk mendukung eksekusi paralel yang aman (thread-safe).
 */
public class GeneticAlgorithm {
    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final int eliteCount;
    private final MutationStrategy mutationStrategy;
    private final CrossoverStrategy crossoverStrategy;
    private final Random rng;
    private List<Individual> population;
    private final Puzzle puzzle;
    private int currentGeneration;
    private Individual bestIndividual;
    private double bestFitness;
    private double avgFitness;

    /**
     * Inisialisasi engine Algoritma Genetik dengan konfigurasi lengkap.
     *
     * @param rng objek Random spesifik (penting untuk isolasi thread pada eksperimen paralel)
     */
    public GeneticAlgorithm(int popSize, int maxGen, double crossoverRate, int eliteCount,
            MutationStrategy mutationStrategy, CrossoverStrategy crossoverStrategy, 
            Puzzle puzzle, Random rng) {
        this.populationSize = popSize;
        this.maxGenerations = maxGen;
        this.crossoverRate = crossoverRate;
        this.eliteCount = eliteCount;
        this.mutationStrategy = mutationStrategy;
        this.crossoverStrategy = crossoverStrategy;
        this.puzzle = puzzle;
        this.rng = rng;

        this.population = new ArrayList<>();
        this.currentGeneration = 0;
        this.bestFitness = 0.0;
    }

    /**
     * Konstruktor overaloading dengan default Uniform Crossover.
     */
    public GeneticAlgorithm(int popSize, int maxGen, double crossoverRate, int eliteCount,
            MutationStrategy mutationStrategy, Puzzle puzzle, Random rng) {
        this(popSize, maxGen, crossoverRate, eliteCount, mutationStrategy, new UniformCrossover(), puzzle, rng);
    }

    /**
     * Menjalankan loop evolusi hingga kriteria berhenti terpenuhi (solusi ditemukan atau batas generasi).
     *
     * @return Individu terbaik yang ditemukan pada akhir proses.
     */
    public Individual run() {
        initPopulation();
        
        while (currentGeneration < maxGenerations) {
            calculatePopulationStats();
            
            // Berhenti jika solusi optimal (fitness 1.0) ditemukan
            if (Math.abs(bestFitness - 1.0) < 0.000001) {
                break;
            }

            population = evolve();
            currentGeneration++;
        }
        
        calculatePopulationStats(); // Update statistik akhir
        return bestIndividual;
    }

    /**
     * Membangkitkan populasi awal secara acak (namun tetap menghormati sel yang dikunci/fixed).
     */
    private void initPopulation() {
        for (int i = 0; i < populationSize; i++) {
            Individual ind = new Individual(puzzle, rng);
            ind.calculateFitness();
            population.add(ind);
        }
    }

    /**
     * Membentuk generasi baru melalui proses Elitism, Seleksi, Crossover, dan Mutasi.
     */
    private List<Individual> evolve() {
        List<Individual> newPopulation = new ArrayList<>();

        // 1. Elitism: Pertahankan individu terbaik
        List<Individual> elites = applyElitism();
        newPopulation.addAll(elites);

        // 2. Reproduksi sisa populasi
        while (newPopulation.size() < populationSize) {
            Individual p1 = selectParent();
            Individual p2 = selectParent();

            Individual offspring;
            if (rng.nextDouble() < crossoverRate) {
                offspring = crossoverStrategy.crossover(p1, p2, rng);
            } else {
                offspring = p1.copy();
            }

            applyMutation(offspring);
            
            offspring.calculateFitness();
            newPopulation.add(offspring);
        }

        return newPopulation;
    }

    /**
     * Memilih parent menggunakan Tournament Selection lokal.
     */
    private Individual selectParent() {
        int k = 5; // Ukuran turnamen
        Individual best = null;
        for (int i = 0; i < k; i++) {
            Individual ind = population.get(rng.nextInt(population.size()));
            if (best == null || ind.getFitness() > best.getFitness()) {
                best = ind;
            }
        }
        return best.copy();
    }

    /**
     * Mengambil N individu terbaik dari populasi saat ini.
     */
    private List<Individual> applyElitism() {
        return Elitism.selectElite(population, eliteCount);
    }

    /**
     * Menerapkan mutasi pada individu berdasarkan strategi yang dipilih.
     * Juga memperbarui statistik populasi jika menggunakan Adaptive Mutation.
     */
    private void applyMutation(Individual individual) {
        if (mutationStrategy instanceof AdaptiveMutation) {
            AdaptiveMutation adaptive = (AdaptiveMutation) mutationStrategy;
            adaptive.updatePopulationInfo(currentGeneration, avgFitness, calculateDiversity());
        }
        mutationStrategy.mutate(individual, puzzle);
    }

    /**
     * Menghitung statistik fitness (terbaik dan rata-rata) populasi saat ini.
     */
    private void calculatePopulationStats() {
        double totalFit = 0;
        bestFitness = -1.0;
        bestIndividual = null;

        for (Individual ind : population) {
            double fit = ind.getFitness();
            totalFit += fit;
            if (fit > bestFitness) {
                bestFitness = fit;
                bestIndividual = ind;
            }
        }
        if (!population.isEmpty()) avgFitness = totalFit / population.size();
    }
    
    public int getCurrentGeneration() { return currentGeneration; }
    public double getBestFitness() { return bestFitness; }
    
    /**
     * Menghitung diversitas populasi berdasarkan perbedaan gen antara dua individu sampel.
     */
    private double calculateDiversity() {
        if (population.size() < 2) return 0.0;
        int totalNonFixedCells = 0;
        int differences = 0;
        Individual first = population.get(0);
        Individual second = population.get(1);

        for (int r = 0; r < first.getRows(); r++) {
            for (int c = 0; c < first.getCols(); c++) {
                if (!first.isFixed(r, c)) {
                    totalNonFixedCells++;
                    if (first.getCell(r, c) != second.getCell(r, c)) {
                        differences++;
                    }
                }
            }
        }
        if (totalNonFixedCells == 0) return 0.0;
        return (double) differences / totalNonFixedCells;
    }
}
