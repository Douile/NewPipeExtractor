package org.schabi.newpipe.extractor.services.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;

public class InvidiousStreamLinkHandlerFactory extends YoutubeStreamLinkHandlerFactory {

    public static final String VIDEO_API_ENDPOINT = "/api/v1/videos/";

    private static final InvidiousStreamLinkHandlerFactory instance = new InvidiousStreamLinkHandlerFactory();

    public static InvidiousStreamLinkHandlerFactory getInstance() { return instance; }

    // TODO: Use instance url
    @Override
    public String getUrl(final String id) { return "https://redirect.invidious.io/watch?v=" + id; }
}
