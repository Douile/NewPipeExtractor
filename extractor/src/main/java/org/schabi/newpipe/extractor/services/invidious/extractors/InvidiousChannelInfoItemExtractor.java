package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JsonUtils;

public class InvidiousChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    private final JsonObject item;
    private String baseUrl;

    public InvidiousChannelInfoItemExtractor(final JsonObject item, final String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getDescription() throws ParsingException {
        return JsonUtils.getString(item, "description");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return JsonUtils.getNumber(item, "subCount").longValue();
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return JsonUtils.getNumber(item, "videoCount").longValue();
    }

    @Override
    public boolean isVerified() { return false; }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }

    @Override
    public String getUrl() throws ParsingException {
        return JsonUtils.getString(item, "authorUrl");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        final JsonArray thumbnails = JsonUtils.getArray(item, "authorThumbnails");

        if (thumbnails.has(0)) {
            return JsonUtils.getString(thumbnails.getObject(0), "url");
        }
        throw new ParsingException("Channel has no thumbnails");
    }
}
