package com.sandy.jcmap;

import imagemap.HTMLParser ;
import imagemap.Shape ;
import imagemap.ShapeList ;

import java.awt.Color ;
import java.awt.Component ;
import java.awt.Graphics ;
import java.awt.Graphics2D ;
import java.awt.image.BufferedImage ;
import java.io.File ;

import javax.swing.ImageIcon ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.jcmap.util.CMapUtil ;
import com.sandy.jcmap.util.CMapUtil.SelectedElement ;

public class CMapImage extends ImageIcon {

    private static final long serialVersionUID = 1L ;
    static final Logger logger = Logger.getLogger( CMapImage.class ) ;
    
    private JCMap app = null ;
    private ShapeList shapeList = new ShapeList() ;
    private int tx = 0 ;
    private int ty = 0 ;
    private double scaleFactor = 1.0 ;
    
    public CMapImage( double scaleFactor, BufferedImage img, File cmapFile, JCMap app ) {
        super( img ) ;
        this.scaleFactor = scaleFactor ;
        this.app = app ;
        if( cmapFile != null ) {
            parseCMap( cmapFile ) ;
        }
    }
    
    private void parseCMap( File cmapFile ) {
        try {
            String cmap = FileUtils.readFileToString( cmapFile ) ;
            HTMLParser parser = new HTMLParser( cmap ) ;
            parser.createShapeList( shapeList ) ;
        }
        catch( Exception e ) {
            e.printStackTrace(); 
        }
    }

    @Override
    public synchronized void paintIcon( Component c, Graphics g, int x, int y ) {
        super.paintIcon( c, g, x, y ) ;
        this.tx = x ;
        this.ty = y ;
        for( Shape s : shapeList.getShapes() ) {
            
            Color color = Color.red ;
            String alias = s.get_href() ;
            SelectedElement elem = CMapUtil.parseURL( alias ) ;
            if( elem.isConcept && elem.isHyperLink ) {
                File file = new File( app.getCurDir(), elem.alias + ".cmap" ) ;
                if( file.exists() ) {
                    color = Color.green ;
                }
                s.draw( (Graphics2D)g, scaleFactor, x, y, color ) ;
            }
        }
    }
    
    public String getActivatedElementURL( int x, int y ) {
        String alias = null ;

        x = (int)(( x - tx )/scaleFactor) ;
        y = (int)(( y - ty )/scaleFactor) ;
        
        for( Shape s : shapeList.getShapes() ) {
            if( s.inside(x, y) ) {
                alias = s.get_href() ;
                break ;
            }
        }
        
        return alias ;
    }
}
