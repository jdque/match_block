package com.matchblock.matchgame;

import com.badlogic.gdx.graphics.Color;
import com.matchblock.engine.Block;

public class ColoredBlock extends Block {
    public enum Type {
        NONE, RED, BLUE, GREEN, PURPLE, ORANGE, MAGENTA
    }

    private final Type type;

    public ColoredBlock(Type type) {
        this.type = type;
    }

    @Override
    public ColoredBlock clone() {
        return new ColoredBlock(this.type);
    }

    @Override
    public boolean isEmpty() {
        return this.type == Type.NONE;
    }

    @Override
    public boolean matches(Block other) {
        ColoredBlock coloredOther = (ColoredBlock) other;
        return coloredOther != null && coloredOther.type == this.type;
    }

    public static Color getBlockColor(ColoredBlock block) {
        if (block == null) {
            return null;
        }

        Type type = block.type;

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
        String typeStr = null;

        if (type == Type.NONE)
            typeStr = "0";
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
        Type type = Type.NONE;

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

        return new ColoredBlock(type);
    }
}
