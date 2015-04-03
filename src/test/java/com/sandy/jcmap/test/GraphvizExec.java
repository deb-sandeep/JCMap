package com.sandy.jcmap.test;

import java.util.ArrayList ;

import com.sandy.jcmap.util.CommandLineExec ;

public class GraphvizExec {

    public static void main( String[] args ) throws Exception {
        
        CommandLineExec.executeCommand( getPNGCommandLine() ) ;
    }
    
    private static String[] getPNGCommandLine() {
        
        ArrayList<String> cmdParts = new ArrayList<String>() ;
        
        cmdParts.add( "/usr/bin/dot" ) ;
        cmdParts.add( "-v" ) ;
        cmdParts.add( "-Tpng" ) ;
        cmdParts.add( "-o" ) ;
        cmdParts.add( "/home/sandeep/projects/workspace/jcmap_temp/cmap0.png" ) ;
        cmdParts.add( "-Kdot" ) ;
        cmdParts.add( "/home/sandeep/projects/workspace/jcmap_temp/cmap0.dot" ) ;
        
        return cmdParts.toArray(new String[2]) ;
    }
}
