/**
 * Given this matrix, a start coordinate, and an end coordinate, return the minimum number of steps required to reach
 * the end coordinate from the start. If there is no possible path, then return null.
 * You can move up, left, down, and right. You cannot move through walls.
 * You cannot wrap around the edges of the board.
 *
 * For example, given the following board:
 * ____________
 * |f, f, f, f|
 * |t, t, f, t|
 * |f, f, f, f|
 * |f, f, f, f|
 * ------------
 * and start = (3, 0) (bottom left) and end = (0, 0) (top left), the minimum number of steps required to reach
 * the end is 7, since we would need to go through (1, 2) because there is a wall everywhere else on the second row.
 */
package dailycodingproblems;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
class Point {
    int x;
    int y;
    int dist;
    
    public Point(int x, int y, int dist) {
        this.x = x;
        this.y = y;
        this.dist = dist;
    }
}
public class DailyCodingProblem23 {
    public static void main(String[] args) {
        boolean[][] maze = {
                {false, true, true, false},
                {false, true, true, false},
                {false, true, true, false},
                {false, false, false, false}
        };
        Point origin = new Point(0,3, 0);
        Point destination= new Point(0,0, 0);
        System.out.println("Minimum Steps: "+ findMinimumSteps(maze,origin,destination));
    }

    private static int findMinimumSteps(boolean[][] maze, Point start, Point end) {
        int countSteps = -1;
        boolean[][] visited = new boolean[maze.length][maze[0].length];
        visited[start.getX()][start.getY()] =  true;

        Queue<Point> queue = new LinkedList<>();
        queue.offer(new Point(start.getX(),start.getY(),0));
        while(!queue.isEmpty()) {
            Point current = queue.poll();
            if(current.getX() == end.getX() && current.getY() == end.getY()) {
                return current.getDist();
            }
            //UP
            if (current.getX()-1>= 0 && !visited[current.getX()-1][current.getY()] && !maze[current.getX()-1][current.getY()]) {
                queue.offer(new Point(current.getX()-1,current.getY(),current.getDist()+1));
                visited[current.getX()-1][current.getY()] = true;
            }
            //DOWN
            if (current.getX()+1 < maze.length && !visited[current.getX()+1][current.getY()] && !maze[current.getX()+1][current.getY()]) {
                queue.offer(new Point(current.getX()+1,current.getY(),current.getDist()+1));
                visited[current.getX()+1][current.getY()] = true;
            }
            //RIGHT
            if (current.getY()+1 < maze[0].length && !visited[current.getX()][current.getY()+1] && !maze[current.getX()][current.getY()+1]) {
                queue.offer(new Point(current.getX(),current.getY()+1,current.getDist()+1));
                visited[current.getX()][current.getY()+1] = true;
            }
            //LEFT
            if (current.getY()-1 >= 0 && !visited[current.getX()][current.getY()-1] && !maze[current.getX()][current.getY()-1]) {
                queue.offer(new Point(current.getX(),current.getY()-1,current.getDist()+1));
                visited[current.getX()][current.getY()-1] = true;
            }
        }
        return countSteps;
    }
    

}
