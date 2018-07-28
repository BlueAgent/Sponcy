package sponcy.common.helper;

public class CostHelper {
    protected final double maxCost, minCost;
    protected double totalCost;

    /**
     * Helps add up costs.
     * minCost must be less than or equal to maxCost (otherwise it will swap them).
     * totalCost is clamped between minCost and maxCost.
     *
     * @param maxCost   the maximum allowable cost.
     * @param minCost   the minimum allowable cost.
     * @param totalCost this starting value of the cost.
     */
    public CostHelper(double maxCost, double minCost, double totalCost) {
        assert minCost <= maxCost;
        if (minCost <= maxCost) {
            this.maxCost = maxCost;
            this.minCost = minCost;
        } else {
            this.maxCost = minCost;
            this.minCost = maxCost;
        }
        assert totalCost <= this.maxCost;
        assert totalCost >= this.minCost;
        this.totalCost = Math.max(this.minCost, Math.min(this.maxCost, totalCost));
    }

    /**
     * Starting cost equal to minCost.
     *
     * @param maxCost the maximum allowable cost.
     * @param minCost the minimum allowable cost.
     * @see CostHelper#CostHelper(double, double, double)
     */
    public CostHelper(double maxCost, double minCost) {
        this(maxCost, minCost, minCost);
    }

    /**
     * Minimum cost of 0.
     * Starting cost of 0.
     *
     * @param maxCost the maximum allowable cost.
     * @see CostHelper#CostHelper(double, double, double)
     */
    public CostHelper(double maxCost) {
        this(maxCost, 0);
    }

    /**
     * Maximum cost of Double.MAX_VALUE.
     * Minimum cost of 0.
     * Starting cost of 0.
     *
     * @see CostHelper#CostHelper(double, double, double)
     */
    public CostHelper() {
        this(Double.MAX_VALUE);
    }

    /**
     * Add a cost (can be negative)
     *
     * @param cost to add to total
     * @return true iff successfully added
     */
    public boolean add(double cost) {
        double newCost = totalCost + cost;
        if (newCost > maxCost) return false;
        if (newCost < minCost) return false;
        totalCost = newCost;
        return true;
    }

    /**
     * Set the cost clamped between max and min
     *
     * @param cost to set.
     * @return this.
     */
    public CostHelper set(double cost) {
        totalCost = Math.max(minCost, Math.min(maxCost, cost));
        return this;
    }

    public double getTotal() {
        return totalCost;
    }

    /**
     * Amount to reach max cost
     *
     * @return Max Cost - Total Cost
     */
    public double getToMax() {
        return maxCost - totalCost;
    }

    /**
     * Amount to reach min cost
     *
     * @return Total Cost - Min Cost
     */
    public double getToMin() {
        return totalCost - minCost;
    }
}
