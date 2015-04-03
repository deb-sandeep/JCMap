package com.sandy.jcmap;

import java.awt.Color ;
import java.awt.Font ;
import java.awt.event.KeyAdapter ;
import java.awt.event.KeyEvent ;
import java.util.ArrayList ;
import java.util.List ;

import javax.swing.JOptionPane ;
import javax.swing.JTextPane ;
import javax.swing.SwingUtilities ;
import javax.swing.event.DocumentEvent ;
import javax.swing.event.DocumentListener ;
import javax.swing.text.BadLocationException ;
import javax.swing.text.DefaultStyledDocument ;
import javax.swing.text.Style ;
import javax.swing.text.StyleConstants ;
import javax.swing.text.StyleContext ;
import javax.swing.text.StyledDocument ;

import org.antlr.v4.runtime.ANTLRInputStream ;
import org.antlr.v4.runtime.Token ;
import org.apache.log4j.Logger ;

import com.sandy.core.StringUtil ;
import com.sandy.jcmap.util.CMapElement ;

public class CMapEditorPane extends JTextPane implements DocumentListener {
    
    static final Logger logger = Logger.getLogger( CMapEditorPane.class ) ;

    private static final List<Integer> KW_TOKENS    = new ArrayList<>() ;
    private static final List<Integer> PAREN_TOKENS = new ArrayList<>() ;
    static {
        KW_TOKENS.add( 1 ) ;
        KW_TOKENS.add( 3 ) ;
        KW_TOKENS.add( 8 ) ;
        KW_TOKENS.add( 10 ) ;
        KW_TOKENS.add( 12 ) ;
        KW_TOKENS.add( 15 ) ;
        KW_TOKENS.add( 16 ) ;
        KW_TOKENS.add( 17 ) ;
        
        PAREN_TOKENS.add( 2 ) ;
        PAREN_TOKENS.add( 4 ) ;
        PAREN_TOKENS.add( 5 ) ;
        PAREN_TOKENS.add( 6 ) ;
        PAREN_TOKENS.add( 7 ) ;
        PAREN_TOKENS.add( 9 ) ;
        PAREN_TOKENS.add( 11 ) ;
        PAREN_TOKENS.add( 13 ) ;
        PAREN_TOKENS.add( 14 ) ;
        PAREN_TOKENS.add( 18 ) ;
    }
    
    private static final long serialVersionUID = 1L ;
    private JCMap          app = null ;
    private StyledDocument doc = null ;
    private String         lastContent = null ;
    private CMapElement    lastCMap = null ;
    
    private List<String> potentialMatches = null ;
    private int nextMatchPos = -1 ;
    private boolean inAutoCompleteMode = false ;
    private String acPrefix = null ;
    
    private boolean dontRefreshMap = false ;

