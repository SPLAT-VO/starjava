package uk.ac.starlink.table.formats;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.TableBuilder;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.util.DataSource;

/**
 * A table builder which reads tables in Comma-Separated Values format.
 * The detailed format of input file which is understood is documented
 * fully in the {@link CsvStarTable} class.
 *
 * @author   Mark Taylor (Starlink)
 * @since    21 Sep 2004
 */
public class CsvTableBuilder implements TableBuilder {

    public String getFormatName() {
        return "CSV";
    }

    public boolean canImport( DataFlavor flavor ) {
        return false;
    }

    public StarTable makeStarTable( DataSource datsrc, boolean wantRandom,
                                    StoragePolicy policy )
            throws TableFormatException, IOException {
        return new CsvStarTable( datsrc );
    }
}
