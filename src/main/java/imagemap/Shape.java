package imagemap ;

import java.awt.* ;
import java.awt.geom.* ;

/**
 * Title: ImageMap Description: client side imagemap creator Copyright:
 * Copyright (c) 2001 Company: webdesign-pirna.de
 * 
 * @author Andreas Tetzl
 * @version 1.0
 * 
 */

public class Shape {

    public static int TYPE_RECT = 1 ;
    public static int TYPE_CIRCLE = 2 ;
    public static int TYPE_POLY = 3 ;

    public static int POINT_RECT_UPPERLEFT = -1 ;
    public static int POINT_RECT_UPPERRIGHT = -2 ;
    public static int POINT_RECT_LOWERLEFT = -3 ;
    public static int POINT_RECT_LOWERRIGHT = -4 ;
    public static int POINT_RECT_INSIDE = -5 ;
    public static int POINT_CIRCLE_RADIUS = -6 ;
    public static int POINT_CIRCLE_INSIDE = -7 ;
    public static int POINT_POLY_INSIDE = -8 ;

    private int type ;
    private Rectangle rect ;
    private Point circle_center ;
    private int circle_r ;
    private Polygon poly ;

    private String href ;
    private String alt ;
    private String onMouseOver ;
    private String onClick ;
    private String onMouseOut ;

    private Point mouseDragPosition ;

    /**
     * Constructor for rectangle objects
     */
    public Shape( int x, int y, int x2, int y2 ) {
        this.type = TYPE_RECT ;
        this.rect = new Rectangle() ;
        set_rect(x, y, x2, y2) ;
        href = new String("http://") ;
        alt = new String() ;
    }

    /**
     * Constructor for circle objects
     */
    public Shape( Point m, int r ) {
        this.type = TYPE_CIRCLE ;
        this.circle_center = new Point(m) ;
        this.circle_r = r ;
        href = new String("http://") ;
        alt = new String() ;
        onClick = new String() ;
        onMouseOver = new String() ;
        onMouseOut = new String() ;
    }

    /**
     * Constructor for polygon objects
     */
    public Shape( Point first ) {
        this.type = TYPE_POLY ;
        poly = new Polygon() ;
        poly.addPoint((int) first.getX(), (int) first.getY()) ;
        href = new String("http://") ;
        alt = new String() ;
        onClick = new String() ;
        onMouseOver = new String() ;
        onMouseOut = new String() ;
    }

    public Shape( Polygon p ) {
        this.type = TYPE_POLY ;
        poly = p ;
        href = new String("http://") ;
        alt = new String() ;
        onClick = new String() ;
        onMouseOver = new String() ;
        onMouseOut = new String() ;
    }

    public void set_href( String href ) {
        if( href == null )
            return ;
        this.href = new String(href) ;
    }

    public void set_alt( String alt ) {
        if( alt == null )
            return ;
        this.alt = new String(alt) ;
    }

    public void set_onMouseOver( String s ) {
        if( s == null )
            return ;
        this.onMouseOver = new String(s) ;
    }

    public void set_onMouseOut( String s ) {
        if( s == null )
            return ;
        this.onMouseOut = new String(s) ;
    }

    public void set_onClick( String s ) {
        if( s == null )
            return ;
        this.onClick = new String(s) ;
    }

    public String get_href() {
        return href ;
    }

    public String get_alt() {
        return alt ;
    }

    public String get_typeString() {
        if( type == TYPE_RECT )
            return "Rectangle" ;
        else if( type == TYPE_CIRCLE )
            return "Circle" ;
        else
            return "Polygon" ;
    }

    public void draw( Graphics2D g2, double zoomFactor, int tx, int ty, Color col ) {
        float dash1[] = { 4.0f } ;
        Stroke oldStroke = g2.getStroke() ;
        BasicStroke bs = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 3.0f, dash1, 0.0f) ;
        g2.translate(tx, ty) ;

