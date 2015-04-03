package com.sandy.jcmap;

import java.awt.BorderLayout ;
import java.awt.Container ;
import java.awt.Dimension ;
import java.awt.GridLayout ;
import java.awt.Toolkit ;
import java.awt.datatransfer.Clipboard ;
import java.awt.datatransfer.ClipboardOwner ;
import java.awt.datatransfer.Transferable ;
import java.awt.event.ActionEvent ;
import java.awt.event.ActionListener ;
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import java.awt.image.BufferedImage ;
import java.io.File ;
import java.io.IOException ;
import java.util.Arrays ;
import java.util.LinkedList ;

import javax.imageio.ImageIO ;
import javax.swing.DefaultListModel ;
import javax.swing.ImageIcon ;
import javax.swing.JButton ;
import javax.swing.JFileChooser ;
import javax.swing.JFrame ;
import javax.swing.JList ;
import javax.swing.JOptionPane ;
import javax.swing.JPanel ;
import javax.swing.JScrollPane ;
import javax.swing.JSplitPane ;
import javax.swing.JToolBar ;
import javax.swing.event.ListSelectionEvent ;
import javax.swing.event.ListSelectionListener ;
import javax.swing.filechooser.FileFilter ;

import org.apache.commons.io.FileUtils ;
import org.apache.log4j.Logger ;

import com.sandy.core.ConfigManager ;
import com.sandy.core.StringUtil ;

public class JCMap extends JFrame implements ActionListener, ClipboardOwner {

    private static final long serialVersionUID = 1L ;
    static Logger logger = Logger.getLogger( JCMap.class ) ;
    
    private CMapEditorPane editor ;
    private CMapImagePanel mapDisplay ;
    private JList<String>  zList ;
    private DefaultListModel<String> zListModel ;
    
    private JButton laBtn = new JButton( getIcon( "leftarrow" ) ) ;
    private JButton raBtn = new JButton( getIcon( "rightarrow" ) ) ;
    private JButton hmBtn = new JButton( getIcon( "home" ) ) ;
    
    private File lastSavedDir = null ;
    private File currentFile = null ;
    private File lastExportDir = null ;
    
    private LinkedList<File> fileList = new LinkedList<>() ;
    
    private boolean isNewFile = true ;
    
    private MapRenderWorker worker = null ;
    
    public JCMap() {
        
        super( "Concept Map Editor" ) ;
        setUpUI() ;
        
        ConfigManager cfgMgr = ConfigManager.getInstance() ;
        String workspacePath = cfgMgr.getString( "jcmap.workspace.dir" ) ;
        lastSavedDir = new File( workspacePath ) ;
        if( !lastSavedDir.exists() ) {
            lastSavedDir.mkdirs() ;
        }
        
        worker = new MapRenderWorker( editor, mapDisplay, 
                                      ConfigManager.getInstance().getString( "graphviz.dot.exec.path" ),
                                      this ) ;
        worker.start() ;
    }
    
    private void setUpUI() {
        
        Container contentPane = getContentPane() ;
        contentPane.setLayout( new BorderLayout() ) ;

        mapDisplay = new CMapImagePanel( this ) ;
        
        JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT ) ;
        splitPane.setDividerLocation( 115 ) ;
        splitPane.setOneTouchExpandable( true ) ; 
        splitPane.add( getTopPanel() ) ;
        splitPane.add( mapDisplay ) ;
        splitPane.setDividerSize( 6 ) ;
        splitPane.setContinuousLayout( true ) ;
        
