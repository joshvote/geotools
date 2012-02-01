package org.geotools.data.wfs.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.WfsFactory;

import org.eclipse.emf.ecore.EObject;
import org.junit.Test;

/**
 * @author Gabriel Roldan
 * @since 2.6.x
 * 
 * 
 * 
 * @source $URL$
 * @version $Id$
 */
@SuppressWarnings("nls")
public class WFSExtensionsTest {

    @Test
    public void testFindParserFactory() {
        GetFeatureType request = WfsFactory.eINSTANCE.createGetFeatureType();
        request.setOutputFormat("application/fakeFormat");
        WFSResponseFactory factory = WFSExtensions.findParserFactory(request);
        assertNotNull(factory);
        assertTrue(factory instanceof TestParserFactory);
    }

    public static class TestParserFactory implements WFSResponseFactory {

        public boolean canProcess(EObject request) {
            return request instanceof GetFeatureType
                    && "application/fakeFormat"
                            .equals(((GetFeatureType) request).getOutputFormat());
        }

        public boolean isAvailable() {
            return true;
        }

        public WFSResponseParser createParser(WFSResponse response) throws IOException {
            throw new UnsupportedOperationException("not intended to be called for this test class");
        }
    }
}