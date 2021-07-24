package org.schabi.newpipe.extractor.services.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory;

public class InvidiousCommentsLinkHandlerFactory extends YoutubeCommentsLinkHandlerFactory {

    private final static InvidiousCommentsLinkHandlerFactory instance = new InvidiousCommentsLinkHandlerFactory();

    public static InvidiousCommentsLinkHandlerFactory getInstance() { return instance; }

    @Override
    public String getUrl(String id) { return "https://redirect.invidious.io/watch?v=" + id; }
}
