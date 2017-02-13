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

        switch (type) {
            case NONE:    color = null;          break;
            case RED:     color = Color.RED;     break;
            case BLUE:    color = Color.BLUE;    break;
            case GREEN:   color = Color.GREEN;   break;
            case PURPLE:  color = Color.PURPLE;  break;
            case ORANGE:  color = Color.ORANGE;  break;
            case MAGENTA: color = Color.MAGENTA; break;
        }

        return color;
    }

    public String toString() {
        String typeStr = null;

        switch (type) {
            case NONE:    typeStr = "0"; break;
            case RED:     typeStr = "1"; break;
            case BLUE:    typeStr = "2"; break;
            case GREEN:   typeStr = "3"; break;
            case PURPLE:  typeStr = "4"; break;
            case ORANGE:  typeStr = "5"; break;
            case MAGENTA: typeStr = "6"; break;
        }

        return typeStr;
    }

    public static ColoredBlock fromString(String typeStr) {
        Type type = null;

        switch (typeStr) {
            case "0": type = Type.NONE;    break;
            case "1": type = Type.RED;     break;
            case "2": type = Type.BLUE;    break;
            case "3": type = Type.GREEN;   break;
            case "4": type = Type.PURPLE;  break;
            case "5": type = Type.ORANGE;  break;
            case "6": type = Type.MAGENTA; break;
        }

        return new ColoredBlock(type);
    }
}