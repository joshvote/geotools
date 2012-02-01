package org.geotools.data.wfs.internal;

import static org.geotools.data.wfs.internal.HttpMethod.*;
import static org.geotools.data.wfs.internal.WFSOperationType.GET_FEATURE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.geotools.data.ows.AbstractRequest;
import org.geotools.data.ows.HTTPResponse;
import org.geotools.data.ows.Request;

public abstract class WFSRequest extends AbstractRequest implements Request {

    protected final WFSStrategy strategy;

    protected final WFSOperationType operation;

    protected final WFSConfig config;

    private final boolean doPost;

    private QName typeName;

    private String outputFormat;

    public WFSRequest(final WFSOperationType operation, final WFSConfig config,
            final WFSStrategy strategy) {

        super(url(operation, config, strategy), (Properties) null);
        this.operation = operation;
        this.config = config;
        this.strategy = strategy;

        if (!config.isPreferPostOverGet()) {
            this.doPost = !strategy.supportsOperation(operation, GET);
        } else {
            this.doPost = strategy.supportsOperation(operation, POST);
        }

        this.outputFormat = strategy.getDefaultOutputFormat(operation);

        setProperty(SERVICE, "WFS");
        setProperty(VERSION, strategy.getVersion());
        setProperty(REQUEST, operation.getName());

    }

    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @param outputFormat
     *            the outputFormat to set
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void setTypeName(QName typeName) {
        this.typeName = typeName;
    }

    public QName getTypeName() {
        return typeName;
    }

    public WFSStrategy getStrategy() {
        return strategy;
    }

    private static URL url(final WFSOperationType operation, final WFSConfig config,
            final WFSStrategy strategy) {

        if (!strategy.supportsOperation(operation, GET)
                && !strategy.supportsOperation(operation, POST)) {
            throw new IllegalArgumentException("WFS doesn't support " + operation.getName());
        }

        HttpMethod method;
        if (!config.isPreferPostOverGet()) {
            method = !strategy.supportsOperation(GET_FEATURE, GET) ? POST : GET;
        } else {
            method = strategy.supportsOperation(GET_FEATURE, POST) ? POST : GET;
        }

        URL targetUrl = strategy.getOperationURL(WFSOperationType.GET_FEATURE, method);

        return targetUrl;
    }

    public WFSOperationType getOperation() {
        return operation;
    }

    @Override
    public boolean requiresPost() {
        return doPost;
    }

    @Override
    protected void initService() {
    }

    @Override
    protected void initVersion() {
    }

    @Override
    protected void initRequest() {
    }

    @Override
    public URL getFinalURL() {
        if (requiresPost()) {
            return super.onlineResource;
        }

        URL finalURL = strategy.buildUrlGET(this);
        return finalURL;
    }

    @Override
    public String getPostContentType() {
        return getOutputFormat();
    }

    @Override
    public void performPostOutput(OutputStream outputStream) throws IOException {

        InputStream in = strategy.getPostContents(this);
        try {
            IOUtils.copy(in, outputStream);
        } finally {
            in.close();
        }
    }

    @Override
    public WFSResponse createResponse(HTTPResponse response) throws IOException {

        final String contentType = response.getContentType();

        WFSResponseFactory responseFactory = WFSExtensions.findResponseFactory(this, contentType);

        WFSResponse wfsResponse = responseFactory.createResponse(this, response);

        return wfsResponse;
    }

}