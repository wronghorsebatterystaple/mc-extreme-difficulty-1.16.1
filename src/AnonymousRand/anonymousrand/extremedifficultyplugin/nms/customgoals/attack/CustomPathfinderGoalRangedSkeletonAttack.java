package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.*;

// originally PathfinderGoalBowShoot
/**
 * A subclass of <code>CustomPathfinderGoalRangedHandheldAttack</code> that handles skeleton strafing movement,
 * analogous to <code>PathfinderGoalBowShoot</code>.
 */
public class CustomPathfinderGoalRangedSkeletonAttack<T extends EntityInsentient & IRangedEntity
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalRangedHandheldAttack<T> {

    protected boolean strafingBackwards;
    protected boolean strafingClockwise;
    protected int strafingTime;

    public CustomPathfinderGoalRangedSkeletonAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, attackCooldown, 1.0);
    }

    public CustomPathfinderGoalRangedSkeletonAttack(T goalOwner, int attackCooldown, double speedTowardsTarget) {
        super(goalOwner, Items.BOW, attackCooldown, speedTowardsTarget);
    }

    @Override
    public void startExecutingMovement() {
        super.startExecutingMovement();

        this.strafingBackwards = false;
        this.strafingClockwise = false;
        /* Skeletons and illusioners start strafing immediately, and don't need line of sight to do so */
        this.strafingTime = 0;
    }

    @Override
    public void tickMovement(EntityLiving goalTarget) {
        super.tickMovement(goalTarget);
        this.strafingTime++;

        /* Skeletons and illusioners try to maintain a distance from their targets equal to 50% of their detection
           range */
        this.strafingBackwards = !(NmsUtil.distSq(this.goalOwner, goalTarget, true) > this.getDetectionRangeSq() * 0.5);
        /* Skeletons and illusioners have an increased frequency of switching strafe rotation: every 10 ticks of
           strafing, they have a 50% chance to switch the rotation direction */
        if (this.strafingTime % 10 == 0) {
            if (this.goalOwner.getRandom().nextDouble() < 0.5) {
                this.strafingClockwise = !this.strafingClockwise;
            }
        }

        // apply strafe
        /* Skeletons and illusioners strafe in circles much faster than they do forward/backward */
        this.goalOwner.getControllerMove().a(this.strafingBackwards ? -0.2F : 0.2F, // strafe(); largely idempotent
                this.strafingClockwise ? 200F : -200F);
        this.goalOwner.a(goalTarget, 30.0F, 30.0F); // faceEntity(); this.goalOwner.getControllerLook().a() doesn't work
    }

    @Override
    protected boolean checkAttack(EntityLiving goalTarget) {
        return this.goalOwner.isHandRaised();
    }

    @Override
    protected void attack(EntityLiving goalTarget) {
        // ItemBow.a() gets the attack power for a corresponding charge of the bow in ticks (we are manually setting it
        // to the normal 20 here to allow rapid fire, to mimic having charged the bow for the full 20 ticks already)
        this.goalOwner.a(goalTarget, ItemBow.a(20)); // shoot()
    }

    protected double getDetectionRangeSq() {
        return this.goalOwner.getDetectionRange() * this.goalOwner.getDetectionRange();
    }
}
