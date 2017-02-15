package com.matchblock.matchgame;

import com.badlogic.gdx.math.GridPoint2;
import com.matchblock.engine.Block;
import com.matchblock.engine.Grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MatchRemover {
    public static List<List<GridPoint2>> getTypeGroups(Grid<ColoredBlock> grid, int minCount) {
        List<List<GridPoint2>> groups = new ArrayList<>();
        HashSet<GridPoint2> visited = new HashSet<>();
        Queue<GridPoint2> next = new LinkedList<>();

        for (int iy = grid.bottom; iy >= grid.top; iy--) {
            for (int ix = grid.left; ix <= grid.right; ix++) {
                Block seedBlock = grid.getBlock(ix, iy);
                if (visited.contains(new GridPoint2(ix, iy)) || seedBlock.isEmpty())
                    continue;

                List<GridPoint2> group = new ArrayList<>();
                next.clear();
                next.add(new GridPoint2(ix, iy));
                while (!next.isEmpty()) {
                    GridPoint2 current = next.poll();
                    int cx = current.x;
                    int cy = current.y;

                    if (visited.contains(current) || !grid.getBlock(cx, cy).matches(seedBlock))
                        continue;

                    visited.add(current);
                    group.add(current);

                    if (cx > grid.left)
                        next.add(new GridPoint2(cx - 1, cy));
                    if (cx < grid.right)
                        next.add(new GridPoint2(cx + 1, cy));
                    if (cy > grid.top)
                        next.add(new GridPoint2(cx, cy - 1));
                    if (cy < grid.bottom)
                        next.add(new GridPoint2(cx, cy + 1));
                }

                if (group.size() >= minCount) {
                    groups.add(group);
                }
            }
        }

        return groups;
    }
}
