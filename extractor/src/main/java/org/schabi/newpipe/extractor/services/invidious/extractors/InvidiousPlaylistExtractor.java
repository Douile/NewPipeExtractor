package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.invidious.linkHandler.InvidiousChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.invidious.linkHandler.InvidiousPlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper.collectStreamsFrom;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class InvidiousPlaylistExtractor extends PlaylistExtractor {
    private JsonObject item;

    public InvidiousPlaylistExtractor(final StreamingService service, final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return "";
    }

    @Override
    public String getBannerUrl() { return null; }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String id = JsonUtils.getString(item, "authorId");
        return ServiceList.Invidious.getChannelLHFactory().getUrl(id, getBaseUrl());
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        final JsonArray thumbnails = JsonUtils.getArray(item, "authorThumbnails");
        if (thumbnails.has(0)) {
            return JsonUtils.getString(thumbnails.getObject(0), "url");
        }
        return null;
    }

    @Override
    public boolean isUploaderVerified() { return false; }

    @Override
    public long getStreamCount() throws ParsingException {
        return JsonUtils.getNumber(item, "videoCount").longValue();
    }

    @Nonnull
    @Override
    public String getSubChannelName() { return ""; }

    @Nonnull
    @Override
    public String getSubChannelUrl() { return ""; }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() { return ""; }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "title");
    }

    /*//////////////////////////////////////////////////////////////////
    /// Fetch page
    ///////////////////////////////////////////////////////////////////*/

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage()
        throws IOException, ExtractionException {
        return getPage(new Page(getUrl()));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
        throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (JsonParserException e) {
                throw new ParsingException("Could not parse json data for playlist info", e);
            }
        }

        if (json != null) {
            InvidiousParsingHelper.validate(json);

            final JsonArray videos = JsonUtils.getArray(json, "videos");

            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
            collectStreamsFrom(collector, videos, getBaseUrl());

            Page nextPage = null;
            // Invidious will return empty video list if there are no more streams to
            // fetch. We can't count how many are left as the pages returned per
            // result is defined per-instance and not shown
            if (!videos.isEmpty()) {
                try {
                    nextPage = InvidiousParsingHelper.getNextPage(page.getUrl());
                } catch(Exception e) {
                    // DO NOTHING
                }
            }

            return new InfoItemsPage<>(collector, nextPage);
        } else {
            throw new ExtractionException("Unable to get Invidious playlist info");
        }
    }

    @Override
    public void onFetchPage(final Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(getBaseUrl() + InvidiousPlaylistLinkHandlerFactory.API_ENDPOINT + getId());
        if (response != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to access Invidious channel data");
        }
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        JsonObject json = null;
        if (!Utils.isNullOrEmpty(responseBody)) {
            try {
                json = JsonParser.object().from(responseBody);
            } catch (JsonParserException e) {
                throw new ParsingException("Could not parse json data for playlist info", e);
            }
        }

        if (json != null) {
            InvidiousParsingHelper.validate(json);
            this.item = json;
        } else {
            throw new ParsingException("Unable to get Invidious playlist info");
        }
    }
}
