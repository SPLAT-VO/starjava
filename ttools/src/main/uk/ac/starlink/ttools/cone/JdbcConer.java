package uk.ac.starlink.ttools.cone;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.task.ChoiceParameter;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.ParameterValueException;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.ttools.task.ConnectionParameter;

/**
 * Coner implementation which works by performing SELECT statements over a
 * JDBC database connection.
 *
 * @author   Mark Taylor
 * @since    15 Aug 2007
 */
public class JdbcConer implements Coner {

    private final ConnectionParameter connParam_;
    private final Parameter dbtableParam_;
    private final Parameter dbraParam_;
    private final Parameter dbdecParam_;
    private final Parameter dbtileParam_;
    private final TilingParameter tilingParam_;
    private final Parameter colsParam_;
    private final Parameter whereParam_;
    private final ChoiceParameter dbunitParam_;

    /**
     * Constructor.
     */
    public JdbcConer() {
        String sys = getSkySystem();
        sys = ( sys == null ) ? ""
                              : ( sys + " " );

        connParam_ = new ConnectionParameter( "db" );

        dbtableParam_ = new Parameter( "dbtable" );
        dbtableParam_.setUsage( "<table-name>" );
        dbtableParam_.setPrompt( "Name of table in database" );
        dbtableParam_.setDescription( new String[] {
            "<p>The name of the table in the SQL database which provides",
            "the remote data.",
            "</p>",
        } );

        dbraParam_ = new Parameter( "dbra" );
        dbraParam_.setUsage( "<sql-col>" );
        dbraParam_.setPrompt( "Name of right ascension column in database" );
        dbraParam_.setDescription( new String[] {
            "<p>The name of a column in the SQL database table",
            "<code>" + dbtableParam_.getName() + "</code>",
            "which gives the " + sys + "right ascension in degrees.",
            "</p>",
        } );

        dbdecParam_ = new Parameter( "dbdec" );
        dbdecParam_.setUsage( "<sql-col>" );
        dbdecParam_.setPrompt( "Name of declination column in database" );
        dbdecParam_.setDescription( new String[] {
            "<p>The name of a column in the SQL database table",
            "<code>" + dbtableParam_.getName() + "</code>",
            "which gives the " + sys + "declination in degrees.",
            "</p>",
        } );

        dbunitParam_ = new ChoiceParameter( "dbunit" );
        dbunitParam_.addOption( AngleUnits.DEGREES, "deg" );
        dbunitParam_.addOption( AngleUnits.RADIANS, "rad" );
        dbunitParam_.setDefault( "deg" );
        dbunitParam_.setPrompt( "Units of ra/dec values in database" );
        dbunitParam_.setDescription( new String[] {
            "<p>Units of the right ascension and declination columns",
            "identified in the database table.",
            "May be either deg[rees] (the default) or rad[ians].",
            "</p>",
        } );

        tilingParam_ = new TilingParameter( "tiling" );

        dbtileParam_ = new Parameter( "dbtile" );
        dbtileParam_.setUsage( "<sql-col>" );
        dbtileParam_.setNullPermitted( true );
        dbtileParam_.setPrompt( "Name of tiling column in database" );
        dbtileParam_.setDescription( new String[] {
            "<p>The name of a column in the SQL database table",
            "<code>" + dbtableParam_.getName() + "</code>",
            "which contains a sky tiling pixel index.",
            "The tiling scheme is given by the " + tilingParam_.getName(),
            "parameter.",
            "Use of a tiling column is optional, but if present",
            "(and if the column is indexed in the database table)",
            "it may serve to speed up searches.",
            "Set to null if the database table contains no tiling column",
            "or if you do not wish to use one.",
            "</p>",
        } );

        colsParam_ = new Parameter( "selectcols" );
        colsParam_.setUsage( "<sql-cols>" );
        colsParam_.setPrompt( "Database columns to select" );
        colsParam_.setDescription( new String[] {
            "<p>An SQL expression for the list of columns to be selected",
            "from the table in the database.",
            "A value of \"<code>*</code>\" retrieves all columns.",
            "</p>",
        } );
        colsParam_.setDefault( "*" );

        whereParam_ = new Parameter( "where" );
        whereParam_.setUsage( "<sql-condition>" );
        whereParam_.setPrompt( "Additional WHERE restriction on selection" );
        whereParam_.setNullPermitted( true );
        whereParam_.setDescription( new String[] {
            "<p>An SQL expression further limiting the rows to be selected",
            "from the database.  This will be combined with the constraints",
            "on position implied by the cone search centres and radii.",
            "The value of this parameter should just be a condition,",
            "it should not contain the <code>WHERE</code> keyword.",
            "A null value indicates no additional criteria.",
            "</p>",
        } );
    }

    /**
     * Returns the empty string.  No particular coordinate system is
     * mandated by this object.
     */
    public String getSkySystem() {
        return "";
    }

    public Parameter[] getParameters() {
        List pList = new ArrayList();
        pList.add( connParam_ );
        pList.addAll( Arrays.asList( connParam_.getAssociatedParameters() ) );
        pList.add( dbtableParam_ );
        pList.add( dbraParam_ );
        pList.add( dbdecParam_ );
        pList.add( dbunitParam_ );
        pList.add( tilingParam_ );
        pList.add( dbtileParam_ );
        pList.add( colsParam_ );
        pList.add( whereParam_ );
        return (Parameter[]) pList.toArray( new Parameter[ 0 ] );
    }

    public ConeSearcher createSearcher( Environment env, boolean bestOnly )
            throws TaskException {
        Connection connection = connParam_.connectionValue( env );
        String table = dbtableParam_.stringValue( env );
        String raCol = dbraParam_.stringValue( env );
        String decCol = dbdecParam_.stringValue( env );
        String tileCol = dbtileParam_.stringValue( env );
        SkyTiling tiling = tileCol == null ? null
                                           : tilingParam_.tilingValue( env );
        AngleUnits units = (AngleUnits) dbunitParam_.objectValue( env );
        String cols = colsParam_.stringValue( env );
        String where = whereParam_.stringValue( env );
        if ( where != null &&
             where.toLowerCase().trim().startsWith( "where" ) ) {
            String msg = "Omit <code>WHERE</code> keyword from "
                       + "<code>" + whereParam_.getName() + "</code> parameter";
            throw new ParameterValueException( whereParam_, msg );
        }
        try {
            return new JdbcConeSearcher( connection, table, raCol, decCol,
                                         units, tileCol, tiling, cols,
                                         where, bestOnly );
        }
        catch ( SQLException e ) {
            throw new TaskException( "Error preparing SQL statement: "
                                   + e.getMessage(), e );
        }
    }
}