    public CMapEditorPane( JCMap app ) {
        
        this.app = app ;
        setFont( new Font( "Courier New", Font.PLAIN, 12 ) ) ;
        
        prepareStyledDocument() ;
        super.setDocument( doc ) ;
        doc.addDocumentListener( this ) ;
        
        addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
                handleKeyPress( e ) ;
            }
        } ) ;
    }
    
    private void prepareStyledDocument() {

        Style base = null ;
        
        doc  = new DefaultStyledDocument() ;
        base = StyleContext.getDefaultStyleContext()
                           .getStyle( StyleContext.DEFAULT_STYLE ) ;
        
        doc.addStyle( "base",     base ) ;
        
        Style keyword = doc.addStyle( "keyword", base ) ;
        StyleConstants.setBold( keyword, true ) ;
        StyleConstants.setForeground( keyword, Color.MAGENTA.darker() ) ;
        
        Style string = doc.addStyle( "string", base ) ;
        StyleConstants.setItalic( string, true ) ;
        StyleConstants.setForeground( string, Color.DARK_GRAY ) ;
        
        Style paren = doc.addStyle( "paren", base ) ;
        StyleConstants.setBold( paren, true ) ;
        StyleConstants.setForeground( paren, Color.RED ) ;
        
        Style id = doc.addStyle( "id", base ) ;
        StyleConstants.setForeground( id, Color.BLUE ) ;
        
        Style linkPhrase = doc.addStyle( "lp", base ) ;
        StyleConstants.setForeground( linkPhrase, Color.GREEN.darker() ) ;
    }

    public void insertUpdate( DocumentEvent e ) { processDocChange() ; }

    public void removeUpdate( DocumentEvent e ) { processDocChange() ; }

    public void changedUpdate( DocumentEvent e ) { processDocChange() ; }
    
    private void processDocChange() {
        try {
            final String content = doc.getText( 0, doc.getLength() ) ;
            
            // If we have structurally not changed the document, we don't 
            // process any document change events. This is essential since we  
            // keep changing styles and for each change of style the document is 
            // considered changed. We don't want to process those events.
            if( lastContent != null && lastContent.equals( content ) ) {
                return ;
            }
            else if( dontRefreshMap ) {
                // If we are in a state where we don't want to refresh the map
                // for example in the middle of a search/replace.. don't do
                // anything.
                return ;
            }
            else {
                lastContent = content ;
            }
            
            app.offerContentForCMapRendering( content ) ;

            // We highlight the contents of the editor asynchronously.
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    highlightDocument( content ) ;
                }
            });
        }
        catch( Exception e1 ) {
            e1.printStackTrace();
        }
    }
    
    public void setLastCMap( CMapElement element ) {
        this.lastCMap = element ;
    }

    private void handleKeyPress( KeyEvent e ) {
        if( e.isControlDown() ) {
            if( e.getKeyCode() == KeyEvent.VK_S ) {
                CMapEditorPane.this.app.saveCMap() ;
            }
            else if( e.getKeyCode() == KeyEvent.VK_TAB ) {
                insertTab() ;
            }
            else if( e.isShiftDown() ) {
                if( e.getKeyCode() == KeyEvent.VK_R ) {
                    replaceSelection() ;
                }
            }
        }
        else {
            if( e.getKeyCode() == KeyEvent.VK_F3 ) {
                findNext() ;
            }
            else if( e.getKeyCode() == KeyEvent.VK_TAB ) {
                if( CMapEditorPane.this.lastCMap != null ) {
                    e.consume() ;
                    if( !inAutoCompleteMode ) {
                        showAutoComplete() ;
                    }
                    else {
                        showNextPotentialMatch() ;
                    }
                }
            }
            else if( inAutoCompleteMode ) {
                e.consume() ;
                try {
                    switch( e.getKeyCode() ) {
                        case KeyEvent.VK_ENTER :
                            moveCaretPosition( getSelectionEnd() ) ;
                            doc.insertString( getCaretPosition(), " ", null ) ;
                            inAutoCompleteMode = false ;
                            break ;
                        case KeyEvent.VK_ESCAPE:
                        case KeyEvent.VK_BACK_SPACE:
                            if( getSelectedText() != null ) {
                                doc.remove( getSelectionStart(), 
                                            getSelectedText().length() ) ;
                            }
                            inAutoCompleteMode = false ;
                            break ;
                    }
                }
                catch( BadLocationException e1 ) {
                    e1.printStackTrace();
                }
            }
        }
    }
    
    private boolean inLPZone = false ;
    private void highlightDocument( String content ) {
        inLPZone = false ;
        ANTLRInputStream  ais    = new ANTLRInputStream( content ) ;
        CMapLexer         lexer  = new CMapLexer( ais ) ;
        
        @SuppressWarnings("unchecked")
        List<Token> tokens = (List<Token>) lexer.getAllTokens() ;
        for( Token t : tokens ) {
            int startIndex = t.getStartIndex() ;
            int endIndex   = t.getStopIndex() ;
            int tokenType  = t.getType() ;
            
            String style = getStyle( tokenType ) ;
            doc.setCharacterAttributes( startIndex, endIndex, 
                                        doc.getStyle(style), true ) ;
        }
    }
    
    private String getStyle( int tokenType ) {
        
        String styleName = "base" ;
        if( tokenType == 14 ) {
            if( inLPZone ) {
                inLPZone = false ;
            }
            else {
                inLPZone = true ;
            }
            styleName = "paren" ;
        }
        else if( inLPZone ) {
            styleName = "lp" ;
        }
        else if( tokenType == 23 ) {
            styleName = "string" ;
        }
        else if( tokenType == 19 ) {
            styleName = "id" ;
        }
        else if( KW_TOKENS.contains( tokenType ) ) {
            styleName = "keyword" ;
        }
        else if( PAREN_TOKENS.contains( tokenType ) ) {
            styleName = "paren" ;
        }
        
        return styleName ;
    }
    
    private void showAutoComplete() {
        
        int caretPos = getCaretPosition() ;
        String textAtPos = getTextAtPos( caretPos ) ;
        
        if( StringUtil.isNotEmptyOrNull( textAtPos ) ) {
            textAtPos = textAtPos.trim() ;
            if( inLPZone ) {
                potentialMatches = lastCMap.getMatchingLPs( textAtPos ) ;
            }
            else {
                potentialMatches = lastCMap.getMatchingConcepts( textAtPos ) ;
            }
            
            potentialMatches.remove( textAtPos ) ;
            
            if( !potentialMatches.isEmpty() ) {
                nextMatchPos = 0 ;
                inAutoCompleteMode = true ;
                acPrefix = textAtPos ;
                showNextPotentialMatch() ;
            }
        }
    }
    
    private String getTextAtPos( int pos ) {
        StringBuffer buffer = new StringBuffer() ;
        int p = pos-1 ;
        try {
            while( p>-1 ) {
                char ch = doc.getText(p, 1).charAt(0) ;
                if( isDelimitingChar( ch ) ) {
                    break ;
                }
                else {
                    buffer.insert( 0, ch ) ;
                }
                p-- ;
            }
        }
        catch( BadLocationException e ) {
            e.printStackTrace();
        }
                
        return buffer.toString().trim() ;
    }
    
    private boolean isDelimitingChar( char ch ) {
        if( ch == ',' || 
            ch == '>' ||
            ch == ';' || 
            ch == '=' ||
            ch == '{' || 
            ch == '}' ) {
            return true ;
        }
        return false ;
    }
    
    private void showNextPotentialMatch() {
        
        int caretPos = getCaretPosition() ;
        String match = potentialMatches.get( nextMatchPos ) ;
        
        String insText = match.substring( acPrefix.length() ) ;
        try {
            if( StringUtil.isNotEmptyOrNull( getSelectedText() ) ) {
                doc.remove( getSelectionStart(), getSelectedText().length() ) ;
            }
            doc.insertString( caretPos, insText, null ) ;
            moveCaretPosition( caretPos ) ;
        }
        catch( BadLocationException e ) {
            e.printStackTrace();
        }
        
        nextMatchPos++ ;
        if( nextMatchPos > potentialMatches.size()-1 ) {
            nextMatchPos = 0 ;
        }
    }

    public boolean find( String alias, int fromIndex ) {
        
        boolean found = false ;
        try {
            for( int i=fromIndex; i<(doc.getLength()-alias.length()); i++ ) {
                String t = doc.getText( i, alias.length() ) ;
                if( t.equals( alias ) ) {
                    int endIndex = i + alias.length() ;
                    requestFocus() ;
                    select( i, endIndex ) ;
                    found = true ;
                    break ;
                }
            }
        }
        catch( BadLocationException e ) {
            e.printStackTrace( System.out );
        }
        return found ;
    }
    
    public void findNext() {
        String text = getSelectedText() ;
        if( StringUtil.isNotEmptyOrNull( text ) ) {
            if( !find( text, getSelectionEnd() ) ) {
                find( text, 0 ) ;
            };
        }
    }
    
    public void replaceSelection() {
        String selectedText = getSelectedText() ;
        if( selectedText != null ) {
            String input = ( String )JOptionPane.showInputDialog(
                            app,
                            null,
                            "Replace",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            selectedText );            
            if( input != null ) {
                dontRefreshMap = true ;
                replaceSelection( input ) ;
                while( find( selectedText, getCaretPosition() ) ) {
                    replaceSelection( input ) ;
                }
                dontRefreshMap = false ;
                processDocChange() ;
            }
        }
    }
    
    public void insertTab() {
        try {
            doc.insertString( getCaretPosition(), "    ", null ) ;
        }
        catch( BadLocationException e ) {
            e.printStackTrace();
        }
    }
}