        if( type == TYPE_RECT ) {
            int x = ( int )( rect.getX() * zoomFactor ) ;
            int y = ( int )( rect.getY() * zoomFactor ) ;
            int w = ( int )( rect.getWidth() * zoomFactor ) ;
            int h = ( int )( rect.getHeight() * zoomFactor ) ;

            g2.setColor(col) ;
            g2.drawRect(x, y, w, h) ;
            g2.setStroke(bs) ;
            g2.setColor(Color.white) ;
            g2.drawRect(x, y, w, h) ;

            g2.setStroke(oldStroke) ;
            g2.setColor(col) ;
            g2.fillRect(x - 2, y - 2, 4, 4) ;
            g2.fillRect(x + w - 2, y - 2, 4, 4) ;
            g2.fillRect(x + w - 2, y + h - 2, 4, 4) ;
            g2.fillRect(x - 2, y + h - 2, 4, 4) ;
            g2.setColor(Color.white) ;
            g2.drawRect(x - 2, y - 2, 4, 4) ;
            g2.drawRect(x + w - 2, y - 2, 4, 4) ;
            g2.drawRect(x + w - 2, y + h - 2, 4, 4) ;
            g2.drawRect(x - 2, y + h - 2, 4, 4) ;
        }
        else if( type == TYPE_CIRCLE ) {
            int x = ( int )( circle_center.getX() * zoomFactor ) ;
            int y = ( int )( circle_center.getY() * zoomFactor ) ;
            int r = ( int )( circle_r * zoomFactor ) ;

            g2.setColor(col) ;
            g2.drawOval(x - r, y - r, r * 2, r * 2) ;
            g2.setStroke(bs) ;
            g2.setColor(Color.white) ;
            g2.drawOval(x - r, y - r, r * 2, r * 2) ;

            g2.setStroke(oldStroke) ;
            g2.setColor(col) ;
            g2.fillRect(x + r - 2, y - 2, 4, 4) ;
            g2.setColor(Color.white) ;
            g2.drawRect(x + r - 2, y - 2, 4, 4) ;

        }
        else if( type == TYPE_POLY ) {
            Polygon pg = poly ;
            if( zoomFactor != 1 ) {
                pg = new Polygon() ;
                for( int i = 0; i < poly.npoints; i++ )
                    pg.addPoint( (int)(poly.xpoints[i] * zoomFactor), 
                                 (int)(poly.ypoints[i] * zoomFactor) ) ;
            }
            g2.setColor(col) ;
            g2.drawPolygon(pg) ;
            g2.setStroke(bs) ;
            g2.setColor(Color.white) ;
            g2.drawPolygon(pg) ;

            g2.setStroke(oldStroke) ;
            g2.setColor(col) ;
            for( int i = 0; i < poly.npoints; i++ )
                g2.fillRect( (int)( poly.xpoints[i] * zoomFactor )- 2, 
                             (int)( poly.ypoints[i] * zoomFactor )- 2, 4, 4 ) ;
            g2.setColor(Color.white) ;
            for( int i = 0; i < poly.npoints; i++ )
                g2.drawRect( (int)( poly.xpoints[i] * zoomFactor ) - 2, 
                             (int)( poly.ypoints[i] * zoomFactor ) - 2, 4, 4 ) ;
        }

