package mosaic.genetic.mutation;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import mosaic.util.GlobalRandom;


public class AdaptiveMutation implements MutationStrategy {

    private final List<MutationStrategy> availableStrategies;
    private final Random random;
    private int currentGeneration;
    private double averageFitness;
    private double diversity;


    public AdaptiveMutation(List<MutationStrategy> strategies) {

        this.availableStrategies = new ArrayList<>(strategies);
        this.currentGeneration = 0;
        this.averageFitness = 0.0;
        this.diversity = 1.0;
        this.random = GlobalRandom.rdm;
    }


    public void updatePopulationInfo(int generation, double avgFitness, double diversity) {
        this.currentGeneration = generation;
        this.averageFitness = avgFitness;
        this.diversity = Math.max(0.0, Math.min(1.0, diversity));
    }

    @Override
    public void mutate(Individual individual, Puzzle puzzle) {
        MutationStrategy selectedStrategy = selectStrategyBasedOnConditions();

        selectedStrategy.mutate(individual, puzzle);
    }

    private MutationStrategy selectStrategyBasedOnConditions() {
        if (currentGeneration < 50) {
            return selectForExplorationPhase();
        }

        if (currentGeneration < 200) {
            return selectForBalancedPhase();
        }

        return selectForExploitationPhase();
    }

    private MutationStrategy selectForExplorationPhase() {
        if (diversity < 0.3) {
            return findStrategyByName("BasicMutation");
        }

        return availableStrategies.get(random.nextInt(availableStrategies.size()));
    }

    private MutationStrategy selectForBalancedPhase() {
        double rand = random.nextDouble(); 

        if (rand < 0.5) { //EXPERIMENT
            return findStrategyByName("ConstraintAwareMutation");
        } else {
            return findStrategyByName("BasicMutation");
        }
    }

    private MutationStrategy selectForExploitationPhase() {
        if (averageFitness > 0.8) { //EXPERIMEENT
            return findStrategyByName("ConstraintAwareMutation");
        } else {
            double rand = random.nextDouble(); 

            if (rand < 0.5) {//EXPERIMENT
                return findStrategyByName("ConstraintAwareMutation");
            } else {
                return findStrategyByName("BasicMutation");
            }
        }
    }

    private MutationStrategy findStrategyByName(String name) {
        for (MutationStrategy strategy : availableStrategies) {
            if (strategy.getStrategyName().contains(name)) {
                return strategy;
            }
        }

        return availableStrategies.get(0);
    }

    

    public int getStrategyCount() {
        return availableStrategies.size();
    }


    @Override
    public String getStrategyName() {
       return "AdaptiveMutation";
    }
}