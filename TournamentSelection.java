import java.util.List;

public class TournamentSelection {

    public static <T extends Individual> T select(List<T> population, int k) {
        T best = null;

        for (int i = 0; i < k; i++) {
            int idx = GlobalRandom.rdm.nextInt(population.size());
            T candidate = population.get(idx);

            if (best == null || candidate.getFitness() < best.getFitness()) {
                best = candidate;
            }
        }

        return best.copy();
    }
}
