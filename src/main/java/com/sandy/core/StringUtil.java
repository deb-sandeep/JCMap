package com.sandy.core ;

public final class StringUtil {

    private StringUtil() {
        super() ;
    }

    public static boolean isEmptyOrNull( final String str ) {
        
        boolean retVal = false ;
        if ( str == null || "".equals( str.trim() ) ) {
            retVal = true ;
        }
        return retVal ;
    }

    public static boolean isNotEmptyOrNull( final String str ) {
        return !isEmptyOrNull( str ) ;
    }
}
