package com.xy177.solwarly.diet;

import java.util.*;

/** Exact minimum-cost acyclic preparation; shared operations are counted once. */
public final class PreparationGraph {
    private final Map<String, List<Recipe>> recipes = new LinkedHashMap<>();
    private final Set<String> roots = new HashSet<>();
    public void root(String item) { roots.add(item); }
    public void add(String output, String id, double weight, List<List<String>> ingredients) {
        // Identical ingredient slots can use the same route; quantities do not add difficulty.
        Set<List<String>> distinct = new LinkedHashSet<>();
        for (List<String> options : ingredients) distinct.add(new ArrayList<>(new TreeSet<>(options)));
        recipes.computeIfAbsent(output, key -> new ArrayList<>()).add(new Recipe(output, id, weight, new ArrayList<>(distinct)));
    }
    public Map<String, Double> analyze() { return analyze(recipes.keySet()); }
    public Map<String, Double> analyze(Collection<String> targets) {
        Set<String> needed = new LinkedHashSet<>(targets);
        Deque<String> pending = new ArrayDeque<>(targets);
        while (!pending.isEmpty()) {
            for (Recipe recipe : recipes.getOrDefault(pending.removeFirst(), Collections.emptyList()))
                for (List<String> options : recipe.ingredients) for (String input : options)
                    if (needed.add(input)) pending.addLast(input);
        }
        List<Recipe> relevant = new ArrayList<>();
        for (String output : needed) relevant.addAll(recipes.getOrDefault(output, Collections.emptyList()));

        // First identify grounded recipes, without expanding any alternative route sets.
        Set<String> reachable = new HashSet<>(roots);
        for (Recipe recipe : relevant)
            for (List<String> options : recipe.ingredients) for (String input : options)
                if (!recipes.containsKey(input)) reachable.add(input);
        boolean changed;
        do {
            changed = false;
            for (Recipe recipe : relevant)
                if (recipe.ingredients.stream().allMatch(options -> options.stream().anyMatch(reachable::contains)))
                    changed |= reachable.add(recipe.output);
        } while (changed);

        Map<String, Double> values = new HashMap<>();
        for (String root : roots) values.put(root, 0.0);
        for (String item : needed) if (reachable.contains(item)) {
            Search search = new Search(reachable);
            search.visit(new LinkedHashSet<>(Collections.singleton(item)), new HashMap<>(), new HashSet<>(), 0);
            if (Double.isFinite(search.best)) values.put(item, search.best);
        }
        return values;
    }
    public Set<String> outputs() { return new HashSet<>(recipes.keySet()); }

    /** Search complete routes instead of storing every interchangeable intermediate route. */
    private final class Search {
        final Set<String> reachable;
        double best = Double.POSITIVE_INFINITY;
        Search(Set<String> reachable) { this.reachable = reachable; }
        void visit(LinkedHashSet<String> pending, Map<String, List<String>> chosen, Set<String> operations, double cost) {
            // Only prune with a complete route's bound. An ingredient's cheapest route is
            // not a safe bound: a more expensive one may share operations with another branch.
            if (cost >= best) return;
            pending.removeIf(item -> roots.contains(item) || !recipes.containsKey(item) || chosen.containsKey(item));
            if (pending.isEmpty()) { best = cost; return; }
            String item = pending.iterator().next(); pending.remove(item);
            for (Recipe recipe : recipes.get(item)) {
                double nextCost = cost + (operations.contains(recipe.id) ? 0 : recipe.weight);
                if (nextCost >= best) continue;
                boolean added = operations.add(recipe.id);
                chooseInputs(recipe, 0, new ArrayList<>(), pending, chosen, operations, nextCost);
                if (added) operations.remove(recipe.id);
            }
        }
        void chooseInputs(Recipe recipe, int slot, List<String> inputs, LinkedHashSet<String> pending,
                          Map<String, List<String>> chosen, Set<String> operations, double cost) {
            if (slot == recipe.ingredients.size()) {
                // Each product needs one route. Combining extra routes for that same
                // product cannot lower the union cost of nonnegative operations.
                chosen.put(recipe.output, new ArrayList<>(inputs));
                LinkedHashSet<String> next = new LinkedHashSet<>(pending); next.addAll(inputs);
                visit(next, chosen, operations, cost);
                chosen.remove(recipe.output);
                return;
            }
            for (String input : recipe.ingredients.get(slot)) {
                if (!reachable.contains(input) || dependsOn(input, recipe.output, chosen, new HashSet<>())) continue;
                inputs.add(input);
                chooseInputs(recipe, slot + 1, inputs, pending, chosen, operations, cost);
                inputs.remove(inputs.size() - 1);
            }
        }
    }
    private static boolean dependsOn(String input, String output, Map<String, List<String>> chosen, Set<String> seen) {
        if (input.equals(output)) return true;
        if (!seen.add(input)) return false;
        for (String ancestor : chosen.getOrDefault(input, Collections.emptyList()))
            if (dependsOn(ancestor, output, chosen, seen)) return true;
        return false;
    }
    private static final class Recipe {
        final String output, id;
        final double weight;
        final List<List<String>> ingredients;
        Recipe(String output, String id, double weight, List<List<String>> ingredients) {
            this.output = output; this.id = id; this.weight = weight; this.ingredients = ingredients;
        }
    }
}
