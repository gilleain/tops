package tops.port.calculate.util;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import tops.port.model.Axis;
import tops.port.model.Chain;
import tops.port.model.SSE;
import tops.port.model.tse.Sheet;

/**
 * Calculate the minimal bounding box of a structure.
 * 
 * @author maclean
 *
 */
public class BoundingBoxCalculator {
    
    public class BoundingBox {  // TODO - extract ...
        public Point3d center;
    }
    
    public BoundingBox calculate(Chain chain, Sheet sheet) { // XXX ugh params
        List<Point3d> points = new ArrayList<Point3d>();
        for (SSE sse : sheet.getElements()) {
            List<Point3d> sseCoords = 
                    chain.secondaryStructureAxis(
                            sse.sseData.SeqStartResidue, sse.sseData.SeqFinishResidue);
            points.addAll(sseCoords);
        }
        BoundingBox boundingBox = calculate(points);
        boundingBox.center = getSheetCenter(sheet);
        return boundingBox;
    }
    
    public BoundingBox calculate(List<Point3d> points) {
        BoundingBox boundingBox = new BoundingBox();
        if (points.size() < 4) {
            return boundingBox;
        } else {
            return boundingBox;
        }
    }
    
    private Point3d getSheetCenter(Sheet sheet) {
        int n = sheet.span();
        Point3d center = new Point3d();
        if (n % 2 == 0) {
            int firstIndex = (n / 2) - 1;
            int secondIndex = firstIndex + 1;
            Axis a1 = sheet.get(firstIndex).axis;
            Axis a2 = sheet.get(secondIndex).axis;
            Point3d p1 = a1.getCentroid();
            Point3d p2 = a2.getCentroid();
            center.set(p1);
            center.add(p2);
            center.scale(0.5);
        } else {
            int index = (n + 1 / 2) - 1; // this could probably be simpler...
            Axis axis = sheet.get(index).axis;
            center.set(axis.getCentroid());
        }
        return center;
    }
    
    private double[] getDoubles(List<Point3d> points) {
        int n = points.size() * 3;
        double[] numbers = new double[n];
        int pointIndex = 0;
        for (int index = 0; index < n; index += 3) {
            Point3d point = points.get(pointIndex);
            numbers[index + 0] = point.x;
            numbers[index + 1] = point.y;
            numbers[index + 2] = point.z;
            pointIndex++;
        }
        return numbers;
    }

}
