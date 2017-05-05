/*
 * Copyright (C) 2003-2004 Central Laboratory of the Research Councils
 *
 *  History:
 *     01-SEP-2003 (Peter W. Draper):
 *        Original version.
 *     26-FEB-2004 (Peter W. Draper):
 *        Added column name methods.
 */
package uk.ac.starlink.splat.data;

import java.io.Serializable;

import uk.ac.starlink.splat.util.SplatException;
import uk.ac.starlink.splat.util.UnitUtilities;
import uk.ac.starlink.ast.FrameSet;

/**
 * Abstract base class for accessing spectral data stored in various
 * data formats.
 *
 * @author Peter W. Draper
 * @version $Id$
 * @see "The Bridge Design Pattern"
 */
public abstract class AbstractSpecDataImpl 
    implements SpecDataImpl, Serializable
{
    /**
     * Constructor - create instance of class.
     *
     * @param name The specification of the spectrum (disk file name
     * etc.).
     */
    public AbstractSpecDataImpl( String name ) 
        throws SplatException
    {
        // Does nothing here.
    }

    /**
     * Constructor, clone from another spectrum.
     *
     * @param name a symbolic name for the spectrum.
     * @param spectrum the spectrum to clone.
     */
    public AbstractSpecDataImpl( String name, SpecData spectrum ) 
        throws SplatException
    {
        this.parentImpl = spectrum.getSpecDataImpl();
    }

    /**
     *  Serialization version ID string (generated by serialver on
     *  original star.jspec.data.SpecDataImpl class).
     */
    static final long serialVersionUID = 3491717813846440966L;
    /**
     * Return a complete array of data values.
     */
    abstract public double[] getData();

    /**
     * Return a complete array of data error values.
     */
    abstract public double[] getDataErrors();

    /**
     * Get the dimensionality of the spectrum.
     */
    abstract public int[] getDims();

    /**
     * Return a symbolic name for the spectrum.
     */
    abstract public String getShortName();

    /**
     * Return a full name for the spectrum (this should be the disk
     * file is associated, otherwise a URL or null).
     */
    abstract public String getFullName();

    /**
     * Return reference to AST frameset that specifies the
     * coordinates associated with the data value positions.
     */
    abstract public FrameSet getAst();

    /**
     * Return the data format as a recognisable short name.
     */
    abstract public String getDataFormat();

    /**
     * Ask spectrum if it can save itself to disk file. Throws
     * SplatException is this fails, or is not available.
     */
    abstract public void save() throws SplatException;

    /**
     * Return a keyed value from the FITS headers. Returns "" if not
     * found. This default implementation returns "", except for "units" and
     * "label", which return the values set by {@link #setDataUnits}
     * and {@link #setDataLabel}.
     */
    public String getProperty( String key )
    {
        if ( "units".equals( key ) ) {
            return dataUnits;
        }
        if ( "label".equals( key ) ) {
            return dataLabel;
        }
        return "";
    }

    /**
     * Value for the data units. Set this if you can.
     */
    protected String dataUnits = "unknown";

    /**
     * Value for the data label. Set this if you can.
     */
    protected String dataLabel = "data values";

    //  Set the data units string.
    public void setDataUnits( String dataUnits )
    {
        if ( dataUnits != null ) {
            this.dataUnits = UnitUtilities.fixUpUnits( dataUnits );
        }
        else {
            this.dataUnits = "unknown";
        }
    }

    //  Get the data units.
    public String getDataUnits()
    {
        return dataUnits;
    }

    //  Set the data label string.
    public void setDataLabel( String dataLabel )
    {
        if ( dataLabel != null ) {
            this.dataLabel = dataLabel;
        }
        else {
            this.dataLabel = "data values";
        }
    }

    //  Get the data label.
    public String getDataLabel()
    {
        return dataLabel;
    }

    /**
     * Whether the class is a FITSHeaderSource, i.e.<!-- --> has FITS headers
     * associated with it and implements FITSHeaderSource.
     * Implementations that can store FITS headers may want to access
     * these.
     */
    public boolean isFITSHeaderSource()
    {
        return ( this instanceof FITSHeaderSource );
    }

    /**
     * The reference to a parent implementation.
     */
    protected SpecDataImpl parentImpl = null;
    
    private ObjectTypeEnum objectType;
    private String timeSystem;
    
    /**
     * Reference to another SpecDataImpl that is a "parent" of this
     * instance. This facility is provided so that data formats that
     * have extra information that is outside of the model inferred by
     * SpecData and SpecDataImpl may be able to recover this when
     * saing themselves to disk file (the case in mind is actually the
     * NDF, a new NDF should really be a copy of any NDF that it is
     * related too, so that information in the MORE extension can be
     * preserved).
     */
    public SpecDataImpl getParentImpl()
    {
        return parentImpl;
    }

    /**
     * Set reference to another SpecDataImpl that is a "parent" of this
     * instance.
     *
     * @see #getParentImpl
     */
    public void setParentImpl( SpecDataImpl parentImpl )
    {
        this.parentImpl = parentImpl;
    }

    //
    // Column name implementations. Default set is immutable.
    //
    public String[] getColumnNames()
    {
        // Not mutable.
        return null;
    }

    public String getCoordinateColumnName()
    {
        return "Coordinates";
    }
    
    public void setCoordinateColumnName( String name )
        throws SplatException
    {
        // Do nothing.
    }

    public String getDataColumnName()
    {
        return "Data values";
    }

    public void setDataColumnName( String name )
        throws SplatException
    {
        // Do nothing.
    }

    //  No error column by default.
    public String getDataErrorColumnName()
    {
        return "";
    }

    public void setDataErrorColumnName( String name )
        throws SplatException
    {
        // Do nothing.
    }
    
    @Override
    public ObjectTypeEnum getObjectType() {
    	return objectType;
    }
    
    @Override
    public void setObjectType(ObjectTypeEnum objectType) {
    	this.objectType = objectType;
    }
    
    @Override
    public String getTimeSystem() {
        return this.timeSystem;
    } 
    
    @Override
    public void setTimeSystem(String ts) {
        this.timeSystem = ts;
    }
}

