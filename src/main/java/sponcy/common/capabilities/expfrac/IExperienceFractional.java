package sponcy.common.capabilities.expfrac;

public interface IExperienceFractional {
    /**
     * Get fractional experience. Should be [0,1).
     *
     * @return Remaining experience
     */
    double getExperienceFraction();

    /**
     * Get total experience. Should be [0, Integer.MAX_VALUE]
     *
     * @return Total experience
     */
    double getExperience();

    /**
     * Maximum experience that can be stored. (Typically Integer.MAX_VALUE)
     *
     * @return Maximum experience.
     */
    double getMaxExperience();

    /**
     * Modify experience. Returns experience stored or taken.
     *
     * @param amount   Amount of experience to add (or remove if negative)
     * @param simulate if True experience change will be simulated
     * @return Resulting change in experience
     */
    double addExperience(double amount, boolean simulate);
}
