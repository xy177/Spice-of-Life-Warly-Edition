package com.xy177.solwarly;

import com.xy177.solwarly.diet.PreparationGraph;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class PreparationGraphTest {
    @Test(timeout = 3000) public void independentRawIngredientTagsDoNotExpandTheirCartesianProduct() {
        PreparationGraph graph = new PreparationGraph();
        List<List<String>> slots = new ArrayList<>();
        for (int slot = 0; slot < 5; slot++) {
            List<String> choices = new ArrayList<>();
            for (int i = 0; i < 80; i++) choices.add("raw" + slot + ":" + i);
            slots.add(choices);
        }
        graph.add("dish", "combine", 1, slots);
        assertEquals(1, graph.analyzeTargets(List.of("dish"), item -> {}).get("dish"), 0);
    }
    @Test(timeout = 3000) public void independentPreparedIngredientTagsUseASafeRemainingCostBound() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        List<List<String>> slots = new ArrayList<>();
        for (int slot = 0; slot < 5; slot++) {
            List<String> choices = new ArrayList<>();
            for (int i = 0; i < 80; i++) {
                String input = "prepared" + slot + ":" + i;
                choices.add(input); recipe(graph, input, input, 1, "raw");
            }
            slots.add(choices);
        }
        graph.add("dish", "combine", 1, slots);
        assertEquals(6, graph.analyzeTargets(List.of("dish"), item -> {}).get("dish"), 0);
    }
    @Test public void rawAlternativeAndNestedTagsKeepTheExactMinimum() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "expensive", "prepare", 5, "raw");
        graph.add("dish", "combine", 1, List.of(List.of("raw", "expensive")));
        assertEquals(1, graph.analyze().get("dish"), 0);
        graph.add("nested", "nested", 1, List.of(List.of("expensive"), List.of("expensive", "dish")));
        assertEquals(6, graph.analyze().get("nested"), 0);
    }
    @Test public void targetOnlyAnalysisStillCountsAllBranchesWithoutScoringEveryAlternative() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "a", "a", 2, "raw"); recipe(graph, "b", "b", 3, "raw");
        recipe(graph, "dish", "combine", 1, "a", "b");
        List<String> progress = new ArrayList<>();
        Map<String, Double> result = graph.analyzeTargets(List.of("dish"), progress::add);
        assertEquals(6, result.get("dish"), 0);
        assertFalse(result.containsKey("a")); assertFalse(result.containsKey("b"));
        assertEquals(List.of("dish"), progress);
    }
    private void recipe(PreparationGraph graph, String output, String operation, double weight, String... inputs) {
        List<List<String>> slots = new ArrayList<>();
        for (String input : inputs) slots.add(Collections.singletonList(input));
        graph.add(output, operation, weight, slots);
    }
    @Test public void manyEquivalentSlotsDoNotMultiplyRoutes() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            String wood = "wood" + i; choices.add(wood);
            recipe(graph, wood, "prepare" + i, 1 + i * 0.25, "raw");
        }
        List<List<String>> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            List<String> options = new ArrayList<>(choices);
            Collections.shuffle(options, new Random(i)); slots.add(options);
        }
        graph.add("dish", "combine", 1, slots);
        assertEquals(2, graph.analyze(Collections.singleton("dish")).get("dish"), 0);
    }
    @Test public void cyclesAcrossSelectedBranchesCannotReplaceGroundedPreparation() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "a", "aFromB", 0, "b"); recipe(graph, "b", "bFromA", 0, "a");
        recipe(graph, "a", "groundA", 3, "raw"); recipe(graph, "b", "groundB", 4, "raw");
        recipe(graph, "dish", "combine", 1, "a", "b");
        assertEquals(4, graph.analyze().get("dish"), 0);
    }
    @Test public void multipleOutputsOfOneOperationAreChargedOnce() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "a", "cut", 3, "raw"); recipe(graph, "b", "cut", 3, "raw");
        recipe(graph, "dish", "combine", 1, "a", "b");
        assertEquals(4, graph.analyze().get("dish"), 0);
    }
    @Test public void requestedSubsetStillIncludesAllRequiredBranches() {
        PreparationGraph graph = new PreparationGraph(); graph.root("raw");
        recipe(graph, "a", "prepareA", 2, "raw"); recipe(graph, "b", "prepareB", 3, "raw");
        recipe(graph, "dish", "combine", 1, "a", "b");
        recipe(graph, "unrelated", "cycle", 0, "unrelated");
        Map<String, Double> values = graph.analyze(Collections.singleton("dish"));
        assertEquals(6, values.get("dish"), 0); assertEquals(2, values.get("a"), 0);
        assertFalse(values.containsKey("unrelated"));
    }
    @Test public void exactSearchMatchesExhaustiveAssignmentsIncludingCyclesAndSharedBranches() {
        Random random = new Random(177);
        for (int sample = 0; sample < 300; sample++) {
            PreparationGraph graph = new PreparationGraph(); graph.root("raw");
            String[][][] inputs = new String[4][2][]; double[][] weights = new double[4][2];
            for (int node = 0; node < 4; node++) for (int route = 0; route < 2; route++) {
                String[] ingredients = new String[1 + random.nextInt(3)];
                for (int i = 0; i < ingredients.length; i++) {
                    int input = random.nextInt(5); ingredients[i] = input == 4 ? "raw" : "n" + input;
                }
                inputs[node][route] = ingredients; weights[node][route] = random.nextInt(5) * 0.25;
                recipe(graph, "n" + node, "r" + node + ":" + route, weights[node][route], ingredients);
            }
            Map<String, Double> actual = graph.analyze();
            // Enumerate all 16 global recipe assignments independently of search/pruning.
            for (int target = 0; target < 4; target++) {
                double minimum = Double.POSITIVE_INFINITY;
                for (int assignment = 0; assignment < 16; assignment++) {
                    Set<Integer> visited = new HashSet<>();
                    if (!grounded(target, assignment, inputs, new HashSet<>(), visited)) continue;
                    double cost = 0;
                    for (int node : visited) cost += weights[node][(assignment >>> node) & 1];
                    minimum = Math.min(minimum, cost);
                }
                if (Double.isFinite(minimum)) assertEquals("sample " + sample, minimum, actual.get("n" + target), 0);
                else assertFalse("sample " + sample, actual.containsKey("n" + target));
            }
        }
    }
    private boolean grounded(int node, int assignment, String[][][] inputs, Set<Integer> path, Set<Integer> visited) {
        if (path.contains(node)) return false;
        if (visited.contains(node)) return true;
        path.add(node);
        for (String input : inputs[node][(assignment >>> node) & 1]) {
            if (!input.equals("raw") && !grounded(Integer.parseInt(input.substring(1)), assignment, inputs, path, visited)) return false;
        }
        path.remove(node); visited.add(node); return true;
    }
    @Test public void optimizedTagsMatchExhaustiveRoutesWithCyclesAndSharedOperations() {
        Random random = new Random(17720);
        for (int sample = 0; sample < 100; sample++) {
            PreparationGraph graph = new PreparationGraph(); graph.root("raw");
            List<List<Variant>> variants = new ArrayList<>();
            double[] weights = {0, 0.25, 1, 1.5, 3};
            for (int node = 0; node < 4; node++) {
                List<Variant> routes = new ArrayList<>();
                for (int route = 0; route < 2; route++) {
                    int operation = random.nextInt(weights.length);
                    List<List<String>> slots = new ArrayList<>();
                    for (int slot = 0, count = 1 + random.nextInt(2); slot < count; slot++) {
                        List<String> choices = new ArrayList<>();
                        for (int option = 0; option < 2; option++) {
                            int input = random.nextInt(5);
                            choices.add(input == 4 ? "raw" : "n" + input);
                        }
                        slots.add(choices);
                    }
                    graph.add("n" + node, "op" + operation, weights[operation], slots);
                    expandVariants(slots, 0, new ArrayList<>(), operation, routes);
                }
                variants.add(routes);
            }
            double[] expected = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            enumerateVariants(variants, 0, new Variant[4], weights, expected);
            Map<String, Double> actual = graph.analyze();
            for (int target = 0; target < 4; target++) {
                if (Double.isFinite(expected[target])) assertEquals("sample " + sample + " n" + target,
                    expected[target], actual.get("n" + target), 0);
                else assertFalse("sample " + sample, actual.containsKey("n" + target));
            }
        }
    }
    private record Variant(int operation, List<String> inputs) {}
    private void expandVariants(List<List<String>> slots, int slot, List<String> inputs,
                                int operation, List<Variant> variants) {
        if (slot == slots.size()) { variants.add(new Variant(operation, new ArrayList<>(inputs))); return; }
        for (String input : slots.get(slot)) {
            inputs.add(input); expandVariants(slots, slot + 1, inputs, operation, variants);
            inputs.remove(inputs.size() - 1);
        }
    }
    private void enumerateVariants(List<List<Variant>> variants, int node, Variant[] selected,
                                   double[] weights, double[] expected) {
        if (node == selected.length) {
            for (int target = 0; target < selected.length; target++) {
                Set<Integer> operations = new HashSet<>();
                if (!groundedVariant(target, selected, new HashSet<>(), new HashSet<>(), operations)) continue;
                double cost = operations.stream().mapToDouble(operation -> weights[operation]).sum();
                expected[target] = Math.min(expected[target], cost);
            }
            return;
        }
        for (Variant variant : variants.get(node)) {
            selected[node] = variant; enumerateVariants(variants, node + 1, selected, weights, expected);
        }
    }
    private boolean groundedVariant(int node, Variant[] selected, Set<Integer> path,
                                    Set<Integer> visited, Set<Integer> operations) {
        if (path.contains(node)) return false;
        if (visited.contains(node)) return true;
        path.add(node);
        for (String input : selected[node].inputs) if (!input.equals("raw") &&
            !groundedVariant(Integer.parseInt(input.substring(1)), selected, path, visited, operations)) return false;
        path.remove(node); visited.add(node); operations.add(selected[node].operation); return true;
    }
}
