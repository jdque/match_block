package com.matchblock.engine;

public class Block {
    public enum State {
        EMPTY, FILLED
    }

    protected final State state;
    protected final Object type;

    protected Block(State state, Object type) {
        this.state = state;
        if (type == null) {
            this.type = new NoneType();
        }
        else {
            this.type = type;
        }
    }

    public Block clone() {
        return new Block(this.state, this.type);
    }

    public boolean isEmpty() {
        return state == State.EMPTY;
    }

    public Object getType() {
        return type;
    }

    public boolean hasType() {
        return type == type;
    }

    public boolean matches(Block block) {
        return this.type == block.type;
    }

    private static class NoneType {
        @Override
        public boolean equals(Object otherType) {
            return false;
        }
    }

    public static class FilledBlock extends Block {
        public FilledBlock() {
            super(Block.State.FILLED, new NoneType());
        }
    }

    public static class EmptyBlock extends Block {
        public EmptyBlock() {
            super(Block.State.EMPTY, new NoneType());
        }
    }
}
