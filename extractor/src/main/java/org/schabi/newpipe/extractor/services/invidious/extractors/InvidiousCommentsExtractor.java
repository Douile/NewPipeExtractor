package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class InvidiousCommentsExtractor extends CommentsExtractor {

    public InvidiousCommentsExtractor(
            final StreamingService service,
            final ListLinkHandler uiHandler
            ) {
        super(service, uiHandler);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
        throws IOException, ExtractionException {
        return getPage(new Page(getUrl()));
    }

    private void collectCommentsFrom(final CommentsInfoItemsCollector collector, final JsonObject json)
        throws ParsingException {
        final JsonArray comments = json.getArray("comments");

        for (final Object c : comments) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                final InvidiousCommentsInfoItemExtractor extractor = new InvidiousCommentsInfoItemExtractor(item, this);
                collector.commit(extractor);
            }
        }
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
        throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (Exception e) {
                throw new ParsingException("Could not parse json data for comments info", e);
            }
        }

        if (json != null) {
            InvidiousParsingHelper.validate(json);

            final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
            collectCommentsFrom(collector, json);

            Page nextPage = null;
            try {
                nextPage = InvidiousParsingHelper.getNextContinuationPage(page.getUrl(), json.getString("continuation"));
            } catch (URISyntaxException e) {
                // IGNORE
            }
            return new InfoItemsPage<>(collector, nextPage);
        } else {
            throw new ExtractionException("Unable to get Invidious comments info");
        }
    }

    @Override
    public void onFetchPage(Downloader downloader) { }
}
