package com.sandy.jcmap.util;

import java.io.File ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.LinkedHashMap ;
import java.util.List ;
import java.util.Map ;

import org.antlr.v4.runtime.ANTLRErrorListener ;
import org.antlr.v4.runtime.ANTLRInputStream ;
import org.antlr.v4.runtime.CommonTokenStream ;
import org.antlr.v4.runtime.tree.ParseTree ;
import org.antlr.v4.runtime.tree.ParseTreeWalker ;
import org.antlr.v4.runtime.tree.TerminalNode ;
import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.jcmap.CMapBaseListener ;
import com.sandy.jcmap.CMapLexer ;
import com.sandy.jcmap.CMapParser ;
import com.sandy.jcmap.CMapParser.Cluster_concept_listContext ;
import com.sandy.jcmap.CMapParser.ConceptContext ;
import com.sandy.jcmap.CMapParser.Concept_aliasContext ;
import com.sandy.jcmap.CMapParser.Concept_labelContext ;
import com.sandy.jcmap.CMapParser.Concept_listContext ;
import com.sandy.jcmap.CMapParser.Concept_list_stmtContext ;
import com.sandy.jcmap.CMapParser.Edge_attrsContext ;
import com.sandy.jcmap.CMapParser.Export_concept_stmtContext ;
import com.sandy.jcmap.CMapParser.Graph_attrsContext ;
import com.sandy.jcmap.CMapParser.Id_list_or_stringContext ;
import com.sandy.jcmap.CMapParser.Include_stmtContext ;
import com.sandy.jcmap.CMapParser.Is_linkContext ;
import com.sandy.jcmap.CMapParser.Linking_phraseContext ;
import com.sandy.jcmap.CMapParser.Naked_concept_listContext ;
import com.sandy.jcmap.CMapParser.No_root_tagContext ;
import com.sandy.jcmap.CMapParser.Node_attrsContext ;
import com.sandy.jcmap.CMapParser.NvpContext ;
import com.sandy.jcmap.CMapParser.Nvp_listContext ;
import com.sandy.jcmap.CMapParser.Preposition_stmtContext ;
import com.sandy.jcmap.CMapParser.Rank_concept_stmtContext ;
import com.sandy.jcmap.CMapParser.ValueContext ;
import com.sandy.jcmap.util.CMapElement.Concept ;
import com.sandy.jcmap.util.CMapElement.LinkingPhrase ;

public class CMapBuilder extends CMapBaseListener {

    static final Logger logger = Logger.getLogger( CMapBuilder.class ) ;
    
    private Map<String, String> globalGA = new HashMap<>() ;
    private Map<String, String> globalNA = new HashMap<>() ;
    private Map<String, String> globalEA = new HashMap<>() ;
    
    private LinkedHashMap<String, Concept> concepts       = new LinkedHashMap<>() ;
    private List<LinkingPhrase>            linkingPhrases = new ArrayList<>() ;
    private List<List<Concept>>            ranks          = new ArrayList<>() ;
    
    private boolean hasRoot = true ;
    
    private Map<String, CMapElement> includes = new LinkedHashMap<>() ;
    private File baseDir = null ;
    private CMapParser parser = null ; 
    private ANTLRErrorListener errorListener = null ;
    
    private boolean ignoreImports = false ;
    
    public CMapBuilder() {
    }
    
    public void setErrorListener( ANTLRErrorListener errList ) {
        this.errorListener = errList ;
    }
    
    public CMapBuilder( File baseDir ) {
        this.baseDir = baseDir ;
    }
    
    public CMapElement buildCMapElement( File file ) throws Exception {
        return buildCMapElement( file, false ) ;
    }
    
    public CMapElement buildCMapElement( File file, boolean ignoreImports ) throws Exception {
        String content = FileUtils.readFileToString( file ) ;
        return buildCMapElement( content, ignoreImports ) ;
    }
    
    public CMapElement buildCMapElement( String input ) {
        return buildCMapElement( input, false ) ;
    }
    
    public CMapElement buildCMapElement( String input, boolean ignoreImports ) {
        
        this.ignoreImports = ignoreImports ; 
        LinkingPhrase.resetCounter() ;
        Concept.resetCounter() ;
        
        globalGA.clear(); 
        globalGA.put( "ranksep", "0.2" ) ;
        globalGA.put( "bgcolor", "transparent" ) ;
        
        globalNA.clear() ;
        globalNA.put( "fontname", "Calibri" ) ;
        globalNA.put( "fontsize", "10" ) ;
        globalNA.put( "shape", "box" ) ;
        globalNA.put( "height", "0.2" ) ;
        globalNA.put( "margin", "0.03" ) ;
        globalNA.put( "penwidth", "0.3" ) ;
        globalNA.put( "color", "gray" ) ;
        globalNA.put( "style", "filled" ) ;
        globalNA.put( "fillcolor", "white" ) ;
        
        globalEA.clear() ;
        globalEA.put( "arrowsize", "0.5" ) ;
        globalEA.put( "penwidth", "0.2" ) ;
        globalEA.put( "color", "blue" ) ;
        
        parse( input ) ;
        
        CMapElement element =  new CMapElement( concepts, linkingPhrases, 
                                                globalGA, globalNA, 
                                                globalEA, ranks, includes ) ;
        element.setHasRoot( hasRoot ) ;
        return element ;
    }
    
