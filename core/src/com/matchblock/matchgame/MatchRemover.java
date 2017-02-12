package com.matchblock.matchgame;

import com.matchblock.engine.Block;
import com.matchblock.engine.Grid;
import com.matchblock.engine.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MatchRemover {
    public static List<List<Point>> getTypeGroups(Grid grid, int minCount) {
        List<List<Point>> groups = new ArrayList<List<Point>>();
        HashSet<Point> visited = new HashSet<Point>();
        Queue<Point> next = new LinkedList<Point>();

        for (int iy = grid.bottom; iy >= grid.top; iy--) {
            for (int ix = grid.left; ix <= grid.right; ix++) {
                Block seedBlock = grid.getBlock(ix, iy);
                if (visited.contains(new Point(ix, iy)) || seedBlock.isEmpty())
                    continue;

                List<Point> group = new ArrayList<Point>();
                next.clear();
                next.add(new Point(ix, iy));
                while (!next.isEmpty()) {
                    Point current = next.poll();
                    int cx = current.x;
                    int cy = current.y;

                    if (visited.contains(current) || !grid.getBlock(cx, cy).matches(seedBlock))
                        continue;

                    visited.add(current);
                    group.add(current);

                    if (cx > grid.left)
                        next.add(new Point(cx - 1, cy));
                    if (cx < grid.right)
                        next.add(new Point(cx + 1, cy));
                    if (cy > grid.top)
                        next.add(new Point(cx, cy - 1));
                    if (cy < grid.bottom)
                        next.add(new Point(cx, cy + 1));
                }

                if (group.size() >= minCount) {
                    groups.add(group);
                }
            }
        }

        return groups;
    }
}
