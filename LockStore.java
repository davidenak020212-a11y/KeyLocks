package com.example.keylocks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

final class LockStore {
    private final Map<String, LockData> locks = new HashMap<>();
    private Path file;
    private static String key(World world, BlockPos pos) { return world.getRegistryKey().getValue() + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ(); }
    String get(World world, BlockPos pos) { LockData d = locks.get(key(world, pos)); return d == null ? null : d.color; }
    void put(World world, BlockPos pos, String color, String owner) { locks.put(key(world, pos), new LockData(color, owner)); }
    void remove(World world, BlockPos pos) { locks.remove(key(world, pos)); }

    void load(MinecraftServer server) {
        file = server.getSavePath(WorldSavePath.ROOT).resolve("keylocks.json");
        locks.clear();
        try {
            if (Files.exists(file)) {
                JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                for (var e : root.entrySet()) {
                    String raw = e.getValue().getAsString();
                    String[] parts = raw.split("\\|", 2);
                    locks.put(e.getKey(), new LockData(parts[0], parts.length > 1 ? parts[1] : ""));
                }
            }
        } catch (Exception e) { System.err.println("[KeyLocks] Could not load lock data: " + e); }
    }
    void save(MinecraftServer server) {
        if (file == null) file = server.getSavePath(WorldSavePath.ROOT).resolve("keylocks.json");
        JsonObject root = new JsonObject();
        locks.forEach((k, v) -> root.addProperty(k, v.color + "|" + v.owner));
        try { Files.writeString(file, root.toString()); }
        catch (Exception e) { System.err.println("[KeyLocks] Could not save lock data: " + e); }
    }
    private record LockData(String color, String owner) {}
}