    private void parse( String input ) {
        
        ANTLRInputStream  ais    = new ANTLRInputStream( input ) ;
        CMapLexer         lexer  = new CMapLexer( ais ) ;
        CommonTokenStream tokens = new CommonTokenStream( lexer ) ;
        
        parser = new CMapParser( tokens ) ;
        parser.removeErrorListeners() ;
        if( errorListener != null ) {
            parser.addErrorListener( errorListener ) ;
        }
        
        ParseTree         tree   = parser.cmap() ;
        ParseTreeWalker   walker = new ParseTreeWalker() ;
        
        walker.walk( this, tree ) ;
    }
    
    @Override
    public void exitInclude_stmt( Include_stmtContext ctx ) {
        
        if( ignoreImports ) return ;
        
        File file = null ;
        CMapElement cmap = null ;
        String inclName = convertIDListOrString( ctx.id_list_or_string() ) ; 
        
        if( this.baseDir != null ) {
            file = new File( this.baseDir, inclName + ".cmap" ) ;
        }
        else {
            file = new File( inclName + ".cmap" ) ;
        }
        
        try {
            if( file.exists() ) {
                CMapBuilder builder = new CMapBuilder(baseDir) ;
                cmap = builder.buildCMapElement( file, true ) ;
                cmap.sanitizeForExport() ;
                includes.put( inclName, cmap ) ;
            }
        }
        catch( Exception e ) {
            logger.error( "Exception while parsing file", e ) ;
        }
    }
    
    @Override
    public void exitNo_root_tag( No_root_tagContext ctx ) {
        this.hasRoot = false ;
    }

    @Override
    public void exitGraph_attrs( Graph_attrsContext ctx ) {
        loadNVPs( globalGA, ctx.nvp_list() ) ;
    }

    @Override
    public void exitNode_attrs( Node_attrsContext ctx ) {
        loadNVPs( globalNA, ctx.nvp_list() ) ;
    }

    @Override
    public void exitEdge_attrs( Edge_attrsContext ctx ) {
        loadNVPs( globalEA, ctx.nvp_list() ) ;
    }
    
    @Override
    public void exitConcept_list_stmt( Concept_list_stmtContext ctx ) {
        
        Concept_listContext listCtx  = ctx.concept_list() ; 
        getConcepts( listCtx ) ;
        // These are just concept definitions, we do not have to do any processing
        // post they are loaded and registered properly.
    }
    
    @Override
    public void exitExport_concept_stmt( Export_concept_stmtContext ctx ) {
        Concept_listContext listCtx  = ctx.concept_list() ;
        if( listCtx != null ) {
            List<Concept> concepts = getConcepts( listCtx ) ;
            if( concepts != null ) {
                for( Concept c : concepts ) {
                    c.setExportable() ;
                }
            }
        }
    }
    
    @Override
    public void exitRank_concept_stmt( Rank_concept_stmtContext ctx ) {
        Concept_listContext listCtx  = ctx.concept_list() ; 
        if( listCtx != null ) {
            List<Concept> concepts = getConcepts( listCtx ) ;
            if( concepts != null && !concepts.isEmpty() ) {
                ranks.add( concepts ) ;
            }
        }
    }

    @Override
    public void exitPreposition_stmt( Preposition_stmtContext ctx ) {
        
        List<Concept> lhsConcepts = null ;
        List<Concept> rhsConcepts = null ;
        LinkingPhrase lp          = null ;
        
        for( int i=0; i<ctx.link().size(); i++ ) {
            
            Concept_listContext lCListCtx = ctx.concept_list( i ) ;
            Linking_phraseContext   lpCtx = ctx.link(i).linking_phrase() ;
            Concept_listContext rCListCtx = ctx.concept_list( i+1 ) ;

            if( lCListCtx != null ) {
                lhsConcepts = getConcepts( lCListCtx ) ;
            }
            
            if( rCListCtx != null ) {
                rhsConcepts = getConcepts( rCListCtx ) ;
                if( rhsConcepts != null && rhsConcepts.size()>1 ) {
                    ranks.add( rhsConcepts ) ;
                }
            }
            
            lp = getLinkingPhrase( lpCtx ) ;
            
            if( lhsConcepts != null && lp != null ) {
                for( Concept c : lhsConcepts ) {
                    c.addNextEntity( lp ) ;
                }
            }
            
            if( rhsConcepts != null && lp != null ) {
                for( Concept c : rhsConcepts ) {
                    lp.addNextEntity( c ) ;
                    c.incrementNumIncoming() ;
                }
            }
        }
    }
    
