package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

import java.util.Random;

public class NewPathfinderGoalSummonLightningRandomly extends PathfinderGoal {

    private final EntityInsentient entity;
    private final World nmsWorld;
    private final org.bukkit.World bukkitWorld;
    private final double chanceMultiplier;
    private Location loc;
    private static final Random random = new Random();

    public NewPathfinderGoalSummonLightningRandomly(EntityInsentient entity, double chanceMultiplier) {
        this.entity = entity;
        this.nmsWorld = entity.getWorld();
        this.bukkitWorld = this.nmsWorld.getWorld();
        this.chanceMultiplier = chanceMultiplier;
    }

    @Override
    public boolean a() {
        if (this.entity.world.isRainingAt(new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ())) && random.nextDouble() < 0.0001 * (this.chanceMultiplier)) {
            return true;
        } else {
            return random.nextDouble() < 0.000025 * (this.chanceMultiplier);
        }
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        this.loc = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.entity.locX(), this.entity.locY(), this.entity.locZ()), random.nextDouble() * 50.0, this.entity.locY(), 361.0);
        new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, this.loc, false);
    }
}
