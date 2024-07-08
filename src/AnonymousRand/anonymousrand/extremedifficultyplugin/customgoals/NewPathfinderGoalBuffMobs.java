package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import net.minecraft.server.v1_16_R1.*;

import java.util.*;

public class NewPathfinderGoalBuffMobs<T extends EntityInsentient & IAttackLevelingMob> extends PathfinderGoal {

    public T entity;
    private final Class<? extends EntityLiving> targetClass;
    private final HashMap<Integer, ArrayList<MobEffect>> attacksAndEffects;
    private final double rangeRadius;
    private final int attacksThreshold, ticksDelayMin, ticksDelayRandBound;
    private static final Random random = new Random();

    public NewPathfinderGoalBuffMobs(T entity, Class<? extends EntityLiving> targetClass, HashMap<Integer, ArrayList<MobEffect>> attacksAndEffects, double rangeRadius, int attacksThreshold, int ticksDelayMin, int ticksDelayRandBound) {
        this.entity = entity;
        this.targetClass = targetClass;
        this.attacksAndEffects = attacksAndEffects;
        this.rangeRadius = rangeRadius;
        this.attacksThreshold = attacksThreshold;
        this.ticksDelayMin = ticksDelayMin;
        this.ticksDelayRandBound = ticksDelayRandBound;
    }

    @Override
    public boolean a() {
        if (this.entity.ticksLived % (random.nextInt(this.ticksDelayRandBound) + this.ticksDelayMin) == 0) {
            if (this.entity.getAttacks() >= this.attacksThreshold) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        int attacksLocal = this.entity.getAttacks();
        this.entity.getWorld().getEntities(this.entity, this.entity.getBoundingBox().g(this.rangeRadius), this.targetClass::isInstance).forEach(entity -> {
            if (entity instanceof EntityPlayer || this.normalGetDistSq(this.entity.getPositionVector(), entity.getPositionVector()) > Math.pow(this.rangeRadius, 2)) { // ensures that the entities is in a sphere around the mob and not a cube
                return;
            }

            for (Map.Entry<Integer, ArrayList<MobEffect>> entry : this.attacksAndEffects.entrySet()) {
                if (attacksLocal >= entry.getKey()) { // entry.getKey is the integer in the hashmap entry
                    for (MobEffect effect : entry.getValue()) { // entry.getValue is the arraylist of mobeffects in the hashmap entry
                        ((EntityLiving) entity).addEffect(effect);
                    }
                }
            }
        });
    }

    public double normalGetDistSq(Vec3D vec3d1, Vec3D vec3d2) { // todo why here? use mobs/util if eventually converted?
        double d0 = vec3d2.getX() - vec3d1.getX();
        double d1 = vec3d2.getY() - vec3d1.getY();
        double d2 = vec3d2.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}