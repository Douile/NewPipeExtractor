package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;

public class InvidiousStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject item;
    private String baseUrl;

    public InvidiousStreamInfoItemExtractor(final JsonObject item, final String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getUrl() throws ParsingException {
        final String id = JsonUtils.getString(item, "videoId");
        return ServiceList.Invidious.getStreamLHFactory().fromId(id, baseUrl).getUrl();
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        final JsonArray thumbnails = JsonUtils.getArray(item, "videoThumbnails");
        if (thumbnails.has(0)) {
            return JsonUtils.getString(thumbnails.getObject(0), "url");
        }
        throw new ParsingException("No thumbnails exist for this stream info item");
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "title");
    }

    @Override
    public boolean isAd() { return false; }

    @Override
    public long getViewCount() { return item.getLong("viewCount"); }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return JsonUtils.getString(item, "authorUrl");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(item, "publishedText");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(InvidiousParsingHelper.parseDateFrom(item.getLong("published")));
    }

    @Override
    public StreamType getStreamType() {
        return item.getBoolean("liveNow") ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() {
        return item.getLong("lengthSeconds") * 1000;
    }

    protected void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
