package sponcy.common.capabilities.expfrac;

/**
 * Reference implementation for fractional experience storage.
 * Really it should be split between the player's experience level (int) and the capability storage
 */
public class ExperienceFractional implements IExperienceFractional {

    //public static final double MAX_EXP_WARN = 2L << 32; // More like 2^53 but then that leaves none for the fractional portion
    private final double maxExperience;
    protected double experience;

    /**
     * Recommended to not be more than Integer.MAX_VALUE
     *
     * @param maxExperience max amount of experience
     */
    public ExperienceFractional(double maxExperience) {
        this.maxExperience = maxExperience;
    }

    @Override
    public double getExperienceFraction() {
        return experience - Math.floor(experience);
    }

    @Override
    public double getExperience() {
        return experience;
    }

    @Override
    public double getMaxExperience() {
        return maxExperience;
    }

    @Override
    public double addExperience(double amount, boolean simulate) {
        double oldExp = experience;
        double newExp = Math.min(Math.max(oldExp + amount, 0), maxExperience);
        if (!simulate) {
            experience = newExp;
        }
        return newExp - oldExp;
    }
}
