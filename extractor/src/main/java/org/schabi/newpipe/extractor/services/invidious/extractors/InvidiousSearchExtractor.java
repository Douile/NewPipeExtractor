package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper.collectInfoItemsFrom;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class InvidiousSearchExtractor extends SearchExtractor {
    public InvidiousSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() {
        // TODO: Invidious does support search suggestions
        return "";
    }

    @Override
    public boolean isCorrectedSearch() { return false; }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() {
        return Collections.emptyList();
    }

    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(new Page(getUrl()));
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonArray json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.array().from(response.responseBody());
            } catch (Exception e) {
                throw new ParsingException("Could not parse json data for search info", e);
            }
        }

        if (json != null) {
            final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());
            collectInfoItemsFrom(collector, json, getBaseUrl());

            // TODO: Add helper to append page to query params
            return new InfoItemsPage<>(collector, null);
        } else {
            throw new ExtractionException("Unable to get Invidious search info");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException { }
}
