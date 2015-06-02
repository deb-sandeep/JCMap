package com.sandy.jcmap.util;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import org.apache.commons.lang.WordUtils ;
import org.apache.log4j.Logger ;

import com.sandy.core.StringUtil ;
import com.sandy.jcmap.util.CMapElement.Concept ;
import com.sandy.jcmap.util.CMapElement.LinkingPhrase ;

public class CMapDotSerializer {

    static final Logger logger = Logger.getLogger( CMapDotSerializer.class ) ;
    
    private CMapElement cmap = null ;
    private boolean noWrapNodeText = false ;
    private int wrapLen = 15 ;
    
    public CMapDotSerializer( CMapElement cmap ) {
        this.cmap = cmap ;
        String val = cmap.getGlobalGraphAttrs().get( JCMapInteralAttributes.INTERNAL_NA_WRAP_LEN ) ;
        if( val != null ) {
            if( val.equals( "nowrap" ) ) {
                noWrapNodeText = true ;
            }
            else {
                try {
                    wrapLen = Integer.parseInt( val ) ;
                }
                catch( NumberFormatException e ) {
                    // Ignore
                }
            }
        }
    }
    
    public String convertCMaptoDot() {
        
        StringBuffer buffer = new StringBuffer() ;
        
        buffer.append( "digraph G {\n" ) ;
        buffer.append( "\n" ) ;
        buffer.append( "graph [\n" ) ;
        appendAttrs( cmap.getGlobalGraphAttrs(), buffer ) ;
        buffer.append( "]\n" ) ;
        buffer.append( "\n" ) ;
        buffer.append( "node [\n" ) ;
        appendAttrs( cmap.getGlobalNodeAttrs(), buffer ) ;
        buffer.append( "]\n" ) ;
        buffer.append( "\n" ) ;
        buffer.append( "edge [\n" ) ;
        appendAttrs( cmap.getGlobalEdgeAttrs(), buffer ) ;
        buffer.append( "]\n" ) ;
        
        buffer.append( "\n" ) ;
        buffer.append( getNodeDefinitions( cmap ) ) ; 
        
        buffer.append( "\n" ) ;
        buffer.append( getEdgeDefinitions( cmap ) ) ; 
        
        buffer.append( "\n" ) ;
        buffer.append( "}" ) ;
        
        return buffer.toString() ;
    }
    
    private void appendAttrs( Map<String, String> attrs, StringBuffer buffer ) {
        
        for( String key : attrs.keySet() ) {
            if( !JCMapInteralAttributes.ATTR_LIST.contains( key ) ) {
                String val = attrs.get( key ) ;
                buffer.append( "    " + key + " = " + val + " ;\n" ) ;
            }
        }
    }
    
    private StringBuffer getNodeDefinitions( CMapElement cmap ) {
        
        StringBuffer buffer = new StringBuffer() ;
        
        List<Concept> zombieConcepts = new ArrayList<>() ;
        List<Concept> graphConcepts  = new ArrayList<>() ;
        
        for( Concept c : cmap.getConcepts().values() ) {
            if( c.isZombie() ) { zombieConcepts.add( c ) ; }
            else { graphConcepts.add( c ) ; }
        }
        
        constructGraphConceptNodeDefinitions( buffer, graphConcepts, cmap.hasRoot() ) ;
        constructZombieNodeDefinitions( buffer, zombieConcepts ) ;
        constructLinkPhrasesDefinitions( cmap, buffer ) ;
        constructRankDefinitions( cmap, buffer ) ;
        
        for( String key : cmap.getIncludedCMaps().keySet() ) {
            CMapElement icmap = cmap.getIncludedCMaps().get(key) ;
            constructImportedNodeDefinitions( buffer, key, icmap, cmap ) ;
        }
        
        return buffer ;
    }

    private void constructRankDefinitions( CMapElement cmap, StringBuffer buffer ) {
        if( !cmap.getRanks().isEmpty() ) {
            buffer.append( "\n" ) ;
            for( List<Concept> rank : cmap.getRanks() ) {
                buffer.append( "{ rank=same; " ) ;
                for( Concept c : rank ) {
                    buffer.append( c.getId() + "; " ) ;
                }
                buffer.append( "}\n" ) ;
            }
        }
    }

