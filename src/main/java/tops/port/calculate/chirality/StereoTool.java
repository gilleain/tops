package tops.port.calculate.chirality;

/* Copyright (C) 2005-2009  The Jmol Development Team
 * Copyright (C) 2010 Gilleain Torrance <gilleain.torrance@gmail.com>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


/**
 * XXX DO NOT COMMIT - NICKED FROM CDK!!
 * 
 * Methods to determine or check the stereo class of a set of points.
 * 
 * Some of these methods were adapted from Jmol's smiles search package.
 * 
 * @author maclean
 * @cdk.module standard
 */
public class StereoTool {

    /**
     * Currently unused, but intended for the StereoTool to indicate what it
     * 'means' by an assignment of some points to a class.
     *
     */
    public enum StereoClass { TETRAHEDRAL, SQUARE_PLANAR, 
        TRIGONAL_BIPYRAMIDAL, OCTAHEDRAL }

    /**
     * The handedness of a tetrahedron, in terms of the point-plane distance
     * of three of the corners, compared to the fourth.
     * 
     * PLUS indices a positive point-plane distance,
     * MINUS is a negative point-plane distance.
     */
    public enum TetrahedralSign { PLUS, MINUS }

    /**
     * The shape that four points take in a plane.
     */
    public enum SquarePlanarShape { U_SHAPE, FOUR_SHAPE, Z_SHAPE }

    /**
     * The maximum angle in radians for two lines to be 'diaxial'.
     * Where 0.95 is about 172 degrees.
     */
    public static final double MAX_AXIS_ANGLE = 0.95;

    /**
     * The maximum tolerance for the normal calculated during colinearity.
     */
    public static final double MIN_COLINEAR_NORMAL = 0.05;

    public static final double PLANE_TOLERANCE = 0.05;
    
    /**
     * Checks these four points for square planarity.
     * 
     * @param pointA an point in the plane
     * @param pointB an point in the plane 
     * @param pointC an point in the plane
     * @param pointD an point in the plane
     * @return true if all the points are in the same plane
     */
    
    public static boolean isSquarePlanar(
            Point3d pointA, Point3d pointB, Point3d pointC, Point3d pointD) {
        return isSquarePlanar(pointA, pointB, pointC, pointD, new Vector3d());
    }
    
    private static boolean isSquarePlanar(
            Point3d pointA, Point3d pointB, 
            Point3d pointC, Point3d pointD, Vector3d normal) {
        // define a plane using ABC, also checking that the are not colinear
        Vector3d vectorAB = new Vector3d();
        Vector3d vectorAC = new Vector3d();
        getRawNormal(pointA, pointB, pointC, normal, vectorAB, vectorAC);
        if (StereoTool.isColinear(normal)) return false;
        
        // check that F is in the same plane as CDE
        return StereoTool.allCoplanar(normal, pointC, pointD);
    }
    

    
    /**
     * Check that all the points in the list are coplanar (in the same plane)
     * as the plane defined by the planeNormal and the pointInPlane.
     * 
     * @param planeNormal the normal to the plane
     * @param pointInPlane any point know to be in the plane
     * @param points an array of points to test 
     * @return false if any of the points is not in the plane
     */
    
    public static boolean allCoplanar(
            Vector3d planeNormal, Point3d pointInPlane, Point3d... points) {
        for (Point3d point : points) {
            double distance = StereoTool.signedDistanceToPlane(
                    planeNormal, pointInPlane, point);
            if (distance < PLANE_TOLERANCE) {
                continue;
            } else {
                return false;
            }
        }
        return true; 
    }
    
   
    
    /**
     * Take four points, and return Stereo.CLOCKWISE or Stereo.ANTI_CLOCKWISE.
     * The first point is the one pointing towards the observer.
     * 
     * @param point1 the point pointing towards the observer
     * @param point2 the second point (points away)
     * @param point3 the third point (points away)
     * @param point4 the fourth point (points away)
     * @return clockwise or anticlockwise
     */
    public enum Stereo { ANTI_CLOCKWISE, CLOCKWISE }
    public static Stereo getStereo(
            Point3d point1, Point3d point2, Point3d point3, Point3d point4) {
        
        // a normal is calculated for the base points (2, 3, 4) and compared to
        // the first point. PLUS indicates ACW.
        TetrahedralSign sign = 
            StereoTool.getHandedness(point2, point3, point4, point1);
        
        if (sign == TetrahedralSign.PLUS) {
            return Stereo.ANTI_CLOCKWISE;
        } else {
            return Stereo.CLOCKWISE;
        }
    }

    /**
     * Gets the tetrahedral handedness of four points - three of which form the
     * 'base' of the tetrahedron, and the other the apex. Note that it assumes
     * a right-handed coordinate system, and that the points {A,B,C} are in
     * a counter-clockwise order in the plane they share.
     * 
     * @param basepointA the first point in the base of the tetrahedron
     * @param basepointB the second point in the base of the tetrahedron
     * @param basepointC the third point in the base of the tetrahedron
     * @param apexpoint the point in the point of the tetrahedron
     * @return
     */
    public static TetrahedralSign getHandedness(
            Point3d pointA, Point3d pointB, Point3d pointC, Point3d pointD) {
        // assumes anti-clockwise for a right-handed system
        Vector3d normal = StereoTool.getNormal(pointA, pointB, pointC);
        
        // it doesn't matter which of points {A,B,C} is used
        return StereoTool.getHandedness(normal, pointA, pointD);
    }
        
    private static TetrahedralSign getHandedness(
            Vector3d planeNormal, Point3d pointInPlane, Point3d testPoint) {
        double distance = signedDistanceToPlane(
                planeNormal, pointInPlane, testPoint);

        // The point-plane distance is the absolute value,
        // the sign of the distance gives the side of the plane the point is on
        // relative to the plane normal.
        if (distance > 0) {
            return TetrahedralSign.PLUS;
        } else {
            return TetrahedralSign.MINUS;
        }
    }

    /**
     * Checks the three supplied points to see if they fall on the same line.
     * It does this by finding the normal to an arbitrary pair of lines between
     * the points (in fact, A-B and A-C) and checking that its length is 0.
     * 
     * @param ptA
     * @param ptB
     * @param ptC
     * @return
     */
    public static boolean isColinear(Point3d ptA, Point3d ptB, Point3d ptC) {
        Vector3d vectorAB = new Vector3d();
        Vector3d vectorAC = new Vector3d();
        Vector3d normal = new Vector3d();
        
        StereoTool.getRawNormal(ptA, ptB, ptC, normal, vectorAB, vectorAC);
        return isColinear(normal);
    }
    
    private static boolean isColinear(Vector3d normal) {
        double baCrossACLen = normal.length();
        return baCrossACLen < StereoTool.MIN_COLINEAR_NORMAL;
    }

    /**
     * Given a normalized normal for a plane, any point in that plane, and
     * a point, will return the distance between the plane and that point.
     *  
     * @param planeNormal the normalized plane normal
     * @param pointInPlane an arbitrary point in that plane
     * @param point the point to measure
     * @return the signed distance to the plane
     */
    
    public static double signedDistanceToPlane(
            Vector3d planeNormal, Point3d pointInPlane, Point3d point) {
        if (planeNormal == null) return Double.NaN;

        Vector3d pointPointDiff = new Vector3d();
        pointPointDiff.sub(point, pointInPlane);
        return planeNormal.dot(pointPointDiff);
    }

    /**
     * <p>Given three points (A, B, C), makes the vectors A-B and A-C, and makes
     * the cross product of these two vectors; this has the effect of making a
     * third vector at right angles to AB and AC.</p>
     * 
     * <p>NOTE : the returned normal is normalized; that is, it has been
     * divided by its length.</p> 
     * 
     * @param ptA the 'middle' point
     * @param ptB one of the end points
     * @param ptC one of the end points
     * @return the vector at right angles to AB and AC
     */
    
    public static Vector3d getNormal(Point3d ptA, Point3d ptB, Point3d ptC) {
        Vector3d vectorAB = new Vector3d();
        Vector3d vectorAC = new Vector3d();
        Vector3d normal   = new Vector3d();
        StereoTool.getRawNormal(ptA, ptB, ptC, normal, vectorAB, vectorAC);
        normal.normalize();
        return normal;
    }
    
    private static void getRawNormal(Point3d ptA, Point3d ptB, Point3d ptC, 
                               Vector3d normal, Vector3d vcAB, Vector3d vcAC) {
        // make A->B and A->C
        vcAB.sub(ptB, ptA);
        vcAC.sub(ptC, ptA);
        
        // make the normal to this
        normal.cross(vcAB, vcAC);
    }

}
