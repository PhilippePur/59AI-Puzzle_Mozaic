<<<<<<< HEAD:mosaic/genetic/selection/TournamentSelection.java
package mosaic.genetic.selection;

=======
>>>>>>> b6ceb761bd511f48ba3cb310c568d496649b504a:mosaic/selection/TournamentSelection.java
import java.util.List;
import mosaic.puzzle.Individual;
import mosaic.util.GlobalRandom;

public class TournamentSelection {
    public static Individual select(List<Individual> population, int k) {
        Individual best = null;

        for (int i = 0; i < k; i++) {
            int idx = GlobalRandom.rdm.nextInt(population.size());
            Individual candidate = population.get(idx);

            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }

        return best.copy();
    }
}