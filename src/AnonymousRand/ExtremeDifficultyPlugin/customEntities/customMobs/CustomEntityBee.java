package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.*;

public class CustomEntityBee extends EntityBee {

    public CustomEntityBee(World world) {
        super(EntityTypes.BEE, world);
        for (Field f : EntityBee.class.getDeclaredFields()) {
            f.setAccessible(true);
        }
    }

    @Override
    public void tick() {
        super.tick();

        /**always aggro at players within 12 blocks*/
        EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(12.0, 128.0, 12.0));
        if (player != null && this.getGoalTarget() != null) {
            this.setGoalTarget(player);
        }

        if (this.ticksLived == 10) { /**bees do 1000 damage but only have 5 health*/
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1000.0);
            this.setHealth(5.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(5.0);
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        Location thisLoc2 = new Location(this.getWorld().getWorld(), this.locX(), this.locY() + 1.0, this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB || thisLoc2.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
        }

        if (this.hasStung()) {
            this.setHasStung(false); /**bees don't die from stinging*/

            CustomEntityBee newBee = new CustomEntityBee(this.getWorld()); /**duplicates when stinging*/
            newBee.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            this.getWorld().addEntity(newBee, CreatureSpawnEvent.SpawnReason.NATURAL);
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
