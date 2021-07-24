package org.schabi.newpipe.extractor.services.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;

import java.util.List;

public class InvidiousPlaylistLinkHandlerFactory extends YoutubePlaylistLinkHandlerFactory {

    private static final InvidiousPlaylistLinkHandlerFactory instance = new InvidiousPlaylistLinkHandlerFactory();

    public static final String API_ENDPOINT = "/api/v1/playlists/";

    public static InvidiousPlaylistLinkHandlerFactory getInstance() { return instance; }

    private InvidiousPlaylistLinkHandlerFactory() {}

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter) {
        return "https://redirect.invidious.io/playlist?list=" + id;
    }
}
