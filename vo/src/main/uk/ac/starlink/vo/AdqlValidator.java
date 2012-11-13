package uk.ac.starlink.vo;

import adql.db.DBChecker;
import adql.db.DBColumn;
import adql.db.DBTable;
import adql.db.DefaultDBColumn;
import adql.db.DefaultDBTable;
import adql.parser.ADQLParser;
import adql.parser.ADQLQueryFactory;
import adql.parser.ParseException;
import adql.parser.QueryChecker;
import adql.parser.TokenMgrError;
import adql.query.ADQLIterator;
import adql.query.ADQLObject;
import adql.query.ADQLQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Handles validation of ADQL queries.
 * In the current implementation the heavy lifting is done by 
 * Gregory Mantelet's ADQL parser.
 *
 * @author   Mark Taylor
 * @since    3 Oct 2011
 */
public class AdqlValidator {

    private final ADQLParser parser_;
    private final QueryChecker checker_;

    /**
     * Constructor.
     *
     * @param  vtables  table metadata for database to be checked against
<<<<<<< HEAD
     * @param  allowUdfs  whether unknown functions should cause a parse error
     */
    public AdqlValidator( ValidatorTable[] vtables, boolean allowUdfs ) {
        parser_ = new ADQLParser( new ADQLQueryFactory( allowUdfs ) );
=======
     */
    public AdqlValidator( ValidatorTable[] vtables ) {
        parser_ = new ADQLParser();
>>>>>>> finished merging changes in trunk to branch splat-ari
        parser_.setDebug( false );
        checker_ = vtables == null ? null
                                   : new DBChecker( toDBTables( vtables ) );
    }

    /**
     * Validates an ADQL string.
     * Any throwable returned hopefully includes useful information about
     * the location and nature of the parse error, but that depends on the
     * implementation.
     *
     * @param  query   ADQL query string
     * @throws  Throwable   if the string is not valid ADQL
     */
    public void validate( String query ) throws Throwable {
        ADQLQuery pq = parser_.parseQuery( query );
        if ( checker_ != null ) {
            try {
                checker_.check( pq );
            }
            catch ( ParseException e ) {
                throw e;
            }
            catch ( TokenMgrError e ) {
                throw e;
            }
        }

        /* Another possible check would be to identify unknown
         * User-Defined Functions here by walking the parse tree and
         * noting UserFunction instances.  However, at present, there
         * is no way to identify where in the ADQL text these appear. */
    }

    /**
     * Utility method to adapt a TableMeta object into a ValidatorTable.
     *
     * @param  tmeta  input table metadata object
     * @return  table metadata object suitable for validation by this class
     */
    public static ValidatorTable toValidatorTable( final TableMeta tmeta ) {
        ColumnMeta[] cmetas = tmeta.getColumns();
        final ValidatorColumn[] vcols = new ValidatorColumn[ cmetas.length ];
        final ValidatorTable vtable = new ValidatorTable() {
            public String getName() {
                return tmeta.getName();
            }
            public ValidatorColumn[] getColumns() {
                return vcols;
            }
        };
        for ( int ic = 0; ic < cmetas.length; ic++ ) {
            final ColumnMeta cmeta = cmetas[ ic ];
            vcols[ ic ] = new ValidatorColumn() {
                public String getName() {
                    return cmeta.getName();
                }
                public ValidatorTable getTable() {
                    return vtable;
                }
            };
        }
        return vtable;
    }

    /**
     * Adapts an array of ValidatorTable objects to a DBTable collection.
     *
     * @param  vtables  table metadata in VO package form
     * @return  table metadata in ADQLParser-friendly form
     */
    private static Collection<DBTable> toDBTables( ValidatorTable[] vtables ) {
        Collection<DBTable> tList = new ArrayList<DBTable>();
        for ( int i = 0; i < vtables.length; i++ ) {
            tList.add( toDBTable( vtables[ i ] ) );
        }
        return tList;
    }

    /**
     * Adapts a ValidatorTable object to a DBTable.
     *
     * @param   vtable  table metadata in VO package form
     * @return  table metadata in ADQLParser-friendly form
     */
    private static DBTable toDBTable( ValidatorTable vtable ) {
        DefaultDBTable dbTable = new DefaultDBTable( vtable.getName() );
        ValidatorColumn[] vcols = vtable.getColumns();
        for ( int ic = 0; ic < vcols.length; ic++ ) {
            dbTable.addColumn( new DefaultDBColumn( vcols[ ic ].getName(),
                                                    dbTable ) );
        }
        return dbTable;
    }

    /**
     * Tests parser.  Use <code>-h</code> for usage.
     */
    public static void main( String[] args )
            throws Throwable,
            java.io.IOException, org.xml.sax.SAXException {
        String usage = "\n   Usage: " + AdqlValidator.class.getName()
                     + " [-meta <tmeta-url>]"
                     + " [-[no]udfs]"
                     + " <query>"
                     + "\n";
        TableMeta[] tmetas = null;
        boolean allowUdfs = false;
        ArrayList<String> argList =
<<<<<<< HEAD
        new ArrayList<String>( java.util.Arrays.asList( args ) );
        for ( Iterator<String> it = argList.iterator(); it.hasNext(); ) {
            String arg = it.next();
            if ( arg.startsWith( "-h" ) ) {
                System.out.println( usage );
                return;
            }
            else if ( arg.equals( "-meta" ) && it.hasNext() ) {
                it.remove();
                String loc = it.next();
                it.remove();
=======
            new ArrayList<String>( java.util.Arrays.asList( args ) );
        try {
            if ( argList.get( 0 ).startsWith( "-h" ) ) {
                System.out.println( usage );
                return;
            }
            if ( argList.get( 0 ).equals( "-meta" ) ) {
                argList.remove( 0 );
                String loc = argList.remove( 0 );
>>>>>>> finished merging changes in trunk to branch splat-ari
                tmetas = TableSetSaxHandler
                        .readTableSet( new java.net.URL( loc ) );
            }
            else if ( arg.equals( "-udfs" ) ) {
                it.remove();
                allowUdfs = true;
            }
            else if ( arg.equals( "-noudfs" ) ) {
                it.remove();
                allowUdfs = false;
            }
        }
<<<<<<< HEAD
        if ( argList.size() != 1 ) {
=======
        ValidatorTable[] vtables = null;
        if ( tmetas != null ) {
            vtables = new ValidatorTable[ tmetas.length ];
            for ( int i = 0; i < tmetas.length; i++ ) {
                vtables[ i ] = toValidatorTable( tmetas[ i ] );
            }
        }
        if ( query == null || ! argList.isEmpty() ) {
>>>>>>> finished merging changes in trunk to branch splat-ari
            System.err.println( usage );
            System.exit( 1 );
            return;
        }
<<<<<<< HEAD
        String query = argList.remove( 0 );
        ValidatorTable[] vtables = null;
        if ( tmetas != null ) {
            vtables = new ValidatorTable[ tmetas.length ];
            for ( int i = 0; i < tmetas.length; i++ ) {
                vtables[ i ] = toValidatorTable( tmetas[ i ] );
            }
        }
        new AdqlValidator( vtables, allowUdfs ).validate( query );
=======
        new AdqlValidator( vtables ).validate( query );
>>>>>>> finished merging changes in trunk to branch splat-ari
    }

    /**
     * Defines table metadata for tables known to the validator.
     */
    public interface ValidatorTable {

        /**
         * Returns the name of this table, including any schema.
         *
         * @return  table name
         */
        String getName();

        /**
         * Returns an array of columns associated with this table.
         *
         * @return  column array
         */
        ValidatorColumn[] getColumns();
    }

    /**
     * Defines column metadata for table columns known to the validator.
     */
    public interface ValidatorColumn {

        /**
         * Returns the name of this column.
         *
         * @return  column name
         */
        String getName();

        /**
         * Returns the table which owns this column.
         *
         * @return  owner table object
         */
        ValidatorTable getTable();
    }
}