    private LinkingPhrase getLinkingPhrase( Linking_phraseContext ctx ) {
        
        String        label = null ;
        LinkingPhrase lp    = null ;
        
        if( ctx != null ) {
            label = convertIDListOrString( ctx.lp_label().id_list_or_string() ) ;
            lp    = new LinkingPhrase( label ) ;
            loadNVPs( lp.getAttrs(), ctx.nvp_list() ) ;
        }
        else {
            lp = new LinkingPhrase( null ) ;
        }
        
        linkingPhrases.add( lp ) ;
        
        return lp ;
    }
    
    private List<Concept> getConcepts( Concept_listContext cListCtx ) {
        
        List<Concept> concepts = new ArrayList<>() ;
        
        Naked_concept_listContext   ncl = cListCtx.naked_concept_list() ;
        Cluster_concept_listContext ccl = cListCtx.cluster_concept_list() ;
        
        // There is nothing beyond the > 
        if( ncl == null && ccl == null ) {
            return null ;
        }
        
        if( ncl != null ) {
            for( ConceptContext cptCtx : ncl.concept() ) {
                Concept concept = getConcept( cptCtx ) ;
                concepts.add( concept ) ;
            }
        }
        else {
            for( ConceptContext cptCtx : ccl.naked_concept_list().concept() ) {
                Concept concept = getConcept( cptCtx ) ;
                concepts.add( concept ) ;
            }
            
            HashMap<String, String> nvpMap = new HashMap<>() ;
            loadNVPs( nvpMap, ccl.nvp_list() ) ;
            for( Concept c : concepts ) {
                c.mergeAttributes( nvpMap ) ;
            }
        }
        
        return concepts ;
    }
    
    private Concept getConcept( ConceptContext ctx ) {
        
        Concept cpt = null ;
        
        Concept_aliasContext aliasCtx = ctx.concept_alias() ;
        Concept_labelContext labelCtx = ctx.concept_label() ;
        Is_linkContext       linkCtx  = ctx.is_link() ; 
        Nvp_listContext      nvpCtx   = ctx.nvp_list() ;
        
        String alias = convertIDListOrString( aliasCtx.id_list_or_string() ) ;
        String label = null ;
        
        cpt = concepts.get( alias ) ;
        if( cpt == null ) {
            
            // Check in imported maps - if found, put in local concepts map 
            // and return instance. Else - create a new concept ;
            cpt = checkImportedConcepts( alias ) ;
            if( cpt == null ) {
                if( labelCtx != null ) {
                    label = convertIDListOrString( labelCtx.id_list_or_string() ) ;
                }
                else {
                    label = alias ;
                }
                
                cpt = new Concept( label, alias ) ;
            }

            // Load any fresh NVP to override the imported definitions
            loadNVPs( cpt.getAttrs(), nvpCtx ) ;
            
            if( linkCtx != null ) {
                cpt.setHyperLinkFlag() ;
            }
            
            concepts.put( alias, cpt ) ;
        }
        
        return cpt ;
    }
    
    private Concept checkImportedConcepts( String alias ) {
        Concept cpt = null ;
        
        for( CMapElement cmap : includes.values() ) {
            if( cmap.getConcepts().containsKey( alias ) ) {
                cpt = cmap.getConcepts().get( alias ) ;
                break ;
            }
        }
        
        return cpt ;
    }
    
    private String convertIDListOrString( Id_list_or_stringContext ctx ) {
        String retVal = "" ;
        if( ctx.ID() != null && !ctx.ID().isEmpty() ) {
            for( TerminalNode term : ctx.ID() ) {
                retVal += term.getText() + " " ;
            }
            retVal = retVal.trim() ;
        }
        else if( ctx.STRING() != null ) {
            retVal = ctx.STRING().getText() ;
            retVal = retVal.substring( 1, retVal.length()-1 ) ;
        }
        return retVal ;
    }
    
    private void loadNVPs( Map<String, String> nvpMap, Nvp_listContext nvpList ) {
        
        if( nvpList != null ) {
            for( NvpContext nvp : nvpList.nvp() ) {
                String id  = nvp.id().getText() ;
                String val = getValueContextAsString( nvp.value() ) ;
                
                if( id.equals("fc" ) ) {
                    id = "fillcolor" ;
                }
                nvpMap.put( id, ((val == null ) ? "true" : val) ) ;
            }
        }
    }
    
    private String getValueContextAsString( ValueContext ctx ) {
        String retVal = null ;
        if( ctx != null ) {
            if( ctx.ID() != null ) {
                retVal = ctx.ID().getText() ;
            }
            else if( ctx.STRING() != null ) {
                retVal = ctx.STRING().getText() ;
                retVal = retVal.substring( 1, retVal.length()-1 ) ;
            }
            else if( ctx.INT() != null ) {
                retVal = ctx.INT().getText() ;
            }
            else {
                logger.error( "Opps value is strange!" ) ;
            }
        }
        return retVal ;
    }
}
