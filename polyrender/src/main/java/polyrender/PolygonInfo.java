package polyrender;

import java.util.List;

public class PolygonInfo {
    private List<List<Integer>> polygon_points;
    private List<Integer> color;

    public List<List<Integer>> getPolygonPoints() {
        return polygon_points;
    }

    public void setPolygonPoints(List<List<Integer>> polygonPoints) {
        this.polygon_points = polygonPoints;
    }

    public List<Integer> getColor() {
        return color;
    }

    public void setColor(List<Integer> color) {
        this.color = color;
    }
}
