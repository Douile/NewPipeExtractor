package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Info;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.invidious.linkHandler.InvidiousChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper.collectStreamsFrom;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class InvidiousChannelExtractor extends ChannelExtractor {
    private JsonObject item;
    private String baseUrl;

    public InvidiousChannelExtractor(final StreamingService service, final ListLinkHandler linkHandler)
        throws ParsingException {
        super(service, linkHandler);
        this.baseUrl = linkHandler.getBaseUrl();
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        final JsonArray thumbnails = JsonUtils.getArray(item, "authorThumbnails");

        if (thumbnails.has(0)) {
            return JsonUtils.getString(thumbnails.getObject(0), "url");
        }
        return null;
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        final JsonArray banners = JsonUtils.getArray(item, "authorBanners");

        if (banners.has(0)) {
            return JsonUtils.getString(banners.getObject(0), "url");
        }
        return null;
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return null;
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return JsonUtils.getNumber(item, "subCount").longValue();
    }

    @Override
    public String getDescription() {
        try {
            return JsonUtils.getString(item, "description");
        } catch(ParsingException e) {
            return "No description";
        }
    }

    @Override
    public String getParentChannelName() { return ""; }

    @Override
    public String getParentChannelUrl() { return ""; }

    @Override
    public String getParentChannelAvatarUrl() { return ""; }

    @Override
    public boolean isVerified() { return false; }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }


    /*//////////////////////////////////////////////////////////////////
    /// Fetch page
    ///////////////////////////////////////////////////////////////////*/

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(new Page(baseUrl + InvidiousChannelLinkHandlerFactory.API_ENDPOINT + getId() + "/videos"));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
    throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonArray json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.array().from(response.responseBody());
            } catch(JsonParserException e) {
                throw new ParsingException("Could not parse json data for channel videos", e);
            }
        }

        if (json != null) {
             final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
             collectStreamsFrom(collector, json, baseUrl);

             try {
                 return new InfoItemsPage<>(collector, InvidiousParsingHelper.getNextPage(page.getUrl()));
             } catch (Exception e) {
                 throw new ExtractionException("Unable to parse next page", e);
             }
        } else {
            throw new ExtractionException("Unable to get Invidious channel info");
        }
    }

    @Override
    public void onFetchPage(final Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(baseUrl + InvidiousChannelLinkHandlerFactory.API_ENDPOINT + getId());
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
                throw new ParsingException("Could not parse json data for channel info", e);
            }
        }

        if (json != null) {
            InvidiousParsingHelper.validate(json);
            this.item = json;
        } else {
            throw new ParsingException("Unable to get Invidious channel info");
        }
    }
}
