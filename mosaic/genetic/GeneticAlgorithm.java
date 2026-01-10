// INI FILE BELUM BERES, KALIAN YANG TAMBAHIN
package mosaic.genetic;

import mosaic.Elitism;
import mosaic.genetic.mutation.*;
import mosaic.puzzle.*;
import mosaic.util.GlobalRandom;
import java.util.*;

public class GeneticAlgorithm {

    // variabels
    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final int eliteCount;
    private MutationStrategy mutationStrategy;

    private List<Individual> population;
    private final Puzzle puzzle;
    private int currentGeneration;
    private Individual bestIndividual;
    private double bestFitness;
    private double avgFitness;

    public GeneticAlgorithm(int popSize, int maxGen, double crossoverRate, int eliteCount,
            MutationStrategy mutationStrategy, Puzzle puzzle) {
        this.populationSize = popSize;
        this.maxGenerations = maxGen;
        this.crossoverRate = crossoverRate;
        this.eliteCount = eliteCount;
        this.mutationStrategy = mutationStrategy;
        this.puzzle = puzzle;

        this.population = new ArrayList<>();
        this.currentGeneration = 0;
        this.bestFitness = 0.0;
    }

    private List<Individual> applyElitism() {
        return Elitism.selectElite(population, eliteCount);
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

    public static void main(String[] args) {

    }
}
