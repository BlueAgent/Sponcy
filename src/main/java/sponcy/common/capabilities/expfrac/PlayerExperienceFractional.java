package sponcy.common.capabilities.expfrac;

import net.minecraft.entity.player.EntityPlayer;

import static sponcy.common.helper.ExperienceHelper.levelAtXp;
import static sponcy.common.helper.ExperienceHelper.xpAtLevel;

public class PlayerExperienceFractional implements IExperienceFractional {

    public static final double MAX_EXPERIENCE = (double) Integer.MAX_VALUE + 1;
    private final EntityPlayer player;
    protected double frac;

    public PlayerExperienceFractional(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public double getExperienceFraction() {
        return frac;
    }

    @Override
    public double getExperience() {
        return this.player.experienceTotal + frac;
    }

    @Override
    public double getMaxExperience() {
        return MAX_EXPERIENCE;
    }

    @Override
    public double addExperience(double amount, boolean simulate) {
        double oldExp = player.experienceTotal + frac;
        double newExp = Math.min(Math.max(oldExp + amount, 0), MAX_EXPERIENCE);
        if (!simulate) {
            int playerExp = newExp >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) newExp;
            player.experienceTotal = playerExp;
            player.experienceLevel = levelAtXp(playerExp);
            player.experience = (float) (playerExp - xpAtLevel(player.experienceLevel)) / player.xpBarCap();
            frac = newExp - playerExp;
        }
        return newExp - oldExp;
    }
}
