package com.sandy.jcmap;

import java.io.File ;
import java.util.concurrent.ArrayBlockingQueue ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.core.ConfigManager ;
import com.sandy.jcmap.util.CMapBuilder ;
import com.sandy.jcmap.util.CMapDotSerializer ;
import com.sandy.jcmap.util.CMapElement ;
import com.sandy.jcmap.util.GraphvizAdapter ;

public class MapRenderWorker extends Thread {
    
    static final Logger logger = Logger.getLogger( MapRenderWorker.class ) ;
    
    private String dotExecPath ;
    private CMapImagePanel mapDisplay ;
    private ArrayBlockingQueue<String> mapQueue = new ArrayBlockingQueue<>(50) ;
    
    private int index = 0 ;
    private File outFolder = null ;
    private JCMap app = null ;
    private CMapEditorPane editor = null ;

    public MapRenderWorker( CMapEditorPane editor, CMapImagePanel label, 
                            String dotExecPath, JCMap app ) {
        this.mapDisplay = label ;
        this.dotExecPath = dotExecPath ;
        this.app = app ;
        this.editor = editor ;
        super.setDaemon( true ) ;
        ConfigManager cfgMgr = ConfigManager.getInstance() ;
        outFolder = new File( cfgMgr.getString( "temp.folder" ) ) ;
    }
    
    public void run() {
        
        while( true ) {
            try {
                // If there are multiple elements queued, up, we just pick up 
                // the last one and purge the rest - performance optimization
                while( mapQueue.size() > 1 ) {
                    mapQueue.remove() ;
                }
                String content = mapQueue.take() ;
                CMapBuilder builder = new CMapBuilder( app.getCurDir() ) ;
                CMapElement element = builder.buildCMapElement( content ) ;
                
                editor.setLastCMap( element ) ;
                
                final File dotFile  = new File( outFolder, "cmap" + index + ".dot" ) ;
                final File pngFile  = new File( outFolder, "cmap" + index + ".png" ) ;
                final File cmapFile = new File( outFolder, "cmap" + index + ".cmap" ) ;
                
                CMapDotSerializer util = new CMapDotSerializer( element ) ;
                String dotContent = util.convertCMaptoDot() ;
                FileUtils.writeStringToFile( dotFile, dotContent ) ; 
                
                final GraphvizAdapter adapter = new GraphvizAdapter( dotExecPath ) ;
                
                Thread t1 = new Thread() {
                    public void run() {
                        try {
                            adapter.generateGraph( dotFile, pngFile ) ;
                        }
                        catch( Exception e ) {
                            e.printStackTrace();
                        } 
                    }
                } ;
                
                Thread t2 = new Thread() {
                    public void run() {
                        try {
                            adapter.generateImap( dotFile, cmapFile ) ; 
                        }
                        catch( Exception e ) {
                            e.printStackTrace();
                        } 
                    }
                } ;
                
                t1.start() ;
                t2.start() ;
                
                t1.join() ;
                t2.join() ;
                
                this.mapDisplay.setImage( pngFile, cmapFile ) ;
                
                // To get close to 25 FPS and shaving of redundant processing.
                Thread.sleep( 30 ) ;
            }
            catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    
    public void offer( String content ) {
        mapQueue.offer( content ) ;
    }
}
