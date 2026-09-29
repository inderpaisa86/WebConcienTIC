package com.concientic.catalog.ingestion;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public final class UrlSafetyValidator {
    private UrlSafetyValidator() {
    }

    public static void validate(URI uri) {
        validate(uri, false);
    }

    static void validate(URI uri, boolean allowLocalAddresses) {
        if (uri == null || uri.getHost() == null || (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())))) {
            throw new IllegalArgumentException("Only absolute HTTP(S) URLs are allowed");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("URLs with user information are not allowed");
        }
        if (allowLocalAddresses) {
            return;
        }
        String host = uri.getHost().toLowerCase();
        if (host.equals("localhost") || host.endsWith(".local") || host.equals("metadata.google.internal")) {
            throw new IllegalArgumentException("Local or metadata hosts are not allowed");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()) {
                    throw new IllegalArgumentException("Private network addresses are not allowed");
                }
            }
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("Host cannot be resolved", exception);
        }
    }
}
