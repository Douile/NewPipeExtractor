package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

public class InvidiousSuggestionExtractor extends SuggestionExtractor {

    public InvidiousSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(String query) throws IOException, ExtractionException {
        final Downloader dl = NewPipe.getDownloader();
        final List<String> suggestions = new ArrayList<>();

        final String baseUrl = ServiceList.Invidious.getBaseUrl();

        final String url = baseUrl + "/api/v1/suggestions?q=" + URLEncoder.encode(query, UTF_8);

        final Response response = dl.get(url);

        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch(JsonParserException e) {
                throw new ParsingException("Could not parse Invidious suggestion response", e);
            }
        }

        if (json != null) {
            InvidiousParsingHelper.validate(json);

            final JsonArray jsonSuggestions = JsonUtils.getArray(json, "suggestions");

            for (final Object suggestion : jsonSuggestions) {
                if (suggestion instanceof String) {
                    suggestions.add((String) suggestion);
                }
            }

            return suggestions;
        } else {
            throw new ParsingException("Could not get Invidious suggestions");
        }
    }
}
