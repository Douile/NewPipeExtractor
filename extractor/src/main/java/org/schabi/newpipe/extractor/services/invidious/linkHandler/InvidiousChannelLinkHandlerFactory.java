package org.schabi.newpipe.extractor.services.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;

import java.util.List;

public class InvidiousChannelLinkHandlerFactory extends YoutubeChannelLinkHandlerFactory {

    private static final InvidiousChannelLinkHandlerFactory instance = new InvidiousChannelLinkHandlerFactory();

    public static final String API_ENDPOINT = "/api/v1/channels/";

    public static InvidiousChannelLinkHandlerFactory getInstance() { return instance; }

    @Override
    public String getUrl(String id, List<String> contentFilters, String searchFilter) {
        return "https://redirect.invidious.io/channel/" + id;
    }
}
