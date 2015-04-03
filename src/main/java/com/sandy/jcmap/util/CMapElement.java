package com.sandy.jcmap.util;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

public class CMapElement {

    private static abstract class AbstractCMapEntity<T> {

        private String id    = "" ;
        private String alias = "" ;
        private String label = null ;
        private List<T> nextEntities = new ArrayList<T>() ;
        private Map<String, String> attrs = new HashMap<>() ;
        
        public AbstractCMapEntity( String id, String label ) {
            this.id    = id ;
            this.alias = label ;
            this.label = label ;
        }
        
        public AbstractCMapEntity( String id, String label, String alias ) {
            this.id    = id ;
            this.alias = alias ;
            this.label = label ;
        }
        
        public List<T> getNextEntities() {
            return nextEntities ;
        }
        
        public void addNextEntity( T entity ) {
            this.nextEntities.add( entity ) ;
        }
        
        public String getId() {
            return id ;
        }
        
        public String getLabel() {
            return label ;
        }
        
        public String getAlias() {
            return alias ;
        }
        
        public abstract Map<String, String> getAttrs() ;
    }

    public static class LinkingPhrase extends AbstractCMapEntity<Concept> {

        public static int counter = 0 ;
        
        public LinkingPhrase( String label ) {
            super( ("L_" + counter++), label ) ;
        }
        
        public LinkingPhrase( String label, String alias ) {
            super( ("L_" + counter++), label, alias ) ;
        }
        
        public boolean isNullLinkingPhrase() {
            return super.getLabel() == null ;
        }
        
        public String toString() {
            return getId() + " [ >" + getLabel() + "< " + getAttrs() + " ]" ;
        }
        
        public Map<String, String> getAttrs() {
            super.attrs.put( "URL", "LN" + getAlias() ) ;
            return super.attrs ;
        }
        
        public static void resetCounter() { counter = 0 ; }
    }

    public static class Concept extends AbstractCMapEntity<LinkingPhrase> {
        
        public static int counter = 0 ;
        private int numIncomingConnections = 0 ;
        private boolean isHyperLink = false ;
        private boolean isExportable = false ;

        public Concept( String label ) {
            super( ("C_" + counter++), label ) ;
        }
        
        public Concept( String label, String alias ) {
            super( ("C_" + counter++), label, alias ) ;
        }
        
        public void setHyperLinkFlag() {
            this.isHyperLink = true ;
        }
        
        public void setExportable() {
            this.isExportable = true ;
        }
        
        public boolean isLink() {
            return this.isHyperLink ;
        }
        
        public boolean isExportable() {
            return this.isExportable ;
        }
        
        public void incrementNumIncoming() {
            this.numIncomingConnections++ ;
        }
        
        public void resetNumIncoming() {
            this.numIncomingConnections = 0 ;
        }
        
        public boolean isZombie() {
            if( numIncomingConnections == 0 ) {
                if( super.getNextEntities().isEmpty() ) {
                    return true ;
                }
            }
            return false ;
        }
        
        public void mergeAttributes( Map<String, String> attrMap ) {
            super.attrs.putAll( attrMap ) ;
        }
        
        public Map<String, String> getAttrs() {
            if( super.attrs.containsKey( "fillcolor" ) && 
                !super.attrs.containsKey( "style" ) ) {
                super.attrs.put( "style", "filled" ) ;
            }
            
            super.attrs.put( "URL", getURL() ) ;
            
            return super.attrs ;
        }
        
        private String getURL() {
            StringBuffer buffer = new StringBuffer() ;
            
            buffer.append( "C" ) ;
            buffer.append( isHyperLink ? "H" : "N" ) ;
            buffer.append( getAlias() ) ;
            
            return buffer.toString() ;
        }
        
        public boolean equals( Object o ) {
            Concept c0 = ( Concept )o ;
            return this.getAlias().equals( c0.getAlias() ) ;
        }
        
        public String toString() {
            return getId() + " [ " + getAlias() + " ( " + getLabel() + " ) " + getAttrs() + " ]" ;
        }
        
        public static void resetCounter() { counter = 0 ; }
    }
    