    private void constructLinkPhrasesDefinitions( CMapElement cmap,
                                                  StringBuffer buffer ) {
        buffer.append( "\n" ) ;
        buffer.append( "node [\n" ) ;
        buffer.append( "    fontcolor = \"blue\" ;\n" ) ;
        buffer.append( "    fontsize = 10 ;\n" ) ;
        buffer.append( "    shape = \"plaintext\" ;\n" ) ;
        buffer.append( "    style = \"filled\" ;\n" ) ;
        buffer.append( "    fillcolor = \"transparent\" ;\n" ) ;
        buffer.append( "]\n\n" ) ;
        
        for( LinkingPhrase p : cmap.getLinkingPhrases() ) {
            if( !p.isNullLinkingPhrase() ) {
                buffer.append( p.getId() + "[" + 
                        "label=\"" + wrapLabel( p.getLabel() ) + "\" ;" ) ;
                for( String k : p.getAttrs().keySet() ) {
                    buffer.append( k + "=\"" + p.getAttrs().get(k) + "\" ;" ) ;
                }
                buffer.append( "] ;\n" ) ;
            }
        }
    }

    private void constructZombieNodeDefinitions( StringBuffer buffer,
                                                 List<Concept> zombieConcepts ) {
        if( !zombieConcepts.isEmpty() ) {
            buffer.append( "subgraph cluster_zombie {\n" ) ;
            buffer.append( "    color = grey ;\n\n" ) ;  
            buffer.append( "    node [\n" ) ;
            buffer.append( "        color = sienna1 ;\n" ) ;
            buffer.append( "        shape = box ;\n" ) ;
            buffer.append( "        style = \"solid\" ;\n" ) ;
            buffer.append("         width = 1 ;\n") ;
            buffer.append( "    ]\n\n" ) ;
            
            String[] cids = new String[zombieConcepts.size()] ;
            int i = 0 ;
            for( Concept c : zombieConcepts ) {
                buffer.append( "    " + c.getId() + "[" ) ;  
                buffer.append( "label=\"" + wrapLabel( c.getLabel() ) + "\"" ) ;
                for( String k : c.getAttrs().keySet() ) {
                    buffer.append( k + "=\"" + c.getAttrs().get(k) + "\" ;" ) ;
                }
                buffer.append( "] ;\n" ) ;
                cids[i++] = c.getId() ;
            }
            
            buffer.append( "    edge [color=transparent] ;\n\n" ) ;
            buffer.append( "   " ) ;
            for( i=0; i<cids.length-1; i++ ) {
                buffer.append( cids[i] + " -> " ) ;
            }
            buffer.append( cids[cids.length-1] + " ;\n" ) ;
            
            buffer.append( "\n}\n" ) ;
        }
    }

    private void constructImportedNodeDefinitions( StringBuffer buffer,
                                                   String       key,
                                                   CMapElement  impCMap,
                                                   CMapElement  cmap ) {
        
        List<Concept> concepts = new ArrayList<>() ;
        for( Concept c : impCMap.getConcepts().values() ) {
            if( !cmap.getConcepts().values().contains( c ) ) {
                concepts.add( c ) ;
            }
        }
        
        if( concepts.isEmpty() ) {
            return ;
        }
        
        String normKey = key.replace( " ", "_" ) ;
        
        buffer.append("subgraph cluster_" + normKey + " {\n") ;
        buffer.append("    label    = " + key + " ;\n") ;
        buffer.append( "   fontname = Calibri" + " ;\n" ) ;
        buffer.append( "   fontsize = 11" + " ;\n" ) ;
        buffer.append("    color    = sienna1 ;\n") ;
        buffer.append("    penwidth = 0.3 ;\n") ;
        buffer.append("    node [\n") ;
        buffer.append("        color = red ;\n") ;
        buffer.append("        shape = box ;\n") ;
        buffer.append("        style = \"solid\" ;\n") ;
        buffer.append("        width = 1 ;\n") ;
        buffer.append("    ]\n\n") ;

        String[] cids = new String[concepts.size()] ;
        int i = 0 ;
        for( Concept c : concepts ) {
            
            String id = normKey + "_" + c.getId() ;
            
            buffer.append("    " + id + "[") ;
            buffer.append("label=\"" + wrapLabel(c.getLabel()) + "\"") ;
            for( String k : c.getAttrs().keySet() ) {
                buffer.append( k + "=\"" + c.getAttrs().get(k) + "\" ;" ) ;
            }
            buffer.append("] ;\n") ;
            cids[i++] = id ;
        }

        buffer.append("    edge [color=transparent] ;\n\n") ;
        buffer.append("   ") ;
        for( i = 0; i < cids.length - 1; i++ ) {
            buffer.append(cids[i] + " -> ") ;
        }
        buffer.append(cids[cids.length - 1] + " ;\n") ;

        buffer.append("\n}\n") ;
    }

