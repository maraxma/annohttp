package org.mosin.annohttp.spring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

@ConfigurationProperties(prefix = AnnoHttpProperties.NAMESPACE)
public class AnnoHttpProperties {

    public static final String NAMESPACE = "annohttp";

    private int connectTimeoutInSeconds = 10;
    private int socketTimeoutInSeconds = 10;
    private int connectionIdleTimeoutInSeconds = 20;
    private int maxConnections = 80;
    private int maxConnectionsPerRoute = 40;
    private boolean keepAlive = true;
    private int keepAliveTimeInSeconds = 30;
    private boolean flowRedirect = true;

    private boolean trustAnySsl = true;

    private String[] serviceBasePackages = new String[] {"./"};

    public int getConnectTimeoutInSeconds() {
        return connectTimeoutInSeconds;
    }

    public void setConnectTimeoutInSeconds(int connectTimeoutInSeconds) {
        this.connectTimeoutInSeconds = connectTimeoutInSeconds;
    }

    public int getSocketTimeoutInSeconds() {
        return socketTimeoutInSeconds;
    }

    public void setSocketTimeoutInSeconds(int socketTimeoutInSeconds) {
        this.socketTimeoutInSeconds = socketTimeoutInSeconds;
    }

    public int getConnectionIdleTimeoutInSeconds() {
        return connectionIdleTimeoutInSeconds;
    }

    public void setConnectionIdleTimeoutInSeconds(int connectionIdleTimeoutInSeconds) {
        this.connectionIdleTimeoutInSeconds = connectionIdleTimeoutInSeconds;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getKeepAliveTimeInSeconds() {
        return keepAliveTimeInSeconds;
    }

    public void setKeepAliveTimeInSeconds(int keepAliveTimeInSeconds) {
        this.keepAliveTimeInSeconds = keepAliveTimeInSeconds;
    }

    public boolean isFlowRedirect() {
        return flowRedirect;
    }

    public void setFlowRedirect(boolean flowRedirect) {
        this.flowRedirect = flowRedirect;
    }

    public boolean isTrustAnySsl() {
        return trustAnySsl;
    }

    public void setTrustAnySsl(boolean trustAnySsl) {
        this.trustAnySsl = trustAnySsl;
    }

    public String[] getServiceBasePackages() {
        return serviceBasePackages;
    }

    public void setServiceBasePackages(String[] serviceBasePackages) {
        this.serviceBasePackages = serviceBasePackages;
    }

    @Override
    public String toString() {
        return "AnnoHttpProperties{" +
                "connectTimeoutInSeconds=" + connectTimeoutInSeconds +
                ", socketTimeoutInSeconds=" + socketTimeoutInSeconds +
                ", connectionIdleTimeoutInSeconds=" + connectionIdleTimeoutInSeconds +
                ", maxConnections=" + maxConnections +
                ", maxConnectionsPerRoute=" + maxConnectionsPerRoute +
                ", keepAlive=" + keepAlive +
                ", keepAliveTimeInSeconds=" + keepAliveTimeInSeconds +
                ", flowRedirect=" + flowRedirect +
                ", trustAnySsl=" + trustAnySsl +
                ", serviceBasePackages=" + Arrays.toString(serviceBasePackages) +
                '}';
    }
}
