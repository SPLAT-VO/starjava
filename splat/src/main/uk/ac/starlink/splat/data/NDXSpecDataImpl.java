/*
 * Copyright (C) 2002-2003 Central Laboratory of the Research Councils
 *
 *  History:
 *     28-MAY-2002 (Peter W. Draper):
 *       Original version.
 */
package uk.ac.starlink.splat.data;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.List;

import org.w3c.dom.Element;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Source;

import uk.ac.starlink.ast.FrameSet;
////import uk.ac.starlink.hdx.HdxContainer;
////import uk.ac.starlink.hdx.HdxContainerFactory;
////import uk.ac.starlink.hdx.Ndx;

import uk.ac.starlink.array.AccessMode;
import uk.ac.starlink.array.ArrayAccess;
import uk.ac.starlink.array.Requirements;
import uk.ac.starlink.array.BadHandler;
import uk.ac.starlink.array.NDArray;
import uk.ac.starlink.array.OrderedNDShape;
import uk.ac.starlink.array.Type;
import uk.ac.starlink.ndx.Ndx;
import uk.ac.starlink.ndx.Ndxs;
import uk.ac.starlink.ndx.NdxIO;
import uk.ac.starlink.ndx.XMLNdxHandler;

import uk.ac.starlink.splat.util.SplatException;

/**
 * NDXSpecDataImpl - implementation of SpecDataImpl to access a spectrum
 *                   stored in an NDX.
 *
 * @author Peter W. Draper
 * @version $Id$
 * @see "The Bridge Design Pattern"
 */
