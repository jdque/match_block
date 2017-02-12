package com.matchblock.engine;

import com.badlogic.gdx.graphics.Color;

public class ColoredBlock extends Block {
    public enum Type {
        RED, BLUE, GREEN, PURPLE, ORANGE, MAGENTA
    }

    public ColoredBlock(Type type) {
        super(Block.State.FILLED, type);
    }

    public static Color getBlockColor(Block block) {
        if (block == null || !block.hasType()) {
            return null;
        }

        Object type = block.getType();

        Color color = null;
        if (type == Type.RED)
            color = Color.RED;
        else if (type == Type.BLUE)
            color = Color.BLUE;
        else if (type == Type.GREEN)
            color = Color.GREEN;
        else if (type == Type.PURPLE)
            color = Color.PURPLE;
        else if (type == Type.ORANGE)
            color = Color.ORANGE;
        else if (type == Type.MAGENTA)
            color = Color.MAGENTA;

        return color;
    }

    public String toString() {
        String typeStr = "";

        if (type == Type.RED)
            typeStr = "1";
        else if (type == Type.BLUE)
            typeStr = "2";
        else if (type == Type.GREEN)
            typeStr = "3";
        else if (type == Type.PURPLE)
            typeStr = "4";
        else if (type == Type.ORANGE)
            typeStr = "5";
        else if (type == Type.MAGENTA)
            typeStr = "6";

        return typeStr;
    }

    public static ColoredBlock fromString(String typeStr) {
        Type type = null;

        if (typeStr.equals("1"))
            type = Type.RED;
        else if (typeStr.equals("2"))
            type = Type.BLUE;
        else if (typeStr.equals("3"))
            type = Type.GREEN;
        else if (typeStr.equals("4"))
            type = Type.PURPLE;
        else if (typeStr.equals("5"))
            type = Type.ORANGE;
        else if (typeStr.equals("6"))
            type = Type.MAGENTA;

        ColoredBlock block = new ColoredBlock(type);

        return block;
    }
}
