import java.util.*;

public class Elitism {

    public static <T extends Individual> List<T> selectElite(List<T> population, int eliteCount) {
        Collections.sort(population, new Comparator<T>() {
            public int compare(T a, T b) {
                return a.getFitness() - b.getFitness();
            }
        });

        List<T> elite = new ArrayList<T>();
        for (int i = 0; i < eliteCount; i++) {
            elite.add((T) population.get(i).copy());//biar yang lama ga keubah pke copy
        }

        return elite;
    }
}
