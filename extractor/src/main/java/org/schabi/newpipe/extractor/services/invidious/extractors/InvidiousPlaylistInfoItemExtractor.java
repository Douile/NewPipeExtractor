package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;

public class InvidiousPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {

    private final JsonObject item;
    private String baseUrl;

    public InvidiousPlaylistInfoItemExtractor(final JsonObject item, final String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        // Invidious doesn't return any thumbnail for playlist items
        return null;
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "title");
    }

    @Override
    public String getUrl() throws ParsingException {
        final String id = JsonUtils.getString(item, "playlistId");
        return ServiceList.Invidious.getPlaylistLHFactory().getUrl(id, baseUrl);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return JsonUtils.getNumber(item, "videoCount").longValue();
    }
}