    private LinkedHashMap<String, Concept> concepts       = null ;
    private List<LinkingPhrase>            linkingPhrases = null ;
    private String                         imgFileName    = null ;
    private List<List<Concept>>            ranks          = new ArrayList<>() ;
    private Map<String, CMapElement>       includedCMaps  = null ;
    
    private Map<String, String> globalGraphAttrs = new HashMap<String, String>() ;
    private Map<String, String> globalNodeAttrs  = new HashMap<String, String>() ;
    private Map<String, String> globalEdgeAttrs  = new HashMap<String, String>() ;
    
    private boolean hasRoot = false ;
    
    public CMapElement( LinkedHashMap<String, Concept> concepts,
                        List<LinkingPhrase> linkingPhrases,
                        Map<String, String> globalGA,
                        Map<String, String> globalNA,
                        Map<String, String> globalEA,
                        List<List<Concept>> ranks,
                        Map<String, CMapElement> includedCMaps ) {
        
        this.concepts       = concepts ;
        this.linkingPhrases = linkingPhrases ;
        this.ranks          = ranks ;
        this.includedCMaps  = includedCMaps ;
        
        this.globalGraphAttrs.putAll( globalGA ) ;
        this.globalNodeAttrs.putAll( globalNA ) ;
        this.globalEdgeAttrs.putAll( globalEA ) ;
    }
    
    public void setHasRoot( boolean hasRoot ) {
        this.hasRoot = hasRoot ;
    }
    
    public boolean hasRoot() { return this.hasRoot ; }
    
    public LinkedHashMap<String, Concept> getConcepts() {
        return this.concepts ;
    }
    
    public Map<String, CMapElement> getIncludedCMaps() {
        return includedCMaps ;
    }
    
    public List<LinkingPhrase> getLinkingPhrases() {
        return this.linkingPhrases ;
    }
    
    public Map<String, String> getGlobalGraphAttrs() {
        return this.globalGraphAttrs ;
    }

    public Map<String, String> getGlobalNodeAttrs() {
        if( globalNodeAttrs.containsKey( "fillcolor" ) && 
            !globalNodeAttrs.containsKey( "style" ) ) {
            globalNodeAttrs.put( "style", "filled" ) ;
        }
        return this.globalNodeAttrs ;
    }
    
    public Map<String, String> getGlobalEdgeAttrs() {
        return this.globalEdgeAttrs ;
    }
    
    public void setImgFileName( String fName ) {
        this.imgFileName = fName ;
    }
    
    public List<List<Concept>> getRanks() {
        return this.ranks ;
    }
    
    public String getImgFileName() {
        return this.imgFileName ;
    }
    
    public void sanitizeForExport() {
        this.linkingPhrases.clear() ;
        this.ranks.clear() ;
        this.includedCMaps.clear() ;
        for( Iterator<String> i = concepts.keySet().iterator(); i.hasNext(); ) {
            Concept c = concepts.get(i.next()) ;
            if( c.isExportable ) {
                c.getNextEntities().clear() ;
                c.resetNumIncoming() ;
            }
            else {
                i.remove() ;
            }
        }
    }
    
    public ArrayList<String> getMatchingConcepts( String startFrag ) {
        ArrayList<String> potentialMatches = new ArrayList<>() ;
        for( String s : concepts.keySet() ) {
            if( s.startsWith( startFrag ) ) {
                potentialMatches.add( s ) ;
            }
        }
        
        if( includedCMaps != null ) {
            for( CMapElement cmap : includedCMaps.values() ) {
                for( String s : cmap.concepts.keySet() ) {
                    if( s.startsWith( startFrag ) ) {
                        potentialMatches.add( s ) ;
                    }
                }
            }
        }
        return potentialMatches ;
    }

    public ArrayList<String> getMatchingLPs( String startFrag ) {
        ArrayList<String> potentialMatches = new ArrayList<>() ;
        
        for( LinkingPhrase lp : linkingPhrases ) {
            if( lp.getAlias() != null ) {
                if( lp.getAlias().startsWith( startFrag ) ) {
                    potentialMatches.add( lp.getAlias() ) ;
                }
            }
        }
        
        return potentialMatches ;
    }
}
