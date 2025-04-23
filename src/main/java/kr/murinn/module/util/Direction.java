package kr.murinn.module.util;

import org.bukkit.block.structure.StructureRotation;

public enum Direction {
    NORTH(StructureRotation.NONE),
    EAST(StructureRotation.CLOCKWISE_90),
    WEST(StructureRotation.COUNTERCLOCKWISE_90),
    SOUTH(StructureRotation.CLOCKWISE_180);

    private final StructureRotation rotation;

    Direction(StructureRotation rotation) {
        this.rotation = rotation;
    }

    public StructureRotation getRotation() {
        return rotation;
    }
}