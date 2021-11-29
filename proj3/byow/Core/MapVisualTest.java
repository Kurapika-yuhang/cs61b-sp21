package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.HexWorld;

import java.util.Random;
import java.util.RandomAccess;

public class MapVisualTest {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public static final long SEED = 955;
    public static final Random RANDOM = new Random(SEED);

    private static class Position {
        private int x;
        private int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }


    /**
     * @param world
     * @return the 2D world filled with Nothing Tile
     */
    private static void fillWithNothing(TETile[][] world) {
        int height = world[0].length;
        int width = world.length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++)
                world[x][y] = Tileset.NOTHING;
        }
    }



    private static void drawWorld(TETile[][] world) {
        fillWithNothing(world);
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];

        drawWorld(world);

        ter.renderFrame(world);
    }
}
