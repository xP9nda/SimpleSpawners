package panda.simplespawners.data;

import java.util.UUID;

public class SpawnerData {

    // Spawner data values
    private UUID spawnerUUID;
    private UUID ownerUUID;

    // Constructor method
    public SpawnerData() {
        setSpawnerUUID(UUID.randomUUID());
    }

    public UUID getSpawnerUUID() {
        return spawnerUUID;
    }

    public void setSpawnerUUID(UUID spawnerUUID) {
        this.spawnerUUID = spawnerUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }
}
