package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.invidious.linkHandler.InvidiousStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class InvidiousStreamExtractor extends StreamExtractor {
    private final String baseUrl;
    private JsonObject json;

    public InvidiousStreamExtractor(final StreamingService service, final LinkHandler linkHandler) throws ParsingException {
        super(service, linkHandler);
        this.baseUrl = getBaseUrl();
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(json, "title");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(json, "publishedText");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final Number published = JsonUtils.getNumber(json, "published");

        return new DateWrapper(
                InvidiousParsingHelper.parseDateFrom(published.longValue())
        );
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        JsonArray thumbnails = JsonUtils.getArray(json, "videoThumbnails");
        if (thumbnails.has(0)) {
            return JsonUtils.getString(thumbnails.getObject(0), "url");
        }
        throw new ParsingException("Could not get thumbnail URL");
    }

    @Nonnull
    @Override
    public Description getDescription() {
        String text;
        try {
            text = JsonUtils.getString(json, "descriptionHtml");
        } catch(ParsingException e) {
            return Description.emptyDescription;
        }

        return new Description(text, Description.HTML);
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        final boolean isFamilyFriendly = JsonUtils.getBoolean(json, "isFamilyFriendly");
        if (!isFamilyFriendly) {
            return 18;
        } else {
            return NO_AGE_LIMIT;
        }
    }

    @Override
    public long getLength() { return json.getLong("lengthSeconds") * 1000; }

    @Override
    public long getTimeStamp() { return 0; }

    @Override
    public long getViewCount() { return json.getLong("viewCount"); }

    @Override
    public long getLikeCount() { return json.getLong("likeCount"); }

    @Override
    public long getDislikeCount() { return json.getLong("dislikeCount"); }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return JsonUtils.getString(json, "authorUrl");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(json, "author");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        final JsonArray avatars = JsonUtils.getArray(json, "authorThumbnails");

        if (avatars.has(0)) {
            return JsonUtils.getString(avatars.getObject(0), "url");
        }

        throw new ParsingException("Could not get uploader avatar URL");
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        return JsonUtils.getString(json, "dashUrl");
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return JsonUtils.getString(json, "hlsUrl");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();

        // TODO

        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();

        // TODO

        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws ExtractionException {
        final List<VideoStream> videoOnlyStreams = new ArrayList<>();

        // TODO

        return videoOnlyStreams;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() throws ParsingException {
        // TODO

        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) throws ParsingException {
        // TODO

        return new ArrayList<>();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return JsonUtils.getBoolean(json, "isLive") ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedItems() throws ExtractionException {
        // TODO

        return null;
    }

    @Override
    public String getErrorMessage() {
        try {
            return JsonUtils.getString(json, "error");
        } catch(final ParsingException e) {
            return null; // no error message
        }
    }

    @Nonnull
    @Override
    public String getHost() {
        return baseUrl;
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() throws ParsingException {
        return JsonUtils.getBoolean(json, "isListed") ? Privacy.PUBLIC : Privacy.UNLISTED;
    }

    @Nonnull
    @Override
    public String getCategory() throws ParsingException {
        return JsonUtils.getString(json, "genre");
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "YouTube licence";
    }

    @Override
    public Locale getLanguageInfo() { return null; }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(json.getArray("keywords"));
    }

    @Nonnull
    @Override
    public String getSupportInfo() { return ""; }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() {
        // TODO

        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() {
        return Collections.emptyList();
    }


    /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(baseUrl + InvidiousStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + getId());
        if (response != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract Invidious channel data");
        }
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            throw new ExtractionException("Unable to extract Invidious stream data", e);
        }
        if (json == null) {
            throw new ExtractionException("Unable to extract Invidious stream data");
        }
        InvidiousParsingHelper.validate(json);
    }
}
