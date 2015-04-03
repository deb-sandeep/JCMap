package com.sandy.jcmap;

import java.awt.event.MouseAdapter ;
import java.awt.event.MouseEvent ;

import javax.swing.Icon ;
import javax.swing.JLabel ;

import org.apache.log4j.Logger ;

import com.sandy.jcmap.util.CMapUtil ;
import com.sandy.jcmap.util.CMapUtil.SelectedElement ;

public class CMapImageLabel extends JLabel {

    private static final long serialVersionUID = 1L ;
    static final Logger logger = Logger.getLogger( CMapImageLabel.class ) ;
    
    private CMapImage image = null ;
    private JCMap     app   = null ;
    
    private class MouseListener extends MouseAdapter {

        @Override
        public void mouseClicked( MouseEvent e ) {
            String url = image.getActivatedElementURL(e.getX(), e.getY()) ;
            SelectedElement element = CMapUtil.parseURL( url ) ;
            if( element != null ) {
                switch ( e.getClickCount() ){
                    case 2:
                        if( element.isHyperLink && element.isConcept ) {
                            app.conceptDrillDownRequested( element.alias ) ;
                        }
                        break ;
                    case 1:
                        app.elementSelected( element.alias ) ;
                        break ;
                }
            }
        }
    } ;
    
    public CMapImageLabel( JCMap app ) {
        super() ;
        this.app = app ;
        addMouseListener( new MouseListener() ) ;
    }

    @Override
    public void setIcon( Icon icon ) {
        super.setIcon( icon ) ;
        this.image = ( CMapImage )icon ;
    }
}
