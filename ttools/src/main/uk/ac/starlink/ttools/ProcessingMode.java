package uk.ac.starlink.ttools;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import uk.ac.starlink.table.StarTable;

/**
 * Interface defining the final element of a table processing stream -
 * the one which disposes of the table in some way.
 *
 * @author   Mark Taylor (Starlink)
 * @since    11 Feb 2005
 */
public abstract class ProcessingMode {

    private PrintStream out_ = System.out;

    /**
     * Returns the name of this mode. 
     * The returned value should be short and preferably lower case - 
     * it will be used (with a prepended "-") as the name of a command
     * line flag.
     *
     * @return   mode name
     */
    public abstract String getName();

    /**
     * Perform disposal of the table.
     * 
     * @param  table  input table to do something with
     */
    public abstract void process( StarTable table ) throws IOException;

    /**
     * Consume a list of arguments.
     * Any arguments which this mode knows about should be noted and 
     * removed from the list.  Any others should be ignored,
     * and left in the list.
     * The return value should be true if everything looks OK, 
     * false if there is some syntax error in the arguments.
     * 
     * @param   argList  an array of strings obtained from the command line
     * @return  true  iff the arguments are unobjectionable
     */
    public boolean setArgs( List argList ) {
        return true;
    }

    /**
     * Returns the output stream to which processing modes should write
     * user-directed (human-readable) output.
     *
     * @return  standard output stream
     */
    public PrintStream getOutputStream() {
        return out_;
    }
}