public class NDXSpecDataImpl 
    extends AbstractSpecDataImpl
{

    //// Implementation of abstract methods.

    /**
     * Constructor - open an HDX description by name. The name is
     * understood by HDX, we don't need to know too much, but this
     * should only contain a single NDX, that is a spectrum.
     */
    public NDXSpecDataImpl( String hdxName )
        throws SplatException
    {
        super( hdxName );
        open( hdxName );
    }

    /**
     * Constructor - use an NDX described in a DOM.
     */
    public NDXSpecDataImpl( Element ndxElement )
        throws SplatException
    {
        super( "Wired NDX" );
        open( ndxElement );
    }

    /**
     * Constructor - use a URL containing a NDX.
     */
    public NDXSpecDataImpl( URL url  )
        throws SplatException
    {
        super( "Network NDX" );
        open( url );
    }

    /**
     * Constructor - use a Source containing a NDX.
     */
    public NDXSpecDataImpl( Source source  )
        throws SplatException
    {
        super( "Sourced NDX" );
        open( source );
    }

    /**
     * Constructor - an NDX.
     */
    public NDXSpecDataImpl( Ndx ndx  )
        throws SplatException
    {
        super( "Native NDX" );
        open( ndx );
    }

    /**
     * Constructor - an NDX with a given short and full names.
     */
    public NDXSpecDataImpl( Ndx ndx, String shortName, String fullName  )
        throws SplatException
    {
        super( "Native NDX" );
        open( ndx );
        this.shortName = shortName;
        this.fullName = fullName;
    }

    /**
     * Constructor. Initialise this spectrum by cloning the content of
     * another spectrum (usual starting point for saving).
     */
    public NDXSpecDataImpl( String hdxName, SpecData source )
        throws SplatException
    {
        super( hdxName );
        throw new SplatException
            ( "Duplication of NDX spectra not implemented" );
    }

    /**
     * Return a copy of the spectrum data values.
     */
    public double[] getData()
    {
        return data;
    }

    /**
     * Return a copy of the spectrum data errors.
     */
    public double[] getDataErrors()
    {
        return errors;
    }

    /**
     * Return the NDX shape.
     */
    public int[] getDims()
    {
        int[] dims = null;
        try {
            long[] ldims =  ndx.getImage().getShape().getDims();
            dims = new int[ldims.length];
            for ( int i = 0; i < dims.length; i++ ) {
                dims[i] = (int) ldims[i];
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            dims = new int[0];
            dims[0] = 0;
        }
        return dims;
    }

    /**
     * Return a symbolic name.
     */
    public String getShortName()
    {
        return shortName;
    }

    /**
     * Return the full name of the NDX.
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * Return reference to NDX AST frameset.
     */
    public FrameSet getAst()
    {
        try {
            return Ndxs.getAst( ndx );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the data format.
     */
    public String getDataFormat()
    {
        return "NDX";
    }

    /**
     * Save the spectrum to disk-file.
     */
    public void save() 
        throws SplatException
    {
        throw new SplatException( "Saving NDX spectra is not implemented" );
    }

    /**
     * Return a keyed value from the NDF character components or FITS headers.
     * Returns "" if not found.
     */
    public String getProperty( String key )
    {
        //  NDX offers title. 
        if ( key.equalsIgnoreCase( "title" ) ) {
            return ndx.getTitle();
        }

        //Should offer label and units sometime.
        /*
        if ( key.equalsIgnoreCase( "label" ) ) {
            return ndx.getTitle();
        }
        if ( key.equalsIgnoreCase( "unit" ) ) {
            return ndx.getTitle();
        }
        */
        return "";
    }

//
//  Implementation specific methods and variables.
//
    /**
     * Reference to NDX object.
     */
    protected Ndx ndx = null;

    /**
     * Reference to NDX Image component Access object.
     */
    protected ArrayAccess imAccess = null;

    /**
     * Reference to NDX Error component Access object.
     */
    protected ArrayAccess errAccess = null;

    /**
     * Original HDX specification that pointed to the NDX.
     */
    protected String fullName;

    /**
     * Short name for the NDX (if any, defaults to long name).
     */
    protected String shortName;

    /**
     * Reference to data values.
     */
    protected double[] data = null;

    /**
     * Reference to data errors.
     */
    protected double[] errors = null;

    /**
     * Static variable for creating unique names.
     */
    private static int counter = 0;

    /**
     * Open an HDX description and locate the NDX.
     *
     * @param hdxName HDX description of the NDX (could be a simple
     *                XML file or my include an XPath component, this
     *                should be dealt with by HDX).
     */
    protected void open( String hdxName )
        throws SplatException
    {
        try {
            // Check for a local file first. Windows doesn't like 
            // using a "file:." for these.
            URL url = null;
            File infile = new File( hdxName );
            if ( infile.exists() ) {
                url = infile.toURL();
            }
            else {
                url = new URL( new URL( "file:." ), hdxName );
            }
            open( url );
        }
        catch ( Exception e ) {
            throw new SplatException( e );
        }
    }

    /**
     * Open an HDX description and locate the NDX.
     */
    protected void open( URL url )
        throws SplatException
    {
        try {
            NdxIO ndxIO = new NdxIO();
            ndx = ndxIO.makeNdx( url, AccessMode.READ );
        }
        catch ( Exception e ) {
            throw new SplatException( e );
        }
        if ( ndx == null ) {
            throw new SplatException( "Document contains no NDXs" );
        }
        setNames( url.toString(), false );

        //  Read in the data.
        readData();
    }

    /**
     * Open an NDX stored in a Source.
     */
    protected void open( Source source )
        throws SplatException
    {
        try {
            // Get a HDX container with the NDX inside.
            ndx = XMLNdxHandler.getInstance().makeNdx( source,
                                                       AccessMode.READ );
        }
        catch (Exception e) {
            throw new SplatException( e );
        }
        if ( ndx == null ) {
            throw new SplatException( "Document contains no NDXs" );
        }
        setNames( "Sourced NDX", true );

        //  Read in the data.
        readData();
    }

    /**
     * Open an NDX stored in an Element.
     *
     * @param ndxElement the Element.
     */
    protected void open( Element ndxElement )
        throws SplatException
    {
        DOMSource ndxSource = new DOMSource( ndxElement );
        open( ndxSource );
        setNames( "Wired NDX", true );
    }

    /**
     * Use an existing NDX.
     *
     * @param ndx the NDX.
     */
    protected void open( Ndx ndx )
        throws SplatException
    {
        this.ndx = ndx;
        setNames( "NDX", true );
        readData();
    }

    //  Match names to title of the ndx, or generate a unique title from
    //  a prefix.
    protected void setNames( String defaultPrefix, boolean unique ) 
    {
        String title = ndx.hasTitle() ? ndx.getTitle() : "";
        if ( title == null || title.equals( "" ) ) {
            if ( unique ) {
                title = defaultPrefix + " (" + (counter++) + ")";
            }
            else {
                title = defaultPrefix;
            }
        }
        fullName = title;
        shortName = title;
    }

    /**
     * Read in the NDX data components. These are copied to local
     * double precision array.
     */
    protected void readData()
        throws SplatException
    {
        // Use a Requirements object to make sure that data is all the
        // same shape. We also require that the data be returned in
        // double precision and using our BAD data value.
        Requirements req = new Requirements();
        req.setType( Type.DOUBLE );
        BadHandler badHandler = 
            BadHandler.getHandler( Type.DOUBLE, new Double(SpecData.BAD) );
        req.setBadHandler( badHandler );
        OrderedNDShape oshape = ndx.getImage().getShape();
        req.setShape( oshape );

        try {
            NDArray imNda = Ndxs.getMaskedImage( ndx, req );
            imAccess = imNda.getAccess();
            if ( ndx.hasVariance() ) {
                NDArray errNda = Ndxs.getMaskedErrors( ndx, req );
                errAccess = errNda.getAccess();
            }
        }
        catch (Exception e) {
            data = null;
            throw new SplatException( e );
        }

        //  No spectra longer than (int)?
        int size = (int) oshape.getNumPixels();

        // Get the data and possibly errors.
        try {
            if ( imAccess.isMapped() ) {
                data = (double []) imAccess.getMapped();
            }
            else {
                data = new double[ size ];
                imAccess.read( data, 0, size );
                imAccess.close();
            }

            errors = null;
            if ( errAccess != null ) {
                if ( errAccess.isMapped() ) {
                    errors = (double[]) errAccess.getMapped();
                }
                else {
                    errors = new double[ size ];
                    errAccess.read( errors, 0, size );
                    errAccess.close();
                }
            }
        }
        catch ( IOException e ) {
            data = errors = null;
            throw new SplatException( e );
        }
        return;
    }

    /**
     * Finalise object. Free any resources associated with member
     * variables.
     */
    protected void finalize() 
        throws Throwable
    {
        if ( imAccess != null ) {
            imAccess.close();
        }
        if ( errAccess != null ) {
            errAccess.close();
        }
        super.finalize();
    }
}
