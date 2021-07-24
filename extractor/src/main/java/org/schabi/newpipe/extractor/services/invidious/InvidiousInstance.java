package org.schabi.newpipe.extractor.services.invidious;

import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;

public class InvidiousInstance {
    private final String url;
    private String name;
    private boolean proxy = false;

    public final static InvidiousInstance defaultInstance = new InvidiousInstance("https://invidious.snopyta.org/");

    public InvidiousInstance(String urlString) {
        this.url = urlString;
        URL url = null;
        try {
            url = Utils.stringToURL(urlString);
        } catch (MalformedURLException e) {
            // do nothing
        }
        this.name = url == null ? "invidious" : url.getHost();
    }

    public InvidiousInstance(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public boolean isProxy() { return proxy; }
    public void setProxy(boolean proxy) { this.proxy = proxy; }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
}
