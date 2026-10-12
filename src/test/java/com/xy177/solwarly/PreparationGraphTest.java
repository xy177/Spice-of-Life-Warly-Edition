package com.xy177.solwarly;

import com.xy177.solwarly.diet.PreparationGraph;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class PreparationGraphTest {
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
}
