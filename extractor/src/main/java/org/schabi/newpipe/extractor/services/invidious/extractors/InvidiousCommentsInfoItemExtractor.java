package org.schabi.newpipe.extractor.services.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;

public class InvidiousCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    private final JsonObject item;
    private final String url;

    public InvidiousCommentsInfoItemExtractor(final JsonObject item, final InvidiousCommentsExtractor extractor)
    throws ParsingException{
        this.item = item;
        this.url = extractor.getUrl();
    }

    @Override
    public String getUrl() { return url; }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        final JsonArray thumbnails = item.getArray("authorThumbnails");
        if (thumbnails.has(0)) {
            return JsonUtils.getString(thumbnails.getObject(0), "url");
        } else {
            throw new ParsingException("Unable to find author thumbnail");
        }
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(item, "publishedText");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final Number published = JsonUtils.getNumber(item, "published");
        return new DateWrapper(InvidiousParsingHelper.parseDateFrom(published.longValue()));
    }

    @Override
    public String getCommentText() throws ParsingException {
        return JsonUtils.getString(item, "content");
    }

    @Override
    public String getCommentId() throws ParsingException {
        return JsonUtils.getString(item, "commentId");
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return getThumbnailUrl();
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "author");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String id = JsonUtils.getString(item, "authorId");
        return ServiceList.Invidious.getCommentsLHFactory().fromId(id).getUrl();
    }

}
