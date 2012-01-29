package org.geotools.data.wfs.internal;

import static org.geotools.data.wfs.internal.WFSOperationType.*;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class TransactionRequest extends WFSRequest {

    public TransactionRequest(WFSConfig config, WFSStrategy strategy) {
        super(TRANSACTION, config, strategy);
    }

    public void add(TransactionElement txElem);

    public List<TransactionElement> getTransactionElements();

    public Insert createInsert(SimpleFeatureType localType);

    public Update createUpdate(String localTypeName);

    public Delete createDelete(String localTypeName);

    public static interface TransactionElement {
        public String getLocalTypeName();
    }

    public static interface Insert extends TransactionElement {
        public void add(final SimpleFeature feature);
    }

    public static interface Update extends TransactionElement {

    }

    public static interface Delete extends TransactionElement {

    }
}