    private void constructGraphConceptNodeDefinitions( StringBuffer buffer,
                                                       List<Concept> graphConcepts,
                                                       boolean hasRoot ) {
        
        boolean isFirst = true ;
        
        for( Concept c : graphConcepts ) {
            buffer.append( c.getId() + "[" ) ;  
            buffer.append( "label=\"" + wrapLabel( c.getLabel() ) + "\" ;" ) ;
            if( isFirst && hasRoot ) {
                isFirst = false ;
                buffer.append( "fillcolor=\"cyan\" ;" ) ;
                buffer.append( "fontsize=\"11\" ;" ) ;
                buffer.append( "color=\"cyan\" ;" ) ;
                buffer.append( "style=\"filled\" ;" ) ;
            }
            else {
                buffer.append( "fillcolor=\"#e8fffe\" ;" ) ;
                buffer.append( "fontsize=\"10\" ;" ) ;
                buffer.append( "color=\"#81fff6\" ;" ) ;
                buffer.append( "style=\"filled\" ;" ) ;
            }
            for( String k : c.getAttrs().keySet() ) {
                buffer.append( k + "=\"" + c.getAttrs().get(k) + "\" ;" ) ;
            }
            buffer.append( "] ;\n" ) ;
        }
    }
    
    private String wrapLabel( String input ) {
        
        String retVal = null ;
        
        if( input.trim().startsWith( "<l>" ) && input.trim().endsWith( "</l>" ) ) {
            input = input.substring( "<l>".length(), input.length() - "</l>".length() ) ;
            retVal = "" ;
            String[] bullets = input.split( "\\*" ) ;
            for( String bullet : bullets ) {
                if( StringUtil.isNotEmptyOrNull( bullet ) ) {
                    retVal += "* " + bullet.trim() + "\\n" ;
                }
            }
            retVal = retVal.trim() ;
        }
        else {
            if( !noWrapNodeText ) {
                retVal = WordUtils.wrap( input, wrapLen, "\\n", false ) ; 
            }
            else {
                retVal = input ;
            }
        }
        return retVal ;
    }

    private StringBuffer getEdgeDefinitions( CMapElement cmap ) {
        
        StringBuffer buffer = new StringBuffer() ;
        
        for( Concept c : cmap.getConcepts().values() ) {
            for( LinkingPhrase p : c.getNextEntities() ) {
                if( !p.isNullLinkingPhrase() ) {
                    buffer.append( c.getId() + " -> " + p.getId() + " ;\n" ) ;
                }
                else {
                    for( Concept cNext : p.getNextEntities() ) {
                        buffer.append( c.getId() + " -> " + cNext.getId() + " ;\n"  ) ;
                    }
                }
            }
        }

        for( LinkingPhrase p : cmap.getLinkingPhrases() ) {
            if( !p.isNullLinkingPhrase() ) {
                for( Concept c : p.getNextEntities() ) {
                    buffer.append( p.getId() + " -> " + c.getId() + " ;\n"  ) ;
                }
            }
        }
        return buffer ;
    }
}
