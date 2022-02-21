import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class GameOfLife {

    public static void main(String[] args) {
        int xAxios = 25;
        int yAxios = 25;
        int generationsTotal = 15;

        List<Cell> seeds = Arrays.asList(
                new Cell(new Position(8,12), CellState.ALIVE),
                new Cell(new Position(9,13), CellState.ALIVE),
                new Cell(new Position(10,11), CellState.ALIVE),
                new Cell(new Position(10,12), CellState.ALIVE),
                new Cell(new Position(10,13), CellState.ALIVE)
        );
        List<Cell> cellsGeneration = initUniverse(seeds, xAxios, yAxios);

        int generationCounter = 0;
        while (generationCounter < generationsTotal) {
            try {
                renderToConsole(cellsGeneration, xAxios, yAxios);
                cellsGeneration = produceNewGeneration(cellsGeneration);
                generationCounter++;

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static  List<Cell> initUniverse(List<Cell> seeds, int xAxios, int yAxios) {
        List<Cell> universeState = new ArrayList<>();
        for (int x = 0; x < xAxios; x++) {
            for (int y = 0; y < yAxios; y++) {
                Position position = new Position(x, y);
                Cell cell = new Cell(position, CellState.DEAD);
                universeState.add(cell);
            }
        }
        universeState.removeAll(seeds);
        universeState.addAll(seeds);
        return universeState;
    }

    // this method always returns a copy of Cell object to avoid bugs related to state changes
    // in case of performance problems could be implemented with mutable state and reusing of same Cell objects
    private static Cell cellToState(Cell cell, List<Cell> neighbors) {
        Position cellPosition = cell.getPosition().copy();
        long aliveNeighborsCount = neighbors.stream()
                .filter((c) -> c.getState().equals(CellState.ALIVE))
                .count();
        if (cell.getState().equals(CellState.ALIVE)) {
            if (aliveNeighborsCount < 2) {
                return new Cell(cellPosition, CellState.DEAD);
            } else if (aliveNeighborsCount == 2 || aliveNeighborsCount == 3) {
                return cell.copy();
            } else if (aliveNeighborsCount > 3) {
                return new Cell(cellPosition, CellState.DEAD);
            }
        } else if (cell.getState().equals(CellState.DEAD)){
            if (aliveNeighborsCount == 3) {
                return new Cell(cellPosition, CellState.ALIVE);
            }
        }
        return cell.copy();
    }

    private static List<Cell> produceNewGeneration(List<Cell> cells) {
        List<Cell> newGeneration = new ArrayList<>();
        cells.forEach(cell -> {
            List<Cell> neighbors = findNeighbors(cell, cells);
            Cell newGenerationCell = cellToState(cell, neighbors);
            newGeneration.add(newGenerationCell);
        });
        return newGeneration;
    }

    private static List<Cell> findNeighbors(Cell currentCell, List<Cell> allCells) {
        Position currentPosition = currentCell.getPosition();
        Integer upstairsAxiosY = (currentCell.getPosition().getY() + 1);
        Integer downstairsAxiosY = (currentCell.getPosition().getY() - 1);

        Predicate<Cell> isNeighborLine = checkedCell -> {
            Integer checkedCellY = checkedCell.getPosition().getY();
            return (checkedCellY.equals(currentPosition.getY())
                    || checkedCellY.equals(upstairsAxiosY)
                    || checkedCellY.equals(downstairsAxiosY));
        };

        Predicate<Cell> isNeighborCell = checkedCell -> {
            Integer checkedCellX = checkedCell.getPosition().getX();
            return (checkedCellX.equals(currentPosition.getX())
                    || checkedCellX.equals(currentPosition.getX() - 1)
                    || checkedCellX.equals(currentPosition.getX() + 1));
        };

        return allCells.stream()
                .filter((c) -> !c.getPosition().equals(currentPosition))
                .filter(isNeighborLine)
                .filter(isNeighborCell)
                .collect(toList());
    }

    // temp helper to test finding of neighbors
    public static void testFindCellNeighbors() {
        List<Cell> cells = initUniverse(new ArrayList<>(), 5, 4);

        Cell centerCell = new Cell(new Position(3, 2), CellState.ALIVE);
        System.out.println("Neighbors of centerCell: " +
                findNeighbors(centerCell, cells).stream()
                        .map(Cell::getPosition)
                        .collect(toList()));

        Cell topLeftCornerCell = new Cell(new Position(0, 3), CellState.ALIVE);
        System.out.println("Neighbors topLeftCornerCell: " +
                findNeighbors(topLeftCornerCell, cells).stream()
                        .map(Cell::getPosition)
                        .collect(toList()));

        Cell topRightCornerCell = new Cell(new Position(4, 3), CellState.ALIVE);
        System.out.println("Neighbors topRightCornerCell: " +
                findNeighbors(topRightCornerCell, cells).stream()
                        .map(Cell::getPosition)
                        .collect(toList()));

        Cell botLeftCornerCell = new Cell(new Position(0, 0), CellState.ALIVE);
        System.out.println("Neighbors botLeftCornerCell: " +
                findNeighbors(botLeftCornerCell, cells).stream()
                        .map(Cell::getPosition)
                        .collect(toList()));

        Cell botRightCornerCell = new Cell(new Position(4, 0), CellState.ALIVE);
        System.out.println("Neighbors botRightCornerCell: " +
                findNeighbors(botRightCornerCell, cells).stream()
                        .map(Cell::getPosition)
                        .collect(toList()));
    }

    private static void renderToConsole(List<Cell> cells, int xAxios, int yAxios) {
        System.out.println("New generation:");
        for (int x = 0; x < xAxios; x++) {
            for (int y = 0; y < yAxios; y++) {
                Position currentPosition = new Position(x, y);
                Optional<Cell> cell = cells.stream()
                        .filter((c) -> c.getPosition().equals(currentPosition))
                        .findFirst();
                if (cell.isPresent() && cell.get().getState().equals(CellState.ALIVE)) {
                    System.out.print(" @ ");
                } else if (cell.isPresent() && cell.get().getState().equals(CellState.DEAD)) {
                    System.out.print(" - ");
                }
            }
            System.out.println("");
        }
    }
}

enum CellState {
    ALIVE, DEAD
}

class Position {
    private final Map<String, Integer> positionMap;

    public Position(Integer x, Integer y) {
        this.positionMap = new HashMap<>();
        this.positionMap.put("x", x);
        this.positionMap.put("y", y);
    }

    public Integer getX() {
        return positionMap.get("x");
    }

    public Integer getY() {
        return positionMap.get("y");
    }

    public Position copy() {
        return new Position(this.getX(), this.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return positionMap.equals(position.positionMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionMap);
    }

    @Override
    public String toString() {
        return "Position{" +
                "positionMap=" + positionMap +
                '}';
    }
}

class Cell {
    private final CellState state;
    private final Position position;

    public Cell(Position position, CellState state) {
        this.position = position;
        this.state = state;
    }

    public CellState getState() {
        return state;
    }

    public Position getPosition() {
        return position;
    }

    public Cell copy(){
        return new Cell(getPosition().copy(), getState());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return position.equals(cell.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "state=" + state +
                ", position=" + position +
                '}';
    }
}
