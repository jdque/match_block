package com.matchblock.matchgame;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.List;

public class Logic {
    public static class Score {
        private int minCount, basePoints, extraBlockBonus, multiGroupBonus;
        private float chainMultiplier;
        private int totalScore;

        public Score() {
            this.totalScore = 0;
            configure(0, 0, 0, 0, 0.0f);
        }

        public void configure(int minCount, int basePoints, int extraBlockBonus, int multiGroupBonus, float chainMultiplier) {
            this.minCount = minCount;
            this.basePoints = basePoints;
            this.extraBlockBonus = extraBlockBonus;
            this.multiGroupBonus = multiGroupBonus;
            this.chainMultiplier = chainMultiplier;
        }

        public void scoreGroups(List<List<GridPoint2>> groups, int currentChain) {
            if (groups.size() == 0)
                return;

            int curScore = 0;
            for (int i = 0; i < groups.size(); i++) {
                int count = groups.get(i).size();
                curScore += basePoints * minCount;
                for (int j = 0; j < count - minCount; j++) {
                    curScore += basePoints + extraBlockBonus * (j + 1);
                }
                if (i > 0) {
                    curScore += multiGroupBonus;
                }
            }

            if (currentChain > 1) {
                curScore *= chainMultiplier * (currentChain - 1);
            }

            totalScore += curScore;
        }

        public void setTotalScore(int score) {
            totalScore = score;
        }

        public int getTotalScore() {
            return totalScore;
        }
    }

    public static class Speed {
        private ArrayList<SpeedValue> speeds;
        private int currIdx, currIter;

        public Speed() {
            speeds = new ArrayList<SpeedValue>();
            reset();
        }

        public void reset() {
            currIdx = 0;
            currIter = 0;
        }

        public void addSpeed(float speed, int steps) {
            speeds.add(new SpeedValue(speed, steps));
        }

        public void step() {
            currIter++;
            if (currIdx < speeds.size() - 1) {
                if (currIter >= speeds.get(currIdx+1).step) {
                    currIdx++;
                }
            }
        }

        public void gotoStep(int step) {
            for (int i = 0; i < speeds.size(); i++) {
                if (speeds.get(i).step > step) {
                    break;
                }
                currIdx = i;
            }
            currIter = step;
        }

        public float getCurrentSpeed() {
            return speeds.get(currIdx).speed;
        }

        public int getStepTotal() {
            return currIter;
        }

        private static class SpeedValue {
            public final float speed;
            public final int step;
            public SpeedValue(float speed, int step) {
                this.speed = speed;
                this.step = step;
            }
        }
    }
}
