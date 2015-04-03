package com.sandy.jcmap.util;

import java.io.File ;
import java.util.ArrayList ;

public class GraphvizAdapter {

    //private static final Logger logger = Logger.getLogger( GraphvizAdapter.class ) ;
    private String dotExecPath = null ;
    
    public GraphvizAdapter( String dotExecPath ) {
        this.dotExecPath = dotExecPath ;
    }
    
    public void generateGraph( File dotFilePath, File outputFilePath )
        throws Exception {
        
        String[] cmd = getPNGCommandLine( dotFilePath, outputFilePath ) ;
        CommandLineExec.executeCommand( cmd ) ;
    }
    
    public void generateImap( File dotFilePath, File outputFilePath )
            throws Exception {
        
        String[] cmd = getIMapCommandLine( dotFilePath, outputFilePath ) ;
        CommandLineExec.executeCommand( cmd ) ;
    }
    
    private String[] getPNGCommandLine( File dotPath, File outputPath ) {
        
        File dotExecFile = new File( dotExecPath ) ;
        
        File outputDir = outputPath.getParentFile() ;
        if( !outputDir.exists() ) {
            outputDir.mkdirs() ;
        }
        
        ArrayList<String> cmdParts = new ArrayList<String>() ;
        
        cmdParts.add( dotExecFile.getAbsolutePath() ) ;
        cmdParts.add( "-v" ) ;
        cmdParts.add( "-Tpng" ) ;
        cmdParts.add( "-o" ) ;
        cmdParts.add( outputPath.getAbsolutePath() ) ;
        cmdParts.add( "-Kdot" ) ;
        cmdParts.add( dotPath.getAbsolutePath() ) ;
        
        return cmdParts.toArray(new String[2]) ;
    }

    private String[] getIMapCommandLine( File dotPath, File outputPath ) {
        
        File dotExecFile = new File( dotExecPath ) ;
        
        File outputDir = outputPath.getParentFile() ;
        if( !outputDir.exists() ) {
            outputDir.mkdirs() ;
        }
        
        ArrayList<String> cmdParts = new ArrayList<String>() ;
        
        cmdParts.add( dotExecFile.getAbsolutePath() ) ;
        cmdParts.add( "-v" ) ;
        cmdParts.add( "-Tcmapx" ) ;
        cmdParts.add( "-o" ) ;
        cmdParts.add( outputPath.getAbsolutePath() ) ;
        cmdParts.add( "-Kdot" ) ;
        cmdParts.add( dotPath.getAbsolutePath() ) ;
        
        return cmdParts.toArray(new String[2]) ;
    }
}
