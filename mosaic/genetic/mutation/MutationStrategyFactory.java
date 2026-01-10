package mosaic.genetic.mutation;

import java.util.Random;

import mosaic.puzzle.Individual;
import mosaic.puzzle.Puzzle;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

public class MutationStrategyFactory {

    private MutationStrategyFactory() {// biar gabisa di buat sembarangan jadi diprivate

    }

    public static MutationStrategy createStrategy(String type, Random random, Map<String, Object> params) {

        String typeLower = type.toLowerCase();

        if ("basic".equals(typeLower)) {
            return createBasicMutation(random, params);
        } else if ("constraint".equals(typeLower) || "constraintaware".equals(typeLower)) {
            return createConstraintAwareMutation(random);
        } else if ("adaptive".equals(typeLower)) {
            return createAdaptiveMutation(random, params);
        } else if ("hybrid".equals(typeLower)) {
            return createHybridMutation(random, params);
        } else if ("random".equals(typeLower)) {
            return createRandomMutation(random);
        } else {
            throw new IllegalArgumentException("Unknown mutation strategy: " + type);
        }
    }

    private static MutationStrategy createBasicMutation(Random random, Map<String, Object> params) {
        double rate = (double) params.getOrDefault("rate", 0.05);
        return new BasicMutation(rate, random);
    }

    private static MutationStrategy createConstraintAwareMutation(Random random) {
        return new ConstraintAwareMutation(random);
    }

    private static MutationStrategy createAdaptiveMutation(Random random, Map<String, Object> params) {
        // Buat beberapa strategi dasar
        List<MutationStrategy> baseStrategies = Arrays.asList(
                new BasicMutation(0.05, random),
                new BasicMutation(0.1, random),
                new ConstraintAwareMutation(random));

        return new AdaptiveMutation(baseStrategies);
    }

    private static MutationStrategy createHybridMutation(Random random, Map<String, Object> params) {
        // Hybrid: 50% basic, 50% constraint-aware
        return new MutationStrategy() {
            @Override
            public void mutate(Individual individual, Puzzle puzzle) {
                if (random.nextDouble() < 0.5) {
                    new BasicMutation(0.05, random).mutate(individual, puzzle);
                } else {
                    new ConstraintAwareMutation(random).mutate(individual, puzzle);
                }
            }

            @Override
            public String getStrategyName() {
                return "HybridMutation";
            }
        };
    }

    private static MutationStrategy createRandomMutation(Random random) {
        // Mutasi dengan teknik yang benar-benar random
        return new MutationStrategy() {
            @Override
            public void mutate(Individual individual, Puzzle puzzle) {
                double choice = random.nextDouble();

                if (choice < 0.33) {
                    // Flip single random cell
                    int r = random.nextInt(individual.getRows());
                    int c = random.nextInt(individual.getCols());
                    if (!individual.isFixed(r, c)) {
                        individual.flipCell(r, c);
                    }
                } else if (choice < 0.66) {
                    // Flip 3x3 block
                    int centerR = 1 + random.nextInt(individual.getRows() - 2);
                    int centerC = 1 + random.nextInt(individual.getCols() - 2);
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int r = centerR + dr;
                            int c = centerC + dc;
                            if (r >= 0 && r < individual.getRows() &&
                                    c >= 0 && c < individual.getCols() &&
                                    !individual.isFixed(r, c)) {
                                individual.flipCell(r, c);
                            }
                        }
                    }
                } else {
                    // Flip entire row or column
                    if (random.nextBoolean()) {
                        // Flip random row
                        int r = random.nextInt(individual.getRows());
                        for (int c = 0; c < individual.getCols(); c++) {
                            if (!individual.isFixed(r, c)) {
                                individual.flipCell(r, c);
                            }
                        }
                    } else {
                        // Flip random column
                        int c = random.nextInt(individual.getCols());
                        for (int r = 0; r < individual.getRows(); r++) {
                            if (!individual.isFixed(r, c)) {
                                individual.flipCell(r, c);
                            }
                        }
                    }
                }
            }

            @Override
            public String getStrategyName() {
                return "RandomMutation";
            }

        };
    }

    public static MutationStrategy createDefaultStrategy(Random random) {
        return createAdaptiveMutation(random, new HashMap<>());
    }
}