package org.schabi.newpipe.extractor.services.invidious;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.*;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.invidious.extractors.*;
import org.schabi.newpipe.extractor.services.invidious.linkHandler.*;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import static java.util.Arrays.asList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.*;

public class InvidiousService extends StreamingService {

    private InvidiousInstance instance;

    public InvidiousService(int id) { this(id, InvidiousInstance.defaultInstance); }

    public InvidiousService(int id, InvidiousInstance instance) {
        super(id, "Invidious", asList(AUDIO, VIDEO, LIVE, COMMENTS));
        this.instance = instance;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() { return InvidiousStreamLinkHandlerFactory.getInstance(); }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() { return InvidiousChannelLinkHandlerFactory.getInstance(); }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() { return InvidiousPlaylistLinkHandlerFactory.getInstance(); }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() { return InvidiousSearchQueryHandlerFactory.getInstance(); }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() { return InvidiousCommentsLinkHandlerFactory.getInstance(); }

    @Override
    public SearchExtractor getSearchExtractor(SearchQueryHandler queryHandler) {
        return new InvidiousSearchExtractor(this, queryHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new InvidiousSuggestionExtractor(this);
    }

    @Override
    public ChannelExtractor getChannelExtractor(ListLinkHandler linkHandler) throws ParsingException {
        return new InvidiousChannelExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(ListLinkHandler linkHandler) {
        return new InvidiousPlaylistExtractor(this, linkHandler);
    }

    @Override
    public StreamExtractor getStreamExtractor(LinkHandler linkHandler) throws ExtractionException {
        return new InvidiousStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(ListLinkHandler linkHandler) {
        return new InvidiousCommentsExtractor(this, linkHandler);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return null;
    }

    @Override
    public KioskList getKioskList() {
        KioskList list = new KioskList(this);

        // TODO: Add trending and popular

        return list;
    }

    @Override
    public String getBaseUrl() { return instance.getUrl(); }

    public InvidiousInstance getInstance() { return this.instance; }

    public void setInstance(InvidiousInstance instance) { this.instance = instance; }
}
