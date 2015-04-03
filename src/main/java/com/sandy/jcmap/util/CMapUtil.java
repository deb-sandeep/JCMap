package com.sandy.jcmap.util;

import org.apache.commons.lang.StringEscapeUtils ;


public class CMapUtil {

    public static class SelectedElement {
        public String alias ;
        public boolean isHyperLink = false ;
        public boolean isConcept   = false ;
    }
    
    public static SelectedElement parseURL( String url ) {
        
        SelectedElement element = null ;
        if( url != null ) {
            element = new SelectedElement() ;
            char c1 = url.charAt( 0 ) ;
            char c2 = url.charAt( 1 ) ;
            
            if( c1 == 'C' ) {
                element.isConcept = true ;
            }
            if( c2 == 'H' ) {
                element.isHyperLink = true ;
            }
            element.alias = StringEscapeUtils.unescapeHtml( url.substring( 2 ) ) ;
        }
        
        return element ;
    }
}
