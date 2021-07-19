package draylar.identity;

import draylar.identity.ability.AbilityRegistry;
import draylar.identity.config.IdentityConfig;
import draylar.identity.network.ServerNetworking;
import draylar.identity.registry.Commands;
import draylar.identity.registry.Components;
import draylar.identity.registry.EntityTags;
import draylar.identity.registry.EventHandlers;
import draylar.omegaconfig.OmegaConfig;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class Identity implements ModInitializer {

    public static final IdentityConfig CONFIG = OmegaConfig.register(IdentityConfig.class);
    public static final AbilitySource ABILITY_SOURCE = Pal.getAbilitySource(id("equipped_identity"));

    public static final Object2FloatMap<Identifier> MAX_HEALTH_OVERRIDES = new Object2FloatOpenHashMap<>();
    static {
        MAX_HEALTH_OVERRIDES.put(new Identifier("adventurez:red_fungus"), 6f);
        MAX_HEALTH_OVERRIDES.put(new Identifier("minecraft:snow_golem"), 6f);
        MAX_HEALTH_OVERRIDES.put(new Identifier("guardiansgalore:boar"), 16f);
        MAX_HEALTH_OVERRIDES.put(new Identifier("biomemakeover:toad"), 8f);
    }

    @Override
    public void onInitialize() {
        EntityTags.init();
        AbilityRegistry.init();
        EventHandlers.init();
        Commands.init();
        ServerNetworking.init();
    }

    public static Identifier id(String name) {
        return new Identifier("identity", name);
    }

    public static boolean hasFlyingPermissions(ServerPlayerEntity player) {
        LivingEntity identity = Components.CURRENT_IDENTITY.get(player).getIdentity();

        if(identity != null && Identity.CONFIG.enableFlight && EntityTags.FLYING.contains(identity.getType())) {
            List<String> requiredAdvancements = CONFIG.advancementsRequiredForFlight;

            // requires at least 1 advancement, check if player has them
            if (!requiredAdvancements.isEmpty()) {

                boolean hasPermission = true;
                for (String requiredAdvancement : requiredAdvancements) {
                    Advancement advancement = player.server.getAdvancementLoader().get(new Identifier(requiredAdvancement));
                    AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);

                    if (!progress.isDone()) {
                        hasPermission = false;
                    }
                }

                return hasPermission;
            }


            return true;
        }

        return false;
    }

    public static boolean isAquatic(LivingEntity entity) {
        return entity instanceof WaterCreatureEntity || entity instanceof GuardianEntity;
    }
}
