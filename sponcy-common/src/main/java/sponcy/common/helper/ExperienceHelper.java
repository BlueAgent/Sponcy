package sponcy.common.helper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import sponcy.common.capabilities.expfrac.IExperienceFractional;

public class ExperienceHelper {

    //Solve: (9 * n * n - 325 * n + 4440) / 2 = 2^31 - 1
    //Result: 1/18 (325 + Sqrt[154618768369]) ~= 21863.385053916877105759
    //     ~= 21863
    public static final int MAX_LEVEL = 21863;
    public static final int MAX_LEVEL_EXP = xpAtLevel(MAX_LEVEL); //2147407943
    public static final int EXP_LEVEL_1 = xpAtLevel(1); //7
    public static final int TYPICAL_LEVEL = 3000;
    public static final int TYPICAL_LEVEL_XP = xpAtLevel(3000); //40014720

    @CapabilityInject(IExperienceFractional.class)
    public static Capability<IExperienceFractional> EXPERIENCE_FRACTIONAL_CAPABILITY;

    /**
     * Get the total experience of the player.
     *
     * @param player target player
     * @return total fractional experience
     */
    public static double getXp(EntityPlayer player) {
        IExperienceFractional inst = (IExperienceFractional) player.getCapability(EXPERIENCE_FRACTIONAL_CAPABILITY, null);
        assert inst != null;
        return inst.getExperience();
    }

    /**
     * Adds or removes experience from the player, checks bounds.
     *
     * @param player   to add or take exp from.
     * @param amount   positive to add, negative to take.
     * @param simulate If true, exp change will only be simulated
     * @return the actual change in experience points.
     */
    public static double addXp(EntityPlayer player, double amount, boolean simulate) {
        IExperienceFractional inst = (IExperienceFractional) player.getCapability(EXPERIENCE_FRACTIONAL_CAPABILITY, null);
        assert inst != null;
        return inst.addExperience(amount, simulate);
    }

    /**
     * Adds or removes experience from the player, checks bounds.
     *
     * @param player   to add or take exp from.
     * @param amount   positive to add, negative to take.
     * @param simulate If true, exp change will only be simulated
     * @return the actual change in experience points.
     */
    public static int addXp(EntityPlayer player, int amount, boolean simulate) {
        int maxIncrease = Integer.MAX_VALUE - player.experienceTotal;
        if (maxIncrease < amount)
            amount = maxIncrease;

        int maxDecrease = -player.experienceTotal;
        if (amount < maxDecrease)
            amount = maxDecrease;

        int total = player.experienceTotal + amount;
        if (!simulate) {
            player.experienceTotal = total;
            player.experienceLevel = levelAtXp(total);
            player.experience = (float) (total - xpAtLevel(player.experienceLevel)) / player.xpBarCap();
        }
        return amount;
    }

    public static float getXpRepairRatio(ItemStack itemStack) {
        return 2f;
    }

    /**
     * Returns the total experience needed to reach the level.
     * Calculated from summation of arithmetic series of {@see net.minecraft.entity.player.EntityPlayer#xpBarCap()}.
     * Maximum level is 21863 with an experience of 2147407943.
     * O(1)
     *
     * @param level to achieve.
     * @return experience to reach desired level.
     */
    public static int xpAtLevel(int level) {
        if (level <= 0)
            return 0;
        if (level > MAX_LEVEL)
            return MAX_LEVEL_EXP;

        //For level < 15
        //S(1) = 7
        //S(2) = 7 + 9 = 16
        //S(3) = 16 + 11 = 27
        //S(n) = a*n^2 + b*n + c
        //Solving
        //[1 1 1][a]   [7 ]
        //[4 2 1][b] = [16]
        //[9 3 1][c]   [27]
        //Co-factor
        //[-1  5  -6]
        //[ 2  -8  6]
        //[-1  3  -2]
        //det = -1 + 5 - 6 = -2 (has solution)
        //Transpose
        //[-1  2  -1]
        //[ 5  -8  3]
        //[-6  6  -2]
        //1/det
        //[ 0.5 -1   0.5]
        //[-2.5  4  -1.5]
        //[ 3   -3   1  ]
        //Multiply that by [7,16,27]
        //Result: n^2 + 6n
        if (level < 15) return level * level + 6 * level;

        //Similarly
        //[225 15 1][a]   [315]
        //[256 16 1][b] = [252]
        //[289 17 1][c]   [394]
        //Result: 5/2 n^2 -81/2 n + 360
        if (level < 30) return (5 * level * level - 81 * level + 720) / 2;

        //[900  30 1][a]   [1395]
        //[961  31 1][b] = [1507]
        //[1024 32 1][c]   [1628]
        //Result: 9/2 n^2 -325/2 n + 2220
        long n = level; //int may overflow otherwise
        return (int) ((9 * n * n - 325 * n + 4440) / 2);
    }

    /**
     * Finds the level (truncated) that corresponds to the experience value.
     * Maximum experience is 2147407943 with a level of 21863.
     * O(log(N)): typically 12 (max 31) cycles to converge.
     *
     * @param xp
     * @return
     */
    public static int levelAtXp(int xp) {
        if (xp < EXP_LEVEL_1)
            return 0;
        if (xp >= MAX_LEVEL_EXP)
            return MAX_LEVEL;

        int lo = 0;
        int hi = xp < TYPICAL_LEVEL_XP ? TYPICAL_LEVEL : MAX_LEVEL;
        int mid, xpAtLvl;
        //Terminates when lo and hi converges
        while (hi - lo > 1) {
            mid = (lo + hi) / 2;
            xpAtLvl = xpAtLevel(mid);

            if (xpAtLvl > xp) { //If guess was too high, lower the upper bound
                hi = mid;
            } else { //If guess was too low, raise the lower bound
                lo = mid;
            }
        }
        return lo;
    }
}