        g2.translate(-tx, -ty) ;
    }

    public void set_rect( int x1, int y1, int x2, int y2 ) {
        int x, y, w, h ;
        if( x2 > x1 ) {
            x = x1 ;
            w = x2 - x1 ;
        }
        else {
            x = x2 ;
            w = x1 - x2 ;
        }

        if( y2 > y1 ) {
            y = y1 ;
            h = y2 - y1 ;
        }
        else {
            y = y2 ;
            h = y1 - y2 ;
        }

        rect = new Rectangle(x, y, w, h) ;
    }

    public void set_rect_ul( int mx, int my ) {
        int x = rect.x, w = rect.width, y = rect.y, h = rect.height ;
        if( mx < rect.x + rect.width ) {
            x = mx ;
            w = rect.x + rect.width - mx ;
        }
        else {
            x = rect.x + rect.width - 1 ;
            w = 1 ;
        }
        if( my < rect.y + rect.height ) {
            y = my ;
            h = rect.y + rect.height - my ;
        }
        else {
            y = rect.y + rect.height - 1 ;
            h = 1 ;
        }

        rect.setLocation(x, y) ;
        rect.setSize(w, h) ;
    }

    public void set_rect_lr( int mx, int my ) {
        int w = mx - rect.x ;
        if( w < 1 )
            w = 1 ;
        int h = my - rect.y ;
        if( h < 1 )
            h = 1 ;
        rect.setSize(w, h) ;
    }

    public void set_rect_ur( int mx, int my ) {
        int y = rect.y, h = rect.height ;
        int w = mx - rect.x ;
        if( w < 1 )
            w = 1 ;
        if( my < rect.y + rect.height ) {
            y = my ;
            h = rect.y + rect.height - my ;
        }
        else {
            y = rect.y + rect.height - 1 ;
            h = 1 ;
        }
        rect.setLocation(rect.x, y) ;
        rect.setSize(w, h) ;
    }

    public void set_rect_ll( int mx, int my ) {
        int x = rect.x, w = rect.width ;
        if( mx < rect.x + rect.width ) {
            x = mx ;
            w = rect.x + rect.width - mx ;
        }
        else {
            x = rect.x + rect.width - 1 ;
            w = 1 ;
        }
        int h = my - rect.y ;
        if( h < 1 )
            h = 1 ;
        rect.setLocation(x, rect.y) ;
        rect.setSize(w, h) ;
    }

    public void set_circle( Point m, int r ) {
        circle_center = new Point(m) ;
        circle_r = r ;
    }

    public void set_polypoint( int p, int x, int y ) {
        poly.xpoints[p - 1] = x ;
        poly.ypoints[p - 1] = y ;
    }

    /**
     * adds a new point to the polygon and returns its position for moving it
     */
    public int add_polypoint( int x, int y ) {
        poly.addPoint(x, y) ;
        return poly.npoints ;
    }

    public void remove_polypoint( int p ) {
        if( type != TYPE_POLY || poly.npoints <= 3 )
            return ;
        Polygon pnew = new Polygon() ;
        for( int i = 0; i < poly.npoints; i++ )
            if( i != p - 1 )
                pnew.addPoint(poly.xpoints[i], poly.ypoints[i]) ;

        poly = pnew ;
    }

    public void remove_lastPolyPoint() {
        if( type != TYPE_POLY )
            return ;
        remove_polypoint(poly.npoints) ;
    }

    public int get_type() {
        return type ;
    }

    public int isKeyPoint( int mx, int my ) {
        if( type == TYPE_RECT ) {
            int x = (int) rect.getX(), y = (int) rect.getY() ;
            int w = (int) rect.getWidth(), h = (int) rect.getHeight() ;
            if( mx >= x - 2 && mx <= x + 2 && my >= y - 2 && my <= y + 2 )
                return POINT_RECT_UPPERLEFT ;
            if( mx >= x + w - 2 && mx <= x + w + 2 && my >= y - 2
                    && my <= y + 2 )
                return POINT_RECT_UPPERRIGHT ;
            if( mx >= x + w - 2 && mx <= x + w + 2 && my >= y + h - 2
                    && my <= y + h + 2 )
                return POINT_RECT_LOWERRIGHT ;
            if( mx >= x - 2 && mx <= x + 2 && my >= y + h - 2
                    && my <= y + h + 2 )
                return POINT_RECT_LOWERLEFT ;
            if( inside(mx, my) ) {
                mouseDragPosition = new Point(mx - x, my - y) ;
                return POINT_RECT_INSIDE ;
            }
        }
        else if( type == TYPE_CIRCLE ) {
            int x = (int) circle_center.x ;
            int y = (int) circle_center.y ;
            int r = circle_r ;

            if( mx >= x + r - 2 && mx <= x + r + 2 && my >= y - 2
                    && my <= y + 2 )
                return POINT_CIRCLE_RADIUS ;
            if( inside(mx, my) ) {
                mouseDragPosition = new Point(mx - x, my - y) ;
                return POINT_CIRCLE_INSIDE ;
            }
        }
        else if( type == TYPE_POLY ) {
            for( int i = 0; i < poly.npoints; i++ ) {
                if( mx >= poly.xpoints[i] - 3 && mx <= poly.xpoints[i] + 3
                        && my >= poly.ypoints[i] - 3
                        && my <= poly.ypoints[i] + 3 )
                    return i + 1 ;
            }
            if( inside(mx, my) ) {
                mouseDragPosition = new Point(mx - poly.xpoints[0], my
                        - poly.ypoints[0]) ;
                return POINT_POLY_INSIDE ;
            }
        }
        return 0 ;
    }

    public void moveKeyPoint( int p, int mx, int my ) {
        if( p == POINT_RECT_UPPERLEFT )
            set_rect_ul(mx, my) ;
        else if( p == POINT_RECT_LOWERRIGHT )
            set_rect_lr(mx, my) ;
        else if( p == POINT_RECT_UPPERRIGHT )
            set_rect_ur(mx, my) ;
        else if( p == POINT_RECT_LOWERLEFT )
            set_rect_ll(mx, my) ;
        else if( p == POINT_RECT_INSIDE )
            set_rect(mx - mouseDragPosition.x, my - mouseDragPosition.y, mx
                    - mouseDragPosition.x + rect.width, my
                    - mouseDragPosition.y + rect.height) ;
        else if( p == POINT_CIRCLE_RADIUS ) {
            if( mx > (int) circle_center.getX() )
                set_circle(new Point(circle_center.x, circle_center.y), mx
                        - circle_center.x) ;
        }
        else if( p == POINT_CIRCLE_INSIDE )
            set_circle(new Point(mx - mouseDragPosition.x, my
                    - mouseDragPosition.y), circle_r) ;

        else if( p == POINT_POLY_INSIDE ) {
            int dx = mx - mouseDragPosition.x - poly.xpoints[0] ;
            int dy = my - mouseDragPosition.y - poly.ypoints[0] ;
            for( int i = 0; i < poly.npoints; i++ )
                set_polypoint(i + 1, poly.xpoints[i] + dx, poly.ypoints[i] + dy) ;
        }
        else if( p > 0 ) // polygon point
            set_polypoint(p, mx, my) ;
    }

    public String get_html() {
        String str = new String() ;
        if( type == TYPE_RECT ) {
            str = new String("<area shape=rect coords=\"" + rect.x + ","
                    + rect.y + "," + (int) (rect.x + rect.width) + ","
                    + (int) (rect.y + rect.height)) ;
        }
        else if( type == TYPE_CIRCLE ) {
            str = new String("<area shape=circle coords=\"" + circle_center.x
                    + "," + circle_center.y + "," + circle_r) ;
        }
        else if( type == TYPE_POLY ) {
            str = new String("<area shape=poly coords=\"") ;
            for( int i = 0; i < poly.npoints; i++ ) {
                if( i > 0 )
                    str += "," ;
                str += "" + poly.xpoints[i] + "," + poly.ypoints[i] ;
            }
        }

        str += "\" href=\"" + href + "\" alt=\"" + alt + "\"" ;
        if( onClick != null && onClick.length() > 0 )
            str += " onClick=\"" + onClick + "\"" ;
        if( onMouseOver != null && onMouseOver.length() > 0 )
            str += " onMouseOver=\"" + onMouseOver + "\"" ;
        if( onMouseOut != null && onMouseOut.length() > 0 )
            str += " onMouseOut=\"" + onMouseOut + "\"" ;

        str += ">\n" ;
        return str ;
    }

    public boolean inside( int x, int y ) {
        if( type == TYPE_RECT ) {
            if( rect.contains(x, y) )
                return true ;
        }
        else if( type == TYPE_CIRCLE ) {
            int mr = (int) Math.sqrt((x - circle_center.x)
                    * (x - circle_center.x) + (y - circle_center.y)
                    * (y - circle_center.y)) ;
            if( mr < circle_r )
                return true ;
        }
        else if( type == TYPE_POLY ) {
            if( poly.contains(x, y) )
                return true ;
        }
        return false ;
    }

    /**
     * convert rect shape to polgygon
     */
    public void convert() {
        if( type != TYPE_RECT )
            return ;
        poly = new Polygon() ;
        poly.addPoint(rect.x, rect.y) ;
        poly.addPoint(rect.x + rect.width, rect.y) ;
        poly.addPoint(rect.x + rect.width, rect.y + rect.height) ;
        poly.addPoint(rect.x, rect.y + rect.height) ;
        type = TYPE_POLY ;
        rect = null ;
    }

    /**
     * convert circle shape to polygon
     * 
     * @param n
     *            number of circle segments in new polygon
     */
    public void convert( int n ) {
        if( type != TYPE_CIRCLE )
            return ;
        poly = new Polygon() ;
        for( int i = 0; i < n; i++ )
            poly.addPoint(
                    circle_center.x
                            + (int) (Math.cos(i * Math.PI * 2 / n) * circle_r),
                    circle_center.y
                            + (int) (Math.sin(i * Math.PI * 2 / n) * circle_r)) ;
        type = TYPE_POLY ;
        circle_center = null ;
    }

    public Point minDistance( int x, int y, Shape activeShape, int movingPoint ) {
        if( type == TYPE_POLY ) {
            Point p = new Point(10000, 10000) ;
            for( int i = 0; i < poly.npoints; i++ ) {
                Point np = new Point(poly.xpoints[i], poly.ypoints[i]) ;
                if( np.distance((double) x, (double) y) < p.distance(
                        (double) x, (double) y) )
                    if( !(activeShape == this && i + 1 == movingPoint) )
                        p = np ;
            }
            return p ;
        }
        else if( type == TYPE_RECT ) {
            if( activeShape == this )
                return null ;
            Point[] points = { rect.getLocation(),
                    new Point(rect.x + rect.width, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Point(rect.x, rect.y + rect.height) } ;
            Point p = points[0] ;
            for( int i = 1; i < 4; i++ )
                if( points[i].distance((double) x, (double) y) < p.distance(
                        (double) x, (double) y) )
                    p = points[i] ;
            return p ;
        }
        else { // type==TYPE_CIRCLE
            if( activeShape == this )
                return null ;
            Point[] points = { circle_center,
                    new Point(circle_center.x + circle_r, circle_center.y),
                    new Point(circle_center.x - circle_r, circle_center.y),
                    new Point(circle_center.x, circle_center.y - circle_r),
                    new Point(circle_center.x, circle_center.y + circle_r) } ;
            Point p = points[0] ;
            for( int i = 1; i < 5; i++ )
                if( points[i].distance((double) x, (double) y) < p.distance(
                        (double) x, (double) y) )
                    p = points[i] ;
            return p ;
        }
    }

    public int minXDistance( int x ) {
        if( type == TYPE_POLY ) {
            int p = 10000 ;
            for( int i = 0; i < poly.npoints; i++ ) {
                int np = poly.xpoints[i] ;
                if( Math.abs(x - np) < Math.abs(x - p) )
                    p = np ;
            }
            return p ;
        }
        else if( type == TYPE_RECT ) {
            int[] points = { rect.x, rect.x + rect.height } ;
            int p = 10000 ;
            for( int i = 0; i < points.length; i++ )
                if( Math.abs(x - points[i]) < Math.abs(x - p) )
                    p = points[i] ;
            return p ;
        }
        else { // type==TYPE_CIRCLE
            int[] points = { circle_center.x, circle_center.x + circle_r,
                    circle_center.x - circle_r } ;
            int p = 10000 ;
            for( int i = 0; i < points.length; i++ )
                if( Math.abs(x - points[i]) < Math.abs(x - p) )
                    p = points[i] ;
            return p ;
        }
    }

    public int minYDistance( int y ) {
        if( type == TYPE_POLY ) {
            int p = 10000 ;
            for( int i = 0; i < poly.npoints; i++ ) {
                int np = poly.ypoints[i] ;
                if( Math.abs(y - np) < Math.abs(y - p) )
                    p = np ;
            }
            return p ;
        }
        else if( type == TYPE_RECT ) {
            int[] points = { rect.y, rect.y + rect.height } ;
            int p = 10000 ;
            for( int i = 0; i < points.length; i++ )
                if( Math.abs(y - points[i]) < Math.abs(y - p) )
                    p = points[i] ;
            return p ;
        }
        else { // type==TYPE_CIRCLE
            int[] points = { circle_center.y, circle_center.y + circle_r,
                    circle_center.y - circle_r } ;
            int p = 10000 ;
            for( int i = 0; i < points.length; i++ )
                if( Math.abs(y - points[i]) < Math.abs(y - p) )
                    p = points[i] ;
            return p ;
        }
    }

    /**
     * Punkt mit Position (x,y) in Polygon NACH Punkt n einfügen
     */
    public void insertPoint( int n, int x, int y ) {
        if( type != TYPE_POLY )
            return ;
        Polygon pnew = new Polygon() ;
        for( int i = 0; i < poly.npoints; i++ ) {
            pnew.addPoint(poly.xpoints[i], poly.ypoints[i]) ;
            if( i == n )
                pnew.addPoint(x, y) ;
        }
        poly = pnew ;
    }

    /**
     * checken, ob Abstand von Maus zu einer Polygon-Linie kleiner als 5 Pixel.
     * Wenn ja, neuen Punkt an Mausposition einfügen
     */
    public boolean tryAddPoint( int x, int y ) {
        if( type != TYPE_POLY )
            return false ;
        double minDist = Line2D.ptSegDist(
                (double) poly.xpoints[poly.npoints - 1],
                (double) poly.ypoints[poly.npoints - 1],
                (double) poly.xpoints[0], (double) poly.ypoints[0], (double) x,
                (double) y) ;
        int minPoint = poly.npoints - 1 ;
        for( int i = 1; i < poly.npoints; i++ ) {
            double l = Line2D.ptSegDist((double) poly.xpoints[i - 1],
                    (double) poly.ypoints[i - 1], (double) poly.xpoints[i],
                    (double) poly.ypoints[i], (double) x, (double) y) ;
            if( l < minDist ) {
                minDist = l ;
                minPoint = i - 1 ;
            }
        }

        if( minDist < 10.0 ) {
            insertPoint(minPoint, x, y) ;
            return true ;
        }

        return false ;
    }

    public boolean removePointAt( int x, int y ) {
        if( type != TYPE_POLY )
            return false ;

        for( int i = 0; i < poly.npoints; i++ ) {
            if( x >= poly.xpoints[i] - 2 && x <= poly.xpoints[i] + 2
                    && y >= poly.ypoints[i] - 2 && y <= poly.ypoints[i] + 2 ) {
                remove_polypoint(i + 1) ;
                return true ;
            }
        }

        return false ;
    }

    public String getCoordString( int mouseX, int mouseY ) {
        if( type == TYPE_POLY ) {
            return "x=" + mouseX + "  y=" + mouseY ;
        }
        else if( type == TYPE_RECT ) {
            return "x=" + rect.x + "  y=" + rect.y + "  w=" + rect.width
                    + "  h=" + rect.height ;
        }
        else { // type==TYPE_CIRCLE
            return "x=" + circle_center.x + "  y=" + circle_center.y + "  r="
                    + circle_r ;
        }
    }
}