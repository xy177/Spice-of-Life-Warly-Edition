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
        return analyze(targets, true, item -> {});
    }
    /** Only requested products need exact scores when filling the food configuration. */
    public Map<String, Double> analyzeTargets(Collection<String> targets, java.util.function.Consumer<String> progress) {
        return analyze(targets, false, progress);
    }
    private Map<String, Double> analyze(Collection<String> targets, boolean includeIngredients,
                                        java.util.function.Consumer<String> progress) {
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

        Map<String, List<Recipe>> prepared = new HashMap<>();
        for (Recipe recipe : relevant) {
            List<List<String>> slots = new ArrayList<>();
            boolean possible = true;
            for (List<String> options : recipe.ingredients) {
                // A naturally available ingredient satisfies this slot without any
                // operation or cycle. Other alternatives cannot improve that choice.
                if (options.stream().anyMatch(input -> roots.contains(input) || !recipes.containsKey(input))) continue;
                List<String> choices = options.stream()
                    .filter(input -> reachable.contains(input) && !input.equals(recipe.output)).toList();
                if (choices.isEmpty()) { possible = false; break; }
                // Satisfying a smaller option set also satisfies each of its supersets.
                if (slots.stream().anyMatch(choices::containsAll)) continue;
                slots.removeIf(existing -> existing.containsAll(choices));
                slots.add(choices);
            }
            if (possible) prepared.computeIfAbsent(recipe.output, key -> new ArrayList<>())
                .add(new Recipe(recipe.output, recipe.id, recipe.weight, slots));
        }
        prepared.values().forEach(list -> list.sort(Comparator.comparingDouble(recipe -> recipe.weight)));

        Map<String, Double> values = new HashMap<>();
        for (String root : roots) values.put(root, 0.0);
        for (String item : includeIngredients ? needed : new LinkedHashSet<>(targets)) if (reachable.contains(item)) {
            progress.accept(item);
            Search search = new Search(reachable, prepared);
            search.visit(new LinkedHashSet<>(Collections.singleton(item)), new HashMap<>(), new HashSet<>(), 0);
            if (Double.isFinite(search.best)) values.put(item, search.best);
        }
        return values;
    }
    public Set<String> outputs() { return new HashSet<>(recipes.keySet()); }

    /** Search complete routes instead of storing every interchangeable intermediate route. */
    private final class Search {
        final Set<String> reachable;
        final Map<String, List<Recipe>> prepared;
        double best = Double.POSITIVE_INFINITY;
        Search(Set<String> reachable, Map<String, List<Recipe>> prepared) {
            this.reachable = reachable; this.prepared = prepared;
        }
        void visit(LinkedHashSet<String> pending, Map<String, List<String>> chosen, Set<String> operations, double cost) {
            // Only prune with a complete route's bound. An ingredient's cheapest route is
            // not a safe bound: a more expensive one may share operations with another branch.
            if (cost >= best) return;
            pending.removeIf(item -> roots.contains(item) || !recipes.containsKey(item) || chosen.containsKey(item));
            if (pending.isEmpty()) { best = cost; return; }
            if (Double.isFinite(best) && cost + remainingCost(pending.stream().map(List::of).toList(), chosen, operations) >= best) return;
            String item = pending.iterator().next(); pending.remove(item);
            for (Recipe recipe : prepared.getOrDefault(item, Collections.emptyList())) {
                double nextCost = cost + (operations.contains(recipe.id) ? 0 : recipe.weight);
                if (nextCost >= best) continue;
                boolean added = operations.add(recipe.id);
                chooseInputs(recipe, 0, new ArrayList<>(), pending, chosen, operations, nextCost);
                if (added) operations.remove(recipe.id);
            }
        }
        void chooseInputs(Recipe recipe, int slot, List<String> inputs, LinkedHashSet<String> pending,
                          Map<String, List<String>> chosen, Set<String> operations, double cost) {
            // The bound can improve inside an earlier combination. Check it before
            // expanding the remaining Cartesian product, not only after all slots.
            if (cost >= best) return;
            if (Double.isFinite(best)) {
                List<List<String>> requirements = new ArrayList<>();
                pending.forEach(item -> requirements.add(List.of(item)));
                inputs.forEach(item -> requirements.add(List.of(item)));
                requirements.addAll(recipe.ingredients.subList(slot, recipe.ingredients.size()));
                if (cost + remainingCost(requirements, chosen, operations) >= best) return;
            }
            if (slot == recipe.ingredients.size()) {
                // Each product needs one route. Combining extra routes for that same
                // product cannot lower the union cost of nonnegative operations.
                chosen.put(recipe.output, new ArrayList<>(inputs));
                LinkedHashSet<String> next = new LinkedHashSet<>(pending); next.addAll(inputs);
                visit(next, chosen, operations, cost);
                chosen.remove(recipe.output);
                return;
            }
            for (String input : inputs) if (recipe.ingredients.get(slot).contains(input)) {
                chooseInputs(recipe, slot + 1, inputs, pending, chosen, operations, cost);
                return;
            }
            for (String input : recipe.ingredients.get(slot)) {
                if (!reachable.contains(input) || dependsOn(input, recipe.output, chosen, new HashSet<>())) continue;
                inputs.add(input);
                chooseInputs(recipe, slot + 1, inputs, pending, chosen, operations, cost);
                inputs.remove(inputs.size() - 1);
            }
        }
        /** Distinct groups cannot pay for the same direct operation. Within a group
         * use only its largest minimum, preserving multi-output and shared routes. */
        double remainingCost(List<List<String>> requirements, Map<String, List<String>> chosen, Set<String> operations) {
            int size = requirements.size();
            int[] groups = new int[size];
            double[] minima = new double[size];
            Map<String, Integer> owners = new HashMap<>();
            for (int slot = 0; slot < size; slot++) {
                groups[slot] = slot; minima[slot] = Double.POSITIVE_INFINITY;
                for (String input : requirements.get(slot)) {
                    if (roots.contains(input) || !recipes.containsKey(input) || chosen.containsKey(input)) {
                        minima[slot] = 0; continue;
                    }
                    for (Recipe option : prepared.getOrDefault(input, Collections.emptyList())) {
                        minima[slot] = Math.min(minima[slot], operations.contains(option.id) ? 0 : option.weight);
                        Integer owner = owners.putIfAbsent(option.id, slot);
                        if (owner != null) groups[group(groups, slot)] = group(groups, owner);
                    }
                }
            }
            double[] costs = new double[size];
            for (int slot = 0; slot < size; slot++) {
                int group = group(groups, slot);
                costs[group] = Math.max(costs[group], minima[slot]);
            }
            double result = 0;
            for (double value : costs) result += value;
            return result;
        }
        int group(int[] groups, int slot) {
            while (groups[slot] != slot) { groups[slot] = groups[groups[slot]]; slot = groups[slot]; }
            return slot;
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
