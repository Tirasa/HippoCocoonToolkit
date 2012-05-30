package org.onehippo.forge.hct.cocoon.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class ClasspathURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * {@inheritDoc}
     *
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
        return "classpath".equalsIgnoreCase(protocol)
                ? new ClasspathURLStreamHandler()
                : null;
    }

    public class ClasspathURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(final URL url)
                throws IOException {

            final URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(url.getPath());

            return resourceUrl.openConnection();
        }
    }
}
