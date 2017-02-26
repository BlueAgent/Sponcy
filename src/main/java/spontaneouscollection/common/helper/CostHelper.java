package spontaneouscollection.common.helper;

public class CostHelper {
    protected final double maxCost, minCost;
    protected double totalCost;

    /**
     * Helps add up costs.
     * minCost must be less than or equal to maxCost (otherwise behaviour is undefined).
     * totalCost is initialised to 0, clamped between minCost and maxCost.
     *
     * @param maxCost the maximum allowable cost.
     * @param minCost the minimum allowable cost.
     */
    public CostHelper(double maxCost, double minCost) {
        this.maxCost = maxCost;
        this.minCost = minCost;
        assert this.minCost <= maxCost;
        this.totalCost = Math.max(minCost, Math.min(maxCost, 0));
    }

    /**
     * Minimum cost of 0.
     *
     * @param maxCost the maximum allowable cost.
     * @see CostHelper#CostHelper(double, double)
     */
    public CostHelper(double maxCost) {
        this(maxCost, 0);
    }

    /**
     * Maximum cost of Double.MAX_VALUE.
     * Minimum cost of 0.
     *
     * @see CostHelper#CostHelper(double, double)
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
        totalCost = cost;
        return true;
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
