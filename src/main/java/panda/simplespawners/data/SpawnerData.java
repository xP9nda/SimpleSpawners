package panda.simplespawners.data;

import java.util.UUID;

public class SpawnerData {

    // Spawner data values
    private UUID spawnerUUID;
    private int x;
    private int y;
    private int z;
    private String world;

    // Constructor method
    public SpawnerData() {

    }

    public UUID getSpawnerUUID() {
        return spawnerUUID;
    }

    public void setSpawnerUUID(UUID spawnerUUID) {
        this.spawnerUUID = spawnerUUID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }
}
