package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityGuardian;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityIronGolem;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityPiglin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntitySilverfish;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableThorLightningEffectStorm;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.Explosion;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class BlockPlaceAndBreakListeners implements Listener {

    public static JavaPlugin plugin;
    private static final Random random = new Random();

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Block bukkitBlock = event.getBlock();
        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        Location loc = bukkitBlock.getLocation();
        Material type = bukkitBlock.getType();

        if (event.getPlayer() != null) {
            if (bukkitBlock.getLocation().getY() >= 129.0) { /**can't build above y level 128 in all dimensions to prevent towering up etc. to avoid mobs*/
                event.setCancelled(true);
                Bukkit.broadcastMessage("Not so fast, smartypants");
                Bukkit.broadcastMessage("You have reached the build height limit of 128 blocks :tf:");
            }

            if (type == Material.CONDUIT) { /**conduits spawn guardians every 5 seconds for 50 seconds*/
                new ConduitSummonGuardian(nmsWorld, loc, 10).runTaskTimer(plugin, 0L, 100L);
            }
        } else {
            if (type == Material.COBWEB) { /**spider-placed cobwebs are deleted after 2.5 seconds*/
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() //async thread is used so that the game doesn't pause completely for 2.5 seconds
                {
                    @Override
                    public void run() {
                        bukkitBlock.setType(Material.AIR);
                    }
                }, 50);
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        EntityPlayer nmsPlayer = ((CraftPlayer)event.getPlayer()).getHandle();
        World nmsWorld = nmsPlayer.getWorld();
        Block bukkitBlock = event.getBlock();
        Location loc = bukkitBlock.getLocation();
        Material type = bukkitBlock.getType();

        switch (type) {
            case DEAD_BUSH, TORCH, ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL, SMITHING_TABLE ->  /**dead bushes, torches, anvils, and smithing tables explode when broken but don't break blocks*/
                nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 2.0F, false, Explosion.Effect.NONE);
            case GOLD_BLOCK, GOLD_ORE, CHEST, BARREL, DISPENSER, DROPPER, ENDER_CHEST, HOPPER, CHEST_MINECART, HOPPER_MINECART, SHULKER_BOX, TRAPPED_CHEST -> { /**breaking these blocks causes piglins in a 40 block cube to go into a frenzy for 15 seconds*/
                List<Entity> nmsEntities = nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().g(40.0), entity -> entity instanceof CustomEntityPiglin);

                for (Entity entity : nmsEntities) {
                    ((CustomEntityPiglin)entity).veryAngryTicks += 300;
                }
            }
            case NETHER_GOLD_ORE -> { /**breaking nether gold ore has a 80% chance to cause a random block within a 5 by 5 by 5 radius to turn into lava*/
                if (random.nextDouble() < 0.8) {
                    (new Location(bukkitBlock.getWorld(), loc.getX() + random.nextInt(5) - 2, loc.getY() + random.nextInt(5) - 2, loc.getZ() + random.nextInt(5) - 2)).getBlock().setType(Material.LAVA);
                }

                List<Entity> nmsEntities = nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().g(40.0), entity -> entity instanceof CustomEntityPiglin);

                for (Entity entity : nmsEntities) {
                    ((CustomEntityPiglin)entity).veryAngryTicks += 300;
                }
            }
            case SPAWNER, CONDUIT, STONE_BRICKS, CRACKED_STONE_BRICKS, MOSSY_STONE_BRICKS, IRON_BARS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS, COBBLESTONE_STAIRS, BOOKSHELF -> { /**breaking these blocks (all found in strongholds) causes a silverfish to spawn*/
                new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), type == Material.CONDUIT ? 50 : (type == Material.SPAWNER ? 5 : 1), null, loc, true); /**breaking a spawner spawns 5 silverfish and breaking a conduit spawns 50*/
            }
        }
    }

    @EventHandler
    public void blockExplodeAndBreakBlock(BlockExplodeEvent event) { /**explosions do not cause blocks to drop*/
        event.setYield(0.0F);

        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        Location loc;
        Material type;

        for (Block block : event.blockList()) {
            loc = block.getLocation();
            type = block.getType();

            switch (type) {
                case DEAD_BUSH, TORCH -> /**dead bushes and torches explode when broken but don't break blocks*/
                    nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 2.0F, false, Explosion.Effect.NONE);
                case STONE_BRICKS, CRACKED_STONE_BRICKS, MOSSY_STONE_BRICKS, IRON_BARS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS, COBBLESTONE_STAIRS, BOOKSHELF -> { /**breaking these blocks (all found in strongholds) causes a silverfish to spawn*/
                    new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, loc, true);
                }
            }

            if (type == Material.STONE_BRICKS || type == Material.CRACKED_STONE_BRICKS || type == Material.MOSSY_STONE_BRICKS || type == Material.IRON_BARS || type == Material.STONE_BRICK_SLAB || type == Material.STONE_BRICK_STAIRS || type == Material.COBBLESTONE_STAIRS || type == Material.BOOKSHELF) {
                new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, loc, true);
            }

            if (type == Material.DEAD_BUSH || type == Material.TORCH) {
                nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 3.0F, false, Explosion.Effect.NONE);
            }
        }
    }

    @EventHandler
    public void entityExplodeAndBreakBlock(EntityExplodeEvent event) { /**explosions do not cause blocks to drop*/
        event.setYield(0.0F);

        World nmsWorld = ((CraftWorld)event.getEntity().getWorld()).getHandle();
        Location loc;
        Material type;

        for (Block block : event.blockList()) {
            loc = block.getLocation();
            type = block.getType();

            if (type == Material.STONE_BRICKS || type == Material.CRACKED_STONE_BRICKS || type == Material.MOSSY_STONE_BRICKS || type == Material.IRON_BARS || type == Material.STONE_BRICK_SLAB || type == Material.STONE_BRICK_STAIRS || type == Material.COBBLESTONE_STAIRS || type == Material.BOOKSHELF) { /**breaking these blocks (all found in strongholds) causes a silverfish to spawn*/
                new SpawnLivingEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, CreatureSpawnEvent.SpawnReason.INFECTION, loc, true);
            }

            if (type == Material.DEAD_BUSH || type == Material.TORCH) { /**dead bushes and torches explode when broken but don't break blocks*/
                nmsWorld.createExplosion(null, loc.getX(), loc.getY(), loc.getZ(), 3.0F, false, Explosion.Effect.NONE);
            }
        }
    }

    static class ConduitSummonGuardian extends BukkitRunnable {

        private final World nmsWorld;
        private final Location loc;
        private int cycles;
        private final int maxCycles;

        public ConduitSummonGuardian(World nmsWorld, Location loc, int maxCycles) {
            this.nmsWorld = nmsWorld;
            this.loc = loc;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles > this.maxCycles) {
                this.cancel();
                return;
            }

            new SpawnLivingEntity(this.nmsWorld, new CustomEntityGuardian(this.nmsWorld), 1, null, this.loc, true);
        }
    }
}
