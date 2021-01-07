package sponcy.common.capabilities.expfrac;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sponcy.Sponcy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExperienceFractionalHandler {

    public static final ResourceLocation KEY = new ResourceLocation(Sponcy.MOD_ID, "expfrac");
    @CapabilityInject(IExperienceFractional.class)
    public static Capability<IExperienceFractional> EXPERIENCE_FRACTIONAL_CAPABILITY;

    public static void register() {
        CapabilityManager.INSTANCE.register(IExperienceFractional.class, new Capability.IStorage<IExperienceFractional>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<IExperienceFractional> capability, IExperienceFractional instance, EnumFacing side) {
                return new NBTTagDouble(instance.getExperience());
            }

            @Override
            public void readNBT(Capability<IExperienceFractional> capability, IExperienceFractional instance, EnumFacing side, NBTBase nbt) {
                if (!(instance instanceof ExperienceFractional))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                ((ExperienceFractional) instance).experience = ((NBTTagDouble) nbt).getDouble();
            }
        }, () -> new ExperienceFractional(Integer.MAX_VALUE));

        MinecraftForge.EVENT_BUS.register(ExperienceFractionalHandler.class);
    }

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof EntityPlayer) {
            final PlayerExperienceFractional inst = new PlayerExperienceFractional((EntityPlayer) e.getObject());
            e.addCapability(KEY, new ICapabilitySerializable<NBTTagDouble>() {
                @Override
                public NBTTagDouble serializeNBT() {
                    return new NBTTagDouble(inst.frac);
                }

                @Override
                public void deserializeNBT(NBTTagDouble nbt) {
                    inst.frac = nbt.getDouble();
                }

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                    return capability == EXPERIENCE_FRACTIONAL_CAPABILITY;
                }

                @Nullable
                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                    return capability == EXPERIENCE_FRACTIONAL_CAPABILITY ? EXPERIENCE_FRACTIONAL_CAPABILITY.<T>cast(inst) : null;
                }
            });
        }
    }
}
