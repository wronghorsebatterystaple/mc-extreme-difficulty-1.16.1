package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntitySpider extends EntitySpider {

    public int attacks;
    private boolean a25, a50, a70, a100;

    public CustomEntitySpider(World world) {
        super(EntityTypes.SPIDER, world);
        this.teleportToPlayer = 0;
        this.attacks = 0;
        this.a25 = false;
        this.a50 = false;
        this.a70 = false;
        this.a100 = false;
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(3, new CustomPathfinderGoalMeleeAttack(this, 1.0, false)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/ //false for the long memory parameter allows the spider to continue attacking instead of only hitting you once
        this.goalSelector.a(2, new NewPathfinderGoalBreakBlocksAround(this, 100, 1, 0, 1, 0, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
    }

    protected int teleportToPlayer;
    protected CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) { /**spiders have +70% movement speed but only 1 damage and 12 health*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.51);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0);
            this.setHealth(12.0f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.0);
        }

        if (this.attacks % 24 == 0 && this.a25) { //reset right before the next cycle
            this.a25 = false;
        }

        if (this.attacks % 25 == 0 && this.attacks != 0 && !this.a25) {
            this.a25 = true;

            Location loc;
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        loc = new Location(this.getWorld().getWorld(), Math.floor(this.locX()) + x, Math.floor(this.locY()) + y, Math.floor(this.locZ()) + z);
                        if (loc.getBlock().getType() == org.bukkit.Material.AIR) { /**every 25 attacks, spiders lay down cobwebs that last 10 seconds in a 5 by 5 cube around itself*/
                            loc.getBlock().setType(org.bukkit.Material.COBWEB);
                            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(loc.getBlock(), loc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 10 seconds
                        }
                    }
                }
            }
        }

        if (this.attacks == 50 && !this.a50) { /**summons 3 vanilla cave spiders on the 50th attack*/
            this.a50 = true;
            EntityCaveSpider caveSpider;
            for (int i = 0; i < 3; i++) {
                caveSpider = new EntityCaveSpider(EntityTypes.CAVE_SPIDER, this.getWorld());
                caveSpider.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(caveSpider, CreatureSpawnEvent.SpawnReason.DROWNED);
            }
        }

        if (this.attacks == 70 && !this.a70) { /**gains speed 1 after the 70th attack*/
            this.a70 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
        }

        if (this.attacks == 100 && !this.a100) { /**summons 5 cave spiders on the 100th attack*/
            this.a100 = true;
            CustomEntitySpiderCave caveSpider;
            for (int i = 0; i < 6; i++) {
                caveSpider = new CustomEntitySpiderCave(this.getWorld());
                caveSpider.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
                this.getWorld().addEntity(caveSpider, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.AIR) { /**spiders lay down cobwebs that last 10 seconds on itself as long as it is inside an air block*/ //cobwebs also indirectly prevent players from shooting arrows onto the spider as the arrows are blocked by the web hitbox
            thisLoc.getBlock().setType(org.bukkit.Material.COBWEB);
            Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(thisLoc.getBlock(), thisLoc.getBlock().getState(), null, null, null, false, null)); //fire event that would otherwise not be fired so that the cobweb block can be broken after 10 seconds
        }

        if (this.getGoalTarget() == null) { //does not see a target within follow range
            this.teleportToPlayer++;
        } else {
            this.teleportToPlayer = 0;
        }

        if (this.teleportToPlayer > 300) { /**has a 0.5% chance every tick to teleport to within follow_range-2 to follow_range+11 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
            if (random.nextDouble() < 0.005) {
                this.initiateTeleport(random.nextDouble() * 13.0 + this.b(GenericAttributes.FOLLOW_RANGE) - 2.0);
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); //mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
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

    protected void initiateTeleport(double h) {
        double hypo = h;
        EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(128.0, 128.0, 128.0)); //get closest player within 128 sphere radius of this

        if (player != null) {
            BlockPosition pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(new BlockPosition(player.locX(), player.locY(), player.locZ()), hypo, this.locY() + 2.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to teleport to (so possible teleport area is a washer-like disc around the player)
            BlockPosition pos2 = this.getWorld().getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, pos); //highest block at those coords

            if (pos2 != null && pos2.getY() < 128.0) { //teleport to highest block if there is one in that location
                this.teleportTo(pos2);
            } else { //clear out 5 by 5 by 5 area around teleport destination before teleporting there
                this.initiateTeleportBreakBlocks(pos);
            }

            this.teleportToPlayer = 0;
        }
    }

    protected void initiateTeleportBreakBlocks(BlockPosition pos) {
        Location loc = new Location (this.getWorld().getWorld(), pos.getX(), pos.getY(), pos.getZ());

        double initX = loc.getX();
        double initY = loc.getY();
        double initZ = loc.getZ();

        for (int x = -2; x < 3; x++) {
            for (int y = -2; y < 3; y++) {
                for (int z = -2; z < 3; z++) {
                    if (loc.getBlock().getType() != org.bukkit.Material.BEDROCK && loc.getBlock().getType() != org.bukkit.Material.END_GATEWAY && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL && loc.getBlock().getType() != org.bukkit.Material.END_PORTAL_FRAME && loc.getBlock().getType() != org.bukkit.Material.NETHER_PORTAL && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK  && loc.getBlock().getType() != org.bukkit.Material.COMMAND_BLOCK_MINECART && loc.getBlock().getType() != org.bukkit.Material.STRUCTURE_BLOCK && loc.getBlock().getType() != org.bukkit.Material.JIGSAW && loc.getBlock().getType() != org.bukkit.Material.BARRIER && loc.getBlock().getType() != org.bukkit.Material.SPAWNER && loc.getBlock().getType() != org.bukkit.Material.COBWEB) { //as long as it isn't one of these blocks
                        loc.setX(initX + x);
                        loc.setY(initY + y);
                        loc.setZ(initZ + z);
                        loc.getBlock().setType(org.bukkit.Material.AIR);
                    }
                }
            }
        }

        this.teleportTo(pos);
    }

    protected boolean teleportTo(BlockPosition pos) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(pos.getX(), pos.getY(), pos.getZ());

        while (blockposition_mutableblockposition.getY() > 0 && !this.world.getType(blockposition_mutableblockposition).getMaterial().isSolid()) {
            blockposition_mutableblockposition.c(EnumDirection.DOWN);
        }

        IBlockData iblockdata = this.world.getType(blockposition_mutableblockposition);

        if (iblockdata.getMaterial().isSolid()) {
            boolean flag2 = this.a(pos.getX(), pos.getY(), pos.getZ(), true);

            if (flag2 && !this.isSilent()) {
                this.world.playSound((EntityHuman)null, this.lastX, this.lastY, this.lastZ, SoundEffects.ENTITY_ENDERMAN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
                this.playSound(SoundEffects.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return flag2;
        } else {
            return false;
        }
    }

    @Override
    public boolean a(double d0, double d1, double d2, boolean flag) {
        double d3 = this.locX();
        double d4 = this.locY();
        double d5 = this.locZ();
        double d6 = d1;
        boolean flag1 = false;
        BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        World world = this.world;

        if (world.isLoaded(blockposition)) {
            boolean flag2 = false;

            while (!flag2 && blockposition.getY() > 0) {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = world.getType(blockposition1);

                if (iblockdata.getMaterial().isSolid()) {
                    flag2 = true;
                } else {
                    --d6;
                    blockposition = blockposition1;
                }
            }

            if (flag2) {
                this.enderTeleportTo(d0, d6, d2);
                if (world.getCubes(this)) { /**can teleport onto fluids*/
                    flag1 = true;
                }
            }
        }

        if (!flag1) {
            this.enderTeleportTo(d3, d4, d5);
            return false;
        } else {
            if (flag) {
                world.broadcastEntityEffect(this, (byte) 46);
            }

            if (this instanceof EntityCreature) {
                ((EntityCreature)this).getNavigation().o();
            }

            return true;
        }
    }
}
