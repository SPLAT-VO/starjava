package uk.ac.starlink.ttools.cone;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.starlink.table.StarTable;

/**
 * Straightforward implementation of ConeResultRowSequence based on a
 * ConeQueryRowSequence.
 *
 * @author   Mark Taylor
 * @since    16 Jan 2008
 */
public class SequentialResultRowSequence implements ConeResultRowSequence {

    private final ConeQueryRowSequence querySeq_;
    private final ConeSearcher coneSearcher_;
<<<<<<< HEAD
    private final ConeErrorPolicy errAct_;
    private final Coverage coverage_;
=======
    private final Footprint footprint_;
>>>>>>> finished merging changes in trunk to branch splat-ari
    private final boolean bestOnly_;
    private final boolean distFilter_;
    private final String distanceCol_;
    private int nQuery_;
    private int nSkip_;
    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.cone" );

    /**
     * Constructor.
     *
     * @param  querySeq  sequence providing cone search query parameters
     * @param  coneSearcher  cone search implementation
<<<<<<< HEAD
     * @param  errAct   defines action on cone search invocation error
     * @param  coverage   coverage for results, or null
=======
     * @param  footprint   coverage footprint for results, or null
>>>>>>> finished merging changes in trunk to branch splat-ari
     * @param  bestOnly  whether all results or just best are required
     * @param  distFilter  true to perform post-query filtering on results
     *                     based on the distance between the query position
     *                     and the result row position
     * @param  distanceCol  name of column to hold distance information 
     *                      in output table, or null
     */
    public SequentialResultRowSequence( ConeQueryRowSequence querySeq,
                                        ConeSearcher coneSearcher,
<<<<<<< HEAD
                                        ConeErrorPolicy errAct,
                                        Coverage coverage, boolean bestOnly,
=======
                                        Footprint footprint, boolean bestOnly,
>>>>>>> finished merging changes in trunk to branch splat-ari
                                        boolean distFilter,
                                        String distanceCol ) {
        querySeq_ = querySeq;
        coneSearcher_ = coneSearcher;
<<<<<<< HEAD
        errAct_ = errAct;
        coverage_ = coverage;
=======
        footprint_ = footprint;
>>>>>>> finished merging changes in trunk to branch splat-ari
        bestOnly_ = bestOnly;
        distFilter_ = distFilter;
        distanceCol_ = distanceCol;
    }

    public StarTable getConeResult() throws IOException {
        double ra = querySeq_.getRa();
        double dec = querySeq_.getDec();
        double radius = querySeq_.getRadius();

        /* Ensure that at least one query is performed even if all points
<<<<<<< HEAD
         * are outside the coverage.  This way the metadata for an empty
         * table is returned, so at least you have the columns. */
        boolean excluded = nQuery_ + nSkip_ > 0
                        && coverage_ != null
                        && ! coverage_.discOverlaps( ra, dec, radius );
=======
         * are outside the footprint.  This way the metadata for an empty
         * table is returned, so at least you have the columns. */
        boolean excluded = nQuery_ + nSkip_ > 0
                        && footprint_ != null
                        && ! footprint_.discOverlaps( ra, dec, radius );
>>>>>>> finished merging changes in trunk to branch splat-ari
        if ( excluded ) {
            Level level = Level.CONFIG;
            if ( logger_.isLoggable( level ) ) {
                logger_.log( level,
<<<<<<< HEAD
                             "Skipping cone query for point outside coverage "
=======
                             "Skipping cone query for point outside footprint "
>>>>>>> finished merging changes in trunk to branch splat-ari
                           + "(" + (float) ra + "," + (float) dec + ")+"
                           + (float) radius );
            }
            nSkip_++;
            return null;
        }
        else {
            nQuery_++;
            return ConeMatcher
<<<<<<< HEAD
                  .getConeResult( coneSearcher_, errAct_, bestOnly_,
                                  distFilter_, distanceCol_, ra, dec, radius );
=======
                  .getConeResult( coneSearcher_, bestOnly_, distFilter_,
                                  distanceCol_, ra, dec, radius );
>>>>>>> finished merging changes in trunk to branch splat-ari
        }
    }

    public boolean next() throws IOException {
        return querySeq_.next();
    }

    public Object getCell( int icol ) throws IOException {
        return querySeq_.getCell( icol );
    }

    public Object[] getRow() throws IOException {
        return querySeq_.getRow();
    }

    public void close() throws IOException {
        querySeq_.close();
<<<<<<< HEAD
        if ( coverage_ != null ) {
=======
        if ( footprint_ != null ) {
>>>>>>> finished merging changes in trunk to branch splat-ari
            logger_.info( "Submitted " + nQuery_ + ", " + "skipped " + nSkip_
                        + " queries to service" );
        }
    }

    public double getDec() throws IOException {
        return querySeq_.getDec();
    }

    public double getRa() throws IOException {
        return querySeq_.getRa();
    }

    public double getRadius() throws IOException {
        return querySeq_.getRadius();
    }

    public long getIndex() throws IOException {
        return querySeq_.getIndex();
    }
}
