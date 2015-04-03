package com.sandy.jcmap.util ;

import java.io.BufferedReader ;
import java.io.InputStream ;
import java.io.InputStreamReader ;

import org.apache.log4j.Logger ;

public class CommandLineExec {
    
    private static final Logger logger = Logger.getLogger( CommandLineExec.class ) ;
    
    private static String getCommandAsString( String[] cmdParts ) {
        
        StringBuilder builder = new StringBuilder() ;
        for( String part : cmdParts ) {
            builder.append( part ).append( " " ) ;
        }
        return builder.toString().trim() ;
    }

    /**
     * This method is used to execute a command in the command prompt (by
     * appending the "cmd /c" to the input command. the integrity of the command
     * is upon the caller. This method DOES NOT spawn a new thread and hence
     * executes in the calling thread. For execution in separate thread separate
     * thread refer method executeCommandInThread
     * 
     * @param command
     */
    public static int executeCommand( String[] command ) {

        int retVal = -1 ;
        try {
            logger.debug( "Executing command = " + getCommandAsString(command) ) ;
            
            Runtime rt = Runtime.getRuntime() ;
            Process pr = rt.exec( command ) ;

            InputStream       is  = pr.getInputStream() ;
            InputStreamReader isr = new InputStreamReader( is ) ;
            BufferedReader  input = new BufferedReader( isr ) ;

            String line = null ;

            while( ( line = input.readLine() ) != null ) {
                logger.debug( "Command " + command + " output : " + line ) ;
            }

            retVal = pr.waitFor() ;
            logger.debug( "Command executed with return code = " + retVal ) ;
        }
        catch( Exception e ) {
            logger.error( "Command execution error.", e ) ;
        }
        
        return retVal ;
    }
}
