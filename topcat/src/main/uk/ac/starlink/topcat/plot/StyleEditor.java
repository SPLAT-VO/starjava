package uk.ac.starlink.topcat.plot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import uk.ac.starlink.topcat.ActionForwarder;
import uk.ac.starlink.topcat.AuxWindow;

/**
 * Grahpical component which provides a GUI for editing the characteristics
 * of a {@link Style} object.  This is an abstract superclass; a specialised
 * implementation will be required for each <code>Style</code> implementation.
 * Since Style objects are usually immutable, this doesn't (necessarily)
 * edit a single style object; instead you configure it with an existing
 * style using the {@link #setState} method and later use the 
 * {@link #getStyle} method and others to obtain the new style which is
 * a result of the editing.
 *
 * @author   Mark Taylor
 * @since    10 Jan 2005
 */
public abstract class StyleEditor extends JPanel
                                  implements ActionListener, ChangeListener {

    private final JLabel legendLabel_;
    private final JTextField labelField_;
    private final ActionForwarder actionForwarder_;
    private boolean initialised_;
    private Style initialStyle_;
    private String initialLabel_;

    /**
     * Constructor.
     */
    public StyleEditor() {
        super();
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        actionForwarder_ = new ActionForwarder();

        /* Legend box. */
        legendLabel_ = new JLabel();
        labelField_ = new JTextField();
        labelField_.addActionListener( this );
        JComponent legendBox = Box.createHorizontalBox();
        legendBox.add( new JLabel( "Icon: " ) );
        legendBox.add( legendLabel_ );
        legendBox.add( Box.createHorizontalStrut( 10 ) );
        legendBox.add( new JLabel( "Label: " ) );
        legendBox.add( labelField_ );
        legendBox.add( Box.createHorizontalGlue() );
        legendBox.setBorder( AuxWindow.makeTitledBorder( "Legend" ) );
        add( legendBox );
    }

    public void setVisible( boolean visible ) {
        if ( visible && ! initialised_ ) {
            init();
            initialised_ = true;
        }
        super.setVisible( visible );
    }

    /**
     * Performs initialisation after construction but before the first
     * display of this component.
     */
    protected void init() {
        setState( null, "" );
    }

    /**
     * Sets the state of this component ready for editing.
     *
     * @param   style  style 
     * @param   label  textual label to use in legends annotating the
     *          style being edited
     */
    public void setState( Style style, String label ) {
        initialStyle_ = style;
        initialLabel_ = label;
        labelField_.setText( label );
        previewLegend( style );
        setStyle( style );
    }

    /**
     * Sets the style.  Implementations should configure their visual
     * state so that it matches the characteristics of the given style.
     *
     * @param  style current style
     */
    public abstract void setStyle( Style style );

    /**
     * Returns a style object derived from the current state of this
     * component.
     *
     * @return  current (edited) style
     */
    public abstract Style getStyle();

    /**
     * Returns the label currently entered in this component.
     *
     * @return   label
     */
    public String getLabel() {
        return labelField_.getText();
    }

    /**
     * Undoes any changes done since {@link #setState} was called.
     */
    public void cancelChanges() {
        setState( initialStyle_, initialLabel_ );
    }

    /**
     * Adds an action listener.  It will be notified every time something
     * the state described by this component changes.
     *
     * @param  listener   listener to add
     */
    public void addActionListener( ActionListener listener ) {
        actionForwarder_.addListener( listener );
    }

    /**
     * Removes an action listener which was previously added.
     *
     * @param   listener  listener to remove
     * @see  #addActionListener
     */
    public void removeActionListener( ActionListener listener ) {
        actionForwarder_.removeListener( listener );
    }

    /**
     * Invoked every time the style described by the current state of this
     * component changes.
     */
    public void actionPerformed( ActionEvent evt ) {
        previewLegend( getStyle() );
        repaint();
        actionForwarder_.actionPerformed( evt );
    }

    /**
     * Invoked every time the style described by the current state of this
     * component changes.
     */
    public void stateChanged( ChangeEvent evt ) {
        previewLegend( getStyle() );
        repaint();
        actionForwarder_.stateChanged( evt );
    }

    /**
     * Displays the legend for a given style in a visible place.
     *
     * @param  style   style to display
     */
    private void previewLegend( Style style ) {
        legendLabel_.setIcon( Styles.getLegendIcon( style, 20, 20 ) );
    }

}
