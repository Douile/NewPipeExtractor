package org.schabi.newpipe.extractor.services.invidious;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import jdk.internal.joptsimple.internal.Strings;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.invidious.extractors.InvidiousChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.services.invidious.extractors.InvidiousPlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.invidious.extractors.InvidiousStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

public class InvidiousParsingHelper {
    private InvidiousParsingHelper() {}

    public static void validate(final JsonObject json) throws ContentNotAvailableException {
        final String error = json.getString("error");
        if (!Utils.isBlank(error)) {
            throw new ContentNotAvailableException(error);
        }
    }

    public static OffsetDateTime parseDateFrom(long unixTime) {
        return Instant.ofEpochMilli(unixTime).atZone(ZoneOffset.UTC).toOffsetDateTime();
    }

    public static Map<String, List<String>> parseQueryParams(final URL url) {
        return parseQueryParams(url.getQuery());
    }

    // from: https://stackoverflow.com/a/13592567
    public static Map<String, List<String>> parseQueryParams(String queryParams) {
       if (Strings.isNullOrEmpty(queryParams)) {
           return Collections.emptyMap();
       }

       return Arrays.stream(queryParams.split("&"))
               .map((String it) -> {
                   final int idx = it.indexOf("=");
                   final String key = idx > 0 ? it.substring(0, idx) : it;
                   final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
                   try {
                       return new AbstractMap.SimpleImmutableEntry<>(
                               URLDecoder.decode(key, UTF_8),
                               URLDecoder.decode(value, UTF_8)
                       );
                   } catch (UnsupportedEncodingException e) {
                       return new AbstractMap.SimpleImmutableEntry<>(
                                    "",
                                    ""
                               );
                   }
               })
               .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue, toList())));
    }

    public static String serializeQueryParams(final Map<String, List<String>> queryParams)
    throws UnsupportedEncodingException {
        if (queryParams.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder("?");
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            final String key = entry.getKey();
            for (final String value : entry.getValue()) {
                if (!Strings.isNullOrEmpty(value)) {
                    result.append(key);
                    result.append('=');
                    result.append(URLEncoder.encode(value, UTF_8));
                }
            }
        }
        return result.toString();
    }

    public static Page getNextPage(final String prevPageUrl)
    throws MalformedURLException, UnsupportedEncodingException, URISyntaxException {
        final URL prevUrl = Utils.stringToURL(prevPageUrl);

        // Pages in invidious are 1-based
        final Map<String, List<String>> params = parseQueryParams(prevUrl);
        if (params.containsKey("page")) {
            int page = 1;
            for (final String value : params.get("page")) {
                try {
                    page = Integer.parseInt(value);
                    break;
                } catch (NumberFormatException e) {
                    // IGNORE
                }
            }
            params.put("page", Collections.singletonList(Integer.toString(++page)));
        } else {
            params.put("page", Collections.singletonList("2"));
        }

        URI uri = new URI(prevUrl.getProtocol(), null, prevUrl.getHost(), prevUrl.getPort(), prevUrl.getPath(), serializeQueryParams(params), null);
        return new Page(uri.toString());
    }

    public static Page getNextContinuationPage(final String prevPageUrl, @Nullable final String continuation)
        throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        final URL prevUrl = Utils.stringToURL(prevPageUrl);

        final Map<String, List<String>> params = parseQueryParams(prevUrl);
        if (!Strings.isNullOrEmpty(continuation)) {
            params.put("continuation", Collections.singletonList(continuation));
        } else {
            params.remove("continuation");
        }

        URI uri = new URI(prevUrl.getProtocol(), null, prevUrl.getHost(), prevUrl.getPort(), prevUrl.getPath(), serializeQueryParams(params), null);
        return new Page(uri.toString());
    }


    public static void collectInfoItemsFrom(final InfoItemsCollector collector, final JsonArray json, final String baseUrl)
    throws ParsingException {
        for (final Object c : json) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;

                final String type = JsonUtils.getString(item, "type");

                switch (type) {
                    case "video": {
                        final InvidiousStreamInfoItemExtractor extractor = new InvidiousStreamInfoItemExtractor(item, baseUrl);
                        collector.commit(extractor);
                        break;
                    }
                    case "playlist": {
                        final InvidiousPlaylistInfoItemExtractor extractor = new InvidiousPlaylistInfoItemExtractor(item, baseUrl);
                        collector.commit(extractor);
                        break;
                    }
                    case "channel": {
                        final InvidiousChannelInfoItemExtractor extractor = new InvidiousChannelInfoItemExtractor(item, baseUrl);
                        collector.commit(extractor);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    public static void collectStreamsFrom(final StreamInfoItemsCollector collector, final JsonArray json, final String baseUrl)
    throws ParsingException {
        for (final Object c : json) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                final InvidiousStreamInfoItemExtractor extractor = new InvidiousStreamInfoItemExtractor(item, baseUrl);
                collector.commit(extractor);
            }
        }
    }
}
