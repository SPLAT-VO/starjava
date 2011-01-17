package uk.ac.starlink.vo;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.TableSequence;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.table.gui.TableLoader;

/**
 * Table load dialogue which allows cone searches.  Cone search services
 * are obtained from a registry.
 *
 * @author   Mark Taylor (Starlink)
 * @since    21 Dec 2004
 */
public class ConeSearchDialog extends SkyDalTableLoadDialog {

    private DoubleValueField raField_;
    private DoubleValueField decField_;
    private DoubleValueField srField_;
    private static final ValueInfo SR_INFO =
        new DefaultValueInfo( "Radius", Double.class, "Search Radius" );

    /**
     * Constructor.
     */
    public ConeSearchDialog() {
        super( "Cone Search",
               "Obtain source catalogues using cone search web services",
               Capability.CONE, true, false );
        setIconUrl( ConeSearchDialog.class.getResource( "cone.gif" ) );
    }

    protected Component createQueryComponent() {
        Component queryPanel = super.createQueryComponent();
        SkyPositionEntry skyEntry = getSkyEntry();
        raField_ = skyEntry.getRaDegreesField();
        decField_ = skyEntry.getDecDegreesField();
        srField_ = DoubleValueField.makeSizeDegreesField( SR_INFO );
        skyEntry.addField( srField_ );
        return queryPanel;
    }

    public TableLoader createTableLoader() {
        String serviceUrl = getServiceUrl();
        checkUrl( serviceUrl );
        final ConeSearch coner = new ConeSearch( serviceUrl );
        final double ra = raField_.getValue();
        final double dec = decField_.getValue();
        final double sr = srField_.getValue();
        final int verb = 0;
        final List metadata = new ArrayList();
        metadata.addAll( Arrays.asList( new DescribedValue[] {
            raField_.getDescribedValue(),
            decField_.getDescribedValue(),
            srField_.getDescribedValue(),
        } ) );
        metadata.addAll( Arrays.asList( getResourceMetadata( serviceUrl ) ) );
        final String summary = getQuerySummary( serviceUrl, sr );
        return new TableLoader() {
            public TableSequence loadTables( StarTableFactory factory )
                    throws IOException {
                StarTable st = coner.performSearch( ra, dec, sr, verb,
                                                    factory );
                st.getParameters().addAll( metadata );
                return Tables.singleTableSequence( st );
            }
            public String getLabel() {
                return summary;
            }
        };
    }
}
