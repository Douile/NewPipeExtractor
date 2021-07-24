package org.schabi.newpipe.extractor.services.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

public class InvidiousSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String ALL = "all";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";
    public static final String MOVIES = "movies";
    public static final String SHOWS = "shows";

    public static final String RELEVANCE = "relevance";
    public static final String RATING = "rating";
    public static final String UPLOAD_DATE = "upload date";
    public static final String VIEW_COUNT = "view count";

    private static final String SEARCH_URL = "https://redirect.invidious.io/search?q=";

    private final static InvidiousSearchQueryHandlerFactory instance = new InvidiousSearchQueryHandlerFactory();

    public static InvidiousSearchQueryHandlerFactory getInstance() { return instance; }

    @Override
    public String getUrl(final String searchString, List<String> contentFilters, String sortFilter) throws ParsingException {
        try {
            StringBuilder additionalQueries = new StringBuilder(searchString);
            if (!contentFilters.isEmpty()) {
                switch (contentFilters.get(0)) {
                    case ALL:
                    default:
                        break;
                    case VIDEOS:
                        additionalQueries.append(" content_type:video");
                        break;
                    case CHANNELS:
                        additionalQueries.append(" content_type:channel");
                        break;
                    case PLAYLISTS:
                        additionalQueries.append(" content_type:playlist");
                        break;
                    case MOVIES:
                        additionalQueries.append(" content_type:movies");
                        break;
                    case SHOWS:
                        additionalQueries.append(" content_type:shows");
                        break;
                }
            }

            switch(sortFilter) {
                default:
                    break;
                case RELEVANCE:
                    additionalQueries.append(" sort:relevance");
                    break;
                case RATING:
                    additionalQueries.append(" sort:rating");
                    break;
                case UPLOAD_DATE:
                    additionalQueries.append(" sort:date");
                    break;
                case VIEW_COUNT:
                    additionalQueries.append(" sort:views");
                    break;
            }

            return SEARCH_URL + URLEncoder.encode(additionalQueries.toString(), UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                VIDEOS,
                CHANNELS,
                PLAYLISTS,
                MOVIES,
                SHOWS,
        };
    }

    @Override
    public String[] getAvailableSortFilter() {
        return new String[] {
            RELEVANCE,
            RATING,
            UPLOAD_DATE,
            VIEW_COUNT,
        };
    }
}
