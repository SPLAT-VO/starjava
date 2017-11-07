package uk.ac.starlink.topcat;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableOutput;
import uk.ac.starlink.table.gui.TableSaveChooser;

/**
 * Save panel for saving multiple tables to the same container file.
 *
 * @author   Mark Taylor
 * @since    15 Jul 2010
 */
public class MultiSavePanel extends SavePanel {

    private final TopcatModelSelectionTable tSelector_;
    private final TableModelListener tableListener_;
    private final int icolSubset_;
    private final int icolOrder_;
    private TableSaveChooser saveChooser_;

    /**
     * Constructor.
     *
     * @param  sto  output marshaller
     */
    public MultiSavePanel( StarTableOutput sto ) {
        super( "Multiple Tables",
               TableSaveChooser.makeFormatBoxModel( sto, true ) );

        /* Create and customise table selector table. */
        tSelector_ = new TopcatModelSelectionTable( "Save", true ) {
            protected int[] getEventColumnIndices( int evtCode ) {
                if ( evtCode == TopcatEvent.CURRENT_SUBSET ) {
                    return new int[] { icolSubset_ };
                }
                else if ( evtCode == TopcatEvent.CURRENT_ORDER ) {
                    return new int[] { icolOrder_ };
                }
                else {
                    return super.getEventColumnIndices( evtCode );
                }
            }
        };
        MetaColumnTableModel tModel = tSelector_.getTableModel();
        List<MetaColumn> metaList = tModel.getColumnList();

        /* Add column to display current subset. */
        icolSubset_ = metaList.size();
        metaList.add( new MetaColumn( "Subset", String.class,
                                      "Current Subset" ) {
            public Object getValue( int irow ) {
                RowSubset subset =
                    tSelector_.getTable( irow ).getSelectedSubset();
                return RowSubset.ALL.equals( subset ) ? null
                                                      : subset.toString();
            }
        } );

        /* Add column to display current sort order. */
        icolOrder_ = metaList.size();
        metaList.add( new MetaColumn( "Sort", String.class,
                                      "Current Sort Order" ) {
            public Object getValue( int irow ) {
                return tSelector_.getTable( irow ).getSelectedSort().toString();
            }
        } );

        /* Listener to ensure that chooser enabledness is set right. */
        tableListener_ = new TableModelListener() {
            public void tableChanged( TableModelEvent evt ) {
                if ( saveChooser_ != null ) {
                    saveChooser_.setEnabled( tSelector_.getSelectedTables()
                                                       .length > 0 );
                }
            }
        };

        /* Place components. */
        setLayout( new BorderLayout() );
        JTable jtable = new JTable( tModel );
        jtable.setRowSelectionAllowed( false );
        jtable.setColumnSelectionAllowed( false );
        jtable.setCellSelectionEnabled( false );
        TableColumnModel colModel = jtable.getColumnModel();
        colModel.getColumn( 0 ).setPreferredWidth( 32 );
        colModel.getColumn( 1 ).setPreferredWidth( 300 );
        colModel.getColumn( icolSubset_ ).setPreferredWidth( 80 );
        colModel.getColumn( icolOrder_ ).setPreferredWidth( 80 );
        JScrollPane scroller =
            new JScrollPane( jtable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        add( scroller, BorderLayout.CENTER );
    }

    public StarTable[] getTables() {
        TopcatModel[] tcModels = tSelector_.getSelectedTables();
        StarTable[] tables = new StarTable[ tcModels.length ];
        for ( int i = 0; i < tcModels.length; i++ ) {
            tables[ i ] = TopcatUtils.getSaveTable( tcModels[ i ] );
        }
        return tables;
    }

    public void setActiveChooser( TableSaveChooser chooser ) {
        TableModel tModel = tSelector_.getTableModel();
        saveChooser_ = chooser;
        if ( saveChooser_ == null ) {
            tModel.removeTableModelListener( tableListener_ );
        }
        else {
            tModel.addTableModelListener( tableListener_ );
            saveChooser_.setEnabled( tSelector_.getSelectedTables()
                                               .length > 0 );
        }
    }
}
