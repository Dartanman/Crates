package me.dartanman.crates.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * WeightedRandomList - Similar to a List, but gives each given object a weight.
 * @author Austin Dart (Dartanman)
 */
public class WeightedRandomList<T extends Object> {

	/**
	 * Entry - Just a little class to contain the object you want to use.
	 * @author Austin Dart (Dartanman)
	 */
    private class Entry {
        double accumulatedWeight;
        T object;
    }

    private List<Entry> entries = new ArrayList();
    private double accumulatedWeight;
    private Random rand = new Random();

    /**
     * Add an object to the WeightedRandomList
     * @param object
     *   The object to add
     * @param weight
     *   The weight of the object
     */
    public void addEntry(T object, double weight) {
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }

    /**
     * Get a semi-randomly chosen object from the WeightedRandomList
     * @return
     *   A semi-randomly chosen object
     */
    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry: entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null;
    }
}