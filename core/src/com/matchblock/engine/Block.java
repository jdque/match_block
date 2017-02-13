package com.matchblock.engine;

public abstract class Block {
    protected Block() {
    }

    public abstract Block clone();

    public abstract boolean isEmpty();

    public abstract boolean matches(Block other);

    public static class FilledBlock extends Block {
        public FilledBlock() {
        }

        @Override
        public Block clone() {
            return new FilledBlock();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean matches(Block other) {
            return !other.isEmpty();
        }
    }

    public static class EmptyBlock extends Block {
        public EmptyBlock() {
        }

        @Override
        public Block clone() {
            return new EmptyBlock();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean matches(Block other) {
            return other.isEmpty();
        }
    }
}