        contentPane.add( splitPane ) ;
        
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ; 
        super.setBounds( 0, 0, screenSz.width, screenSz.height ) ;
        super.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                saveCMap() ;
                System.exit(-1) ;
            }
        });
        super.setVisible( true ) ;
        editor.requestFocus() ;
    }
    
    private JPanel getTopPanel() {
        
        JPanel topPanel = new JPanel( new BorderLayout() ) ;
        
        editor = new CMapEditorPane( this ) ;
        JPanel panel = new JPanel( new BorderLayout() ) ;
        panel.add( editor ) ;
        JScrollPane editorSP = new JScrollPane( panel ) ;
        
        topPanel.add( getToolBarPanel(), BorderLayout.WEST ) ;
        topPanel.add( editorSP, BorderLayout.CENTER ) ;
        topPanel.add( getTopRightPanel(), BorderLayout.EAST ) ;
        
        return topPanel ;
    }
    
    private JPanel getTopRightPanel() {
        JPanel panel = new JPanel( new BorderLayout() ) ;
        panel.setPreferredSize( new Dimension( 200, 10 ) ) ;
        
        zListModel = new DefaultListModel<>() ;
        zList = new JList<String>( zListModel ) ;
        JScrollPane sp = new JScrollPane( zList ) ;
        
        zList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e ) {
                if( !e.getValueIsAdjusting() ) {
                    String str = zList.getSelectedValue() ;
                    if( StringUtil.isNotEmptyOrNull( str ) ) {
                        File f = new File( lastSavedDir, str ) ;
                        openFile( f ) ;
                    }
                }
            }
        });
        
        panel.add( sp ) ;
        
        return panel ;
    }
    
    private JPanel getToolBarPanel() {
        JPanel panel = new JPanel( new GridLayout( 1, 3 ) ) ;
        panel.setPreferredSize( new Dimension( 105, 10 ) ) ;
        
        panel.add( getToolBar1() ) ;
        panel.add( getToolBar2() ) ;
        panel.add( getToolBar3() ) ;
        
        return panel ;
    }
    
    private JToolBar getToolBar1() {
        
        JToolBar toolBar = new JToolBar( JToolBar.VERTICAL ) ;
        toolBar.setFloatable( false ) ;
        
        JButton newBtn = new JButton( getIcon( "new" ) ) ;
        JButton saveBtn = new JButton( getIcon( "save" ) ) ;
        JButton openBtn = new JButton( getIcon( "open" ) ) ;
        
        newBtn.setActionCommand( "new_cmap" ) ;
        saveBtn.setActionCommand( "save_cmap" ) ;
        openBtn.setActionCommand( "open_cmap" ) ;
        
        newBtn.addActionListener( this ) ;
        saveBtn.addActionListener( this ) ;
        openBtn.addActionListener( this ) ;
        
        toolBar.add( newBtn ) ;
        toolBar.add( saveBtn ) ;
        toolBar.add( openBtn ) ;
        
        return toolBar ;
    }
    
    private JToolBar getToolBar2() {
        
        JToolBar toolBar = new JToolBar( JToolBar.VERTICAL ) ;
        toolBar.setFloatable( false ) ;
        
        laBtn.setActionCommand( "la_btn" ) ;
        raBtn.setActionCommand( "ra_btn" ) ;
        hmBtn.setActionCommand( "hm_btn" ) ;
        
        laBtn.addActionListener( this ) ;
        raBtn.addActionListener( this ) ;
        hmBtn.addActionListener( this ) ;
        
        toolBar.add( laBtn ) ;
        toolBar.add( raBtn ) ;
        toolBar.add( hmBtn ) ;
        
        laBtn.setEnabled( false ) ;
        raBtn.setEnabled( false ) ;
        hmBtn.setEnabled( false ) ;
        
        return toolBar ;
    }
    
    private JToolBar getToolBar3() {
        
        JToolBar toolBar = new JToolBar( JToolBar.VERTICAL ) ;
        toolBar.setFloatable( false ) ;
        
        JButton exportImgBtn  = new JButton( getIcon( "export" ) ) ;
        JButton copyImgBtn = new JButton( getIcon( "copy" ) ) ;
        
        exportImgBtn.setActionCommand( "export_img" ) ;
        copyImgBtn.setActionCommand( "copy_img" ) ;
        
        exportImgBtn.addActionListener( this ) ;
        copyImgBtn.addActionListener( this ) ;
        
        toolBar.add( exportImgBtn ) ;
        toolBar.add( copyImgBtn ) ;
        
        return toolBar ;
    }
    
    private ImageIcon getIcon( String imgName ) {
        ImageIcon icon = null ;
        try {
            icon = new ImageIcon( ImageIO.read( JCMap.class.getResource( "/" + imgName + ".png" ) ) ) ;
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
        return icon ;
    }
    
    public void offerContentForCMapRendering( String content ) {
        worker.offer( content ) ;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        String actCmd = e.getActionCommand() ;
        if( actCmd.equals( "new_cmap" ) ) {
            newCMap() ;
        }
        else if( actCmd.equals( "save_cmap" ) ) {
            saveCMap() ;
        }
        else if( actCmd.equals( "open_cmap" ) ) {
            openCMap() ;
        }
        else if( actCmd.equals( "hm_btn" ) ) {
            openFile( fileList.getFirst() ) ;
        }
        else if( actCmd.equals( "la_btn" ) ) {
            int i = fileList.indexOf( currentFile ) ;
            if( i > 0 ) {
                openFile( fileList.get( i-1 ) ) ;
            }
        }
        else if( actCmd.equals( "ra_btn" ) ) {
            int i = fileList.indexOf( currentFile ) ;
            if( i < fileList.size()-1 ) {
                openFile( fileList.get( i+1 ) ) ;
            }
        }
        else if( actCmd.equals( "export_img" ) ) {
            exportCurImg() ;
        }
        else if( actCmd.equals( "copy_img" ) ) {
            copyImgToClipboard() ;
        }
    }
    
    private void newCMap() {
        
        saveCurrentFile() ;
        
        isNewFile = true ;
        editor.setText( "" ) ;
        currentFile = null ;
        zListModel.clear() ;
        setTitle( "**usaved**" ) ;
        String workspacePath = ConfigManager.getInstance().getString( "jcmap.workspace.dir" ) ;
        lastSavedDir = new File( workspacePath ) ;
    }
    
    public boolean saveCMap() {
        
        boolean saved = false ;
        String content = editor.getText().trim() ;
        
        if( currentFile != null ) {
            saveCurrentFile() ;
        }
        else {
            if( StringUtil.isNotEmptyOrNull( content ) ) {
                JFileChooser fileChooser = new JFileChooser() ;
                fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
                
                if( lastSavedDir != null ) {
                    fileChooser.setCurrentDirectory( lastSavedDir ) ;
                }
                
                int result = fileChooser.showSaveDialog( this ) ;
                if( result == JFileChooser.APPROVE_OPTION ) {
                    File dir = fileChooser.getSelectedFile() ;
                    lastSavedDir = dir ;
                    currentFile = new File( lastSavedDir, "main.cmap" ) ;

                    saveCurrentFile() ;
                    super.setTitle( currentFile.getAbsolutePath() ) ;
                    fileList.add( currentFile ) ;
                    isNewFile = false ;
                    saved = true ;
                }
            }
        }
        return saved ;
    }
    
    private void openCMap() {
        
        JFileChooser fileChooser = new JFileChooser() ;
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
        
        if( lastSavedDir != null ) {
            fileChooser.setCurrentDirectory( lastSavedDir ) ;
        }
        
        fileChooser.setFileFilter( new FileFilter() {
            public String getDescription() { return "CMap main file" ; }
            public boolean accept( File f ) {
                if( f.isDirectory() ) {
                    return true ;
                }
                return false ;
            }
        });
        
        int result = fileChooser.showOpenDialog( this ) ;
        if( result == JFileChooser.APPROVE_OPTION ) {
            
            File dir = fileChooser.getSelectedFile() ;
            File file = new File( dir, "main.cmap" ) ;
            if( !file.exists() ) {
                JOptionPane.showMessageDialog(
                                 this, 
                                 "This directory does not contain main.cmap" ) ;
                return ;
            }
            
            openFile( file ) ;
            refreshZList() ;
        }
    }
    
    private void refreshZList() {
        if( !isNewFile ) {
            zListModel.clear() ;
            File[] files = lastSavedDir.listFiles() ;
            Arrays.sort( files ) ;
            for( File f : files ) {
                zListModel.addElement( f.getName() ) ;
            }
        }
    }
    
    private void openFile( File file ) {
        try {
            saveCurrentFile(); 
            
            String content = FileUtils.readFileToString( file ) ;
            editor.setText( content + "\n" ) ;
            super.setTitle( file.getAbsolutePath() ) ;
            
            lastSavedDir = file.getParentFile() ;
            currentFile = file ;
            
            if( !fileList.contains( file ) ) {
                fileList.add( file ) ;
            }
            
            int i = fileList.indexOf( file ) ;
            laBtn.setEnabled( i!=0 ) ;
            raBtn.setEnabled( i!=fileList.size()-1 ) ;
            hmBtn.setEnabled( !fileList.isEmpty() ) ;
            isNewFile = false ;
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }
    
    private void saveCurrentFile() {
        if( currentFile != null ) {
            String content = editor.getText().trim() ;
            try {
                content += "\n" ;
                FileUtils.writeStringToFile( currentFile, content ) ;
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    
    public File getCurDir() {
        return this.lastSavedDir ;
    }
    
    public void conceptDrillDownRequested( String conceptAlias ) {
        
        if( isNewFile ) {
            if( !saveCMap() ) {
                return ;
            };
        }
        File file = new File( getCurDir(), conceptAlias + ".cmap" ) ;
        if( !file.exists() ) {
            try {
                file.createNewFile() ;
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
        openFile( file ) ;
        refreshZList() ;
    }
    
    private void exportCurImg() {
        File curImgFile = mapDisplay.getCurImgFile() ;
        if( curImgFile != null ) {
            JFileChooser fileChooser = new JFileChooser() ;
            fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY ) ;
            
            if( lastExportDir != null ) {
                fileChooser.setCurrentDirectory( lastExportDir ) ;
            }
            
            int result = fileChooser.showSaveDialog( this ) ;
            if( result == JFileChooser.APPROVE_OPTION ) {
                File file = fileChooser.getSelectedFile() ;
                lastExportDir = file.getParentFile() ;
                try {
                    FileUtils.copyFile( curImgFile, file ) ;
                }
                catch( IOException e ) {
                    e.printStackTrace() ;
                }
            }
        }
    }
    
    private void copyImgToClipboard() {
        File curImgFile = mapDisplay.getCurImgFile() ;
        BufferedImage img ;
        if( curImgFile != null ) {
            try {
                img = ImageIO.read( curImgFile ) ;
                TransferableImage trans = new TransferableImage( img ) ;
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard() ;
                c.setContents( trans, this ) ;
            }
            catch( Exception e ) {
                e.printStackTrace() ;
            }
        }
    }
    
    public static void main( String[] args ) throws Exception {
        ConfigManager cfgMgr = ConfigManager.getInstance() ;
        cfgMgr.initialize() ; 
        
        new JCMap() ;
    }

    @Override
    public void lostOwnership( Clipboard clipboard, Transferable contents ) {}
    
    public void elementSelected( String alias ) {
        editor.find( alias, 0 ) ;
    }
}
