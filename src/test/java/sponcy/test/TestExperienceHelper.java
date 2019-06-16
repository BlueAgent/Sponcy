package sponcy.test;

import org.junit.Test;
import sponcy.common.helper.ExperienceHelper;

import static org.junit.Assert.assertEquals;

public class TestExperienceHelper {

    /**
     * Gets the max size of the experience bar for the given level
     * @param level Player level
     * @return Max size of experience bar
     * @see net.minecraft.entity.player.EntityPlayer#xpBarCap()
     */
    public static int xpBarCap(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            if (level >= 15) {
                return 37 + (level - 15) * 5;
            } else {
                return 7 + level * 2;
            }
        }
    }

    @Test
    public void testXpSummation() {
        int currentSum = 0;
        for (int level = 0; level <= ExperienceHelper.TYPICAL_LEVEL; level++) {
            assertEquals("Total EXP to level " + level + " equals " + currentSum, currentSum, ExperienceHelper.xpAtLevel(level));
            currentSum += xpBarCap(level);
        }
    }

    @Test
    public void testXpReverse() {
        int currentSum = 0;
        int nextSum = 0;
        for (int level = 0; level <= ExperienceHelper.TYPICAL_LEVEL; level++) {
            nextSum += xpBarCap(level);
            for (int xp = currentSum; xp < nextSum; xp++) {
                assertEquals(xp + " EXP should reverse to " + level, level, ExperienceHelper.levelAtXp(xp));
            }
            currentSum = nextSum;
        }
    }

}
