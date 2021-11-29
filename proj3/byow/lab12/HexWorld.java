package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);



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
     * Fills the given 2D array of tiles with Nothing tiles.
     * @param tiles
     */
    public static void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }
    /**
     *  return a random tile
     * */
    private static TETile randomTile() {
        int r = RANDOM.nextInt(5);
        switch (r) {
            case 0: return Tileset.FLOWER;
            case 1: return Tileset.WALL;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.WALL;
            case 4: return Tileset.MOUNTAIN;
            default: return Tileset.NOTHING;
        }
    }

    /*
    * draw a row of length len start at the given position
    *
    * */
    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int len) {
        for (int dx = 0; dx < len; dx++) {
            tiles[p.x + dx][p.y] = tile;
        }
    }

    public static void drawHex(TETile[][] tiles, Position p, TETile tile, int size) {
        if (size < 2)  return;
        hexHelper(tiles, p, tile, size - 1, size);
    }

    /**
     * draw a hexagon recursively
     * b: the num of blank
     * t: the num of tiles
     * */
    private static void hexHelper(TETile[][] tiles, Position p, TETile tile, int b, int t) {
        Position start = p.shift(b, 0);
        drawRow(tiles, start, tile, t);

        if (b > 0) {
            Position nextP = p.shift(0, -1);
            hexHelper(tiles, nextP, tile, b - 1, t + 2);
        }

        Position startReflex = start.shift(0, -(2 * b + 1));
        drawRow(tiles, startReflex, tile, t);
    }

    /**
     * draw hex world of the given size
     * horizontal unit: 2*size - 1
     * vertical unit: 1*size
     * */
    public static void addHexWorld(TETile[][] tiles, Position p, int size) {
        if (size < 2) return;
        addHexWorldHelper(tiles, p, size, size - 1, size);
    }

    private static void addHexWorldHelper(TETile[][] tiles, Position p, int size, int b, int t) {
        int xUnit = 2*size - 1;
        int yUnit = size;

        Position startCol = p.shift(0, -b*yUnit);
        // print this column
        addHexCol(tiles, startCol, size, t);

        if (b > 0) {
            Position nextP = p.shift(xUnit, 0);
            addHexWorldHelper(tiles, nextP, size, b - 1, t + 1);
        }


        // print this column reflexion
        Position startColReflexion = startCol.shift(2*b*(xUnit), 0);
        addHexCol(tiles, startColReflexion, size, t);
    }

    private static void addHexCol(TETile[][] tiles, Position p, int size, int len) {
        // len: 几个hex
        // 从上往下画
        for (int i = 0; i < len; i++) {
            Position currP = p.shift(0, -i*2*size);
            drawHex(tiles, currP, randomTile(), size);
        }
    }

    public static void drawWorld(TETile[][] tiles) {
        fillWithNothing(tiles);
        addHexWorld(tiles, new Position(10,40), 3);
    }


    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        drawWorld(world);

        ter.renderFrame(world);
    }

}
