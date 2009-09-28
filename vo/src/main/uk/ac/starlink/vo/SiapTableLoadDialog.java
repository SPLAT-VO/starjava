package uk.ac.starlink.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.util.gui.ShrinkWrapper;

/**
 * Table load dialogue for retrieving the result of a SIAP query.
 * SIAP services are obtained from a registry.
 *
 * @author   Mark Taylor (Starlink)
 * @since    5 Dec 2005
 * @see      <http://www.ivoa.net/Documents/latest/SIA.html>
 */
public class SiapTableLoadDialog extends DalTableLoadDialog {

    private final DoubleValueField raField_;
    private final DoubleValueField decField_;
    private final DoubleValueField sizeField_;
    private final JComboBox formatSelector_;
    private static final ValueInfo SIZE_INFO =
        new DefaultValueInfo( "Angular Size", Double.class,
                              "Angular size of the search region"
                            + " in RA and Dec" );

    /**
     * Constructor.
     */
    public SiapTableLoadDialog() {
        super( "SIA Query",
               "Get results of a Simple Image Access Protocol query",
               new KeywordServiceQueryFactory( Capability.SIA ), true, true );
        SkyPositionEntry skyEntry = getSkyEntry();
        raField_ = skyEntry.getRaDegreesField();
        decField_ = skyEntry.getDecDegreesField();
        sizeField_ = DoubleValueField.makeSizeDegreesField( SIZE_INFO );
        sizeField_.getEntryField().setText( "0" );
        skyEntry.addField( sizeField_ );

        /* Add a selector for image format. */
        JComponent formatLine = Box.createHorizontalBox();
        formatSelector_ = new JComboBox( new String[] {
            "image/fits",
            "GRAPHIC",
            "ALL",
            "",
        } );
        formatSelector_.setEditable( true );
        formatSelector_.setSelectedIndex( 0 );
        formatLine.add( new JLabel( "Image Format: " ) );
        formatLine.add( new ShrinkWrapper( formatSelector_ ) );
        formatLine.add( Box.createHorizontalGlue() );
        getControlBox().add( Box.createVerticalStrut( 5 ) );
        getControlBox().add( formatLine );
    }

    public TableSupplier getTableSupplier() {
        String serviceUrl = getServiceUrl();
        checkUrl( serviceUrl );
        double ra = raField_.getValue();
        double dec = decField_.getValue();
        double size = sizeField_.getValue();
        final DalQuery query = new DalQuery( serviceUrl, "SIA", ra, dec, size );
        Object format = formatSelector_.getSelectedItem();
        if ( format != null && format.toString().trim().length() > 0 ) {
            query.addArgument( "FORMAT", format.toString() );
        }
        final List metadata = new ArrayList();
        metadata.addAll( Arrays.asList( new DescribedValue[] {
            raField_.getDescribedValue(),
            decField_.getDescribedValue(),
            sizeField_.getDescribedValue(),
        } ) );
        metadata.addAll( Arrays.asList( getResourceMetadata( serviceUrl ) ) );
        final String summary = getQuerySummary( serviceUrl, size );
        return new TableSupplier() {
            public StarTable getTable( StarTableFactory factory,
                                       String format ) throws IOException {
                StarTable st = query.execute( factory );
                st.getParameters().addAll( metadata );
                return st;
            }
            public String getTableID() {
                return summary;
            }
        };
    }

    public RegCapabilityInterface[] getCapabilities( RegResource resource ) {
        RegCapabilityInterface[] caps = super.getCapabilities( resource );
        List siapcapList = new ArrayList();
        for ( int i = 0; i < caps.length; i++ ) {
            if ( Capability.SIA.isInstance( caps[ i ] ) ) {
                siapcapList.add( caps[ i ] );
            }
        }
        return (RegCapabilityInterface[])
               siapcapList.toArray( new RegCapabilityInterface[ 0 ] );
    }
}
