package com.sandy.core;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class ConfigManager extends PropertiesConfiguration {

    private static final Logger logger = Logger.getLogger( ConfigManager.class ) ;

    private static ConfigManager instance = new ConfigManager() ;

    public static final String DEF_CONFIG_RESOURCE = "/config.properties" ;

    protected ConfigManager() {
    }

    public static ConfigManager getInstance() {
        return instance ;
    }

    public synchronized void initialize()
        throws ConfigurationException {

        final URL cfgURL = ConfigManager.class.getResource( DEF_CONFIG_RESOURCE ) ;
        initialize( cfgURL ) ;
    }

    public synchronized void initialize( final List<Object> cfgURLList )
        throws ConfigurationException {

        for( final Iterator<Object> iter = cfgURLList.iterator(); iter.hasNext(); ) {
            final Object cfgRes = iter.next() ;
            if( cfgRes instanceof String ) {
                super.load( ConfigManager.class.getResource( ( String )cfgRes ) ) ;
            }
            else if( cfgRes instanceof URL ) {
                super.load( ( URL )cfgRes ) ;
            }
            else if( cfgRes instanceof File ) {
                super.load( ( File )cfgRes ) ;
            }
            else {
                final String msg = "Unindentified configuration resource " + cfgRes ;
                logger.error( msg ) ;
                throw new ConfigurationException( msg ) ;
            }
        }
    }

    public synchronized void initialize( final URL cfgURL )
        throws ConfigurationException {
        super.load( cfgURL ) ;
    }
}
