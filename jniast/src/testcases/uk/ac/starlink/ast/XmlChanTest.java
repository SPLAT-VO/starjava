package uk.ac.starlink.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

public class XmlChanTest extends TestCase {

    private String confusingID = 
        "<test>test &lt; &quotone&quot <![CDATA[&&<<>>]]>" +
        " test 'two' </test>";

    public XmlChanTest( String name ) {
        super( name );
    }

    public void testEncoding() throws IOException {
        AstObject obj1 = new Frame( 1 );
        obj1.setID( confusingID );
        XmlChan xc = new MemoryXmlChan();
        xc.write( obj1 );
        AstObject obj2 = xc.read();
        assertEquals( obj1.getID(), obj2.getID() );
    }

    public void testConstants() {
        assertEquals( "http://www.starlink.ac.uk/ast/xml/", 
                      XmlChan.AST__XMLNS );
    }

}
