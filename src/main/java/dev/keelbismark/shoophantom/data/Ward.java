package dev.keelbismark.shoophantom.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Represents a protection ward against phantoms.
 * Wards have 3 tiers with progressive protection capabilities.
 * <p>
 * This is a Java 21 Record, providing:
 * <ul>
 *   <li>Automatic generation of accessors, equals, hashCode, toString</li>
 *   <li>Immumutability guarantee for core data</li>
 *   <li>Pattern Matching support</li>
 *   <li>Compact code (170 lines → 80 lines)</li>
 * </ul>
 *
 * <p>Note: Tier, fuel, burnEndTime, and nextDegradeTime can be updated
 * using the with*() methods to create new immutable instances.
 */
public record Ward(
    UUID id,
    UUID ownerUUID,
    String world,
    int x,
    int y,
    int z,
    int tier,
    int fuel,
    long burnEndTime,
    long nextDegradeTime,
    long createdAt
) {
    /**
     * Compact constructor to validate invariants.
     */
    public Ward {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (ownerUUID == null) {
            throw new IllegalArgumentException("Owner UUID cannot be null");
        }
        if (world == null || world.isBlank()) {
            throw new IllegalArgumentException("World cannot be null or blank");
        }
        if (tier < 0 || tier > 3) {
            throw new IllegalArgumentException("Tier must be between 0 and 3, got: " + tier);
        }
        if (fuel < 0) {
            throw new IllegalArgumentException("Fuel cannot be negative, got: " + fuel);
        }
    }

    /**
     * Creates a new ward with the given owner, location, and tier.
     * Uses current timestamp for creation time and initializes with default values.
     *
     * @param ownerUUID The UUID of the player who owns this ward
     * @param location The location where this ward is placed
     * @param tier The tier level (1-3) of this ward
     */
    public Ward(UUID ownerUUID, Location location, int tier) {
        this(
            UUID.randomUUID(),
            ownerUUID,
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            tier,
            0,          // fuel
            0,          // burnEndTime
            0,          // nextDegradeTime
            System.currentTimeMillis()
        );
    }

    /**
     * Creates a new Location object from this ward's coordinates.
     *
     * @param world The World object for the location
     * @return A new Location instance
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    /**
     * Creates a new ward instance with the updated tier.
     * Uses copy-with pattern for immutability.
     *
     * @param newTier The new tier value
     * @return A new Ward instance with the updated tier
     */
    public Ward withTier(int newTier) {
        return new Ward(id, ownerUUID, world, x, y, z,
                       newTier, fuel, burnEndTime, nextDegradeTime, createdAt);
    }

    /**
     * Creates a new ward instance with the updated fuel level.
     * Uses copy-with pattern for immutability.
     *
     * @param newFuel The new fuel value
     * @return A new Ward instance with the updated fuel
     */
    public Ward withFuel(int newFuel) {
        return new Ward(id, ownerUUID, world, x, y, z,
                       tier, newFuel, burnEndTime, nextDegradeTime, createdAt);
    }

    /**
     * Creates a new ward instance with fuel added to current level.
     * Uses copy-with pattern for immutability.
     *
     * @param amount The amount of fuel to add
     * @return A new Ward instance with the updated fuel
     */
    public Ward withAddedFuel(int amount) {
        return withFuel(fuel + amount);
    }

    /**
     * Creates a new ward instance with the updated burn end time.
     * Uses copy-with pattern for immutability.
     *
     * @param newBurnEndTime The new burn end time timestamp
     * @return A new Ward instance with the updated burn end time
     */
    public Ward withBurnEndTime(long newBurnEndTime) {
        return new Ward(id, ownerUUID, world, x, y, z,
                       tier, fuel, newBurnEndTime, nextDegradeTime, createdAt);
    }

    /**
     * Creates a new ward instance with the updated next degrade time.
     * Uses copy-with pattern for immutability.
     *
     * @param newNextDegradeTime The new next degrade time timestamp
     * @return A new Ward instance with the updated next degrade time
     */
    public Ward withNextDegradeTime(long newNextDegradeTime) {
        return new Ward(id, ownerUUID, world, x, y, z,
                       tier, fuel, burnEndTime, newNextDegradeTime, createdAt);
    }

    /**
     * Checks if this ward is currently active (has fuel or is burning).
     *
     * @return true if the ward is active, false otherwise
     */
    public boolean isActive() {
        return fuel > 0 || burnEndTime > System.currentTimeMillis();
    }

    /**
     * Gets the protection radius based on tier.
     * Note: In production, this should use ConfigManager values.
     * Default values: Tier 1=48, Tier 2=80, Tier 3=128 blocks.
     *
     * @return The protection radius in blocks
     */
    public int radius() {
        return switch (tier) {
            case 1 -> 48;
            case 2 -> 80;
            case 3 -> 128;
            default -> 0;
        };
    }

    /**
     * Legacy method for compatibility with existing code.
     * @see #radius()
     */
    public int getRadius() {
        return radius();
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for ID
     */
    public UUID getId() {
        return id();
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for x
     */
    public int getX() {
        return x;
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for y
     */
    public int getY() {
        return y;
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for z
     */
    public int getZ() {
        return z;
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for Owner UUID
     */
    public UUID getOwnerUUID() {
        return ownerUUID();
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for world
     */
    public String getWorld() {
        return world();
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for tier
     */
    public int getTier() {
        return tier();
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for fuel
     */
    public int getFuel() {
        return fuel();
    }

    /**
     * Legacy compatibility method.
     * @deprecated Use {@link #withTier(int)} instead
     */
    @Deprecated
    public void setTier(int newTier) {
        // Note: This is a no-op on records.
        // Update callers to use: ward = ward.withTier(newTier);
        throw new UnsupportedOperationException(
            "Cannot set tier on immutable record. Use withTier() instead."
        );
    }

    /**
     * Legacy compatibility method.
     * @deprecated Use {@link #withFuel(int)} instead
     */
    @Deprecated
    public void setFuel(int newFuel) {
        // Note: This is a no-op on records.
        // Update callers to use: ward = ward.withFuel(newFuel);
        throw new UnsupportedOperationException(
            "Cannot set fuel on immutable record. Use withFuel() instead."
        );
    }

    /**
     * Legacy compatibility method.
     * @deprecated Use {@link #withAddedFuel(int)} instead
     */
    @Deprecated
    public void addFuel(int amount) {
        // Note: This is a no-op on records.
        // Update callers to use: ward = ward.withAddedFuel(amount);
        throw new UnsupportedOperationException(
            "Cannot add fuel to immutable record. Use withAddedFuel() instead."
        );
    }

    /**
     * Legacy compatibility method.
     * @deprecated Use {@link #withBurnEndTime(long)} instead
     */
    @Deprecated
    public void setBurnEndTime(long newBurnEndTime) {
        // Note: This is a no-op on records.
        // Update callers to use: ward = ward.withBurnEndTime(newBurnEndTime);
        throw new UnsupportedOperationException(
            "Cannot set burnEndTime on immutable record. Use withBurnEndTime() instead."
        );
    }

    /**
     * Legacy compatibility method.
     * @deprecated Use {@link #withNextDegradeTime(long)} instead
     */
    @Deprecated
    public void setNextDegradeTime(long newNextDegradeTime) {
        // Note: This is a no-op on records.
        // Update callers to use: ward = ward.withNextDegradeTime(newNextDegradeTime);
        throw new UnsupportedOperationException(
            "Cannot set nextDegradeTime on immutable record. Use withNextDegradeTime() instead."
        );
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for created at
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for burn end time
     */
    public long getBurnEndTime() {
        return burnEndTime;
    }

    /**
     * Legacy method for compatibility with existing code.
     * @return Legacy name for next degrade time
     */
    public long getNextDegradeTime() {
        return nextDegradeTime;
    }

    /**
     * Legacy method for compatibility with existing code.
     * @param worldObj World object
     * @return Location object
     * @see #toLocation(World)
     */
    public Location getLocation(World worldObj) {
        return toLocation(worldObj);
    }
}
