package dm.social.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class PostContent {
    @QuerySqlField
    private String text;

    @QuerySqlField(index = true)
    private long createdUTCTimestamp;

    public PostContent(String text, long createdUTCTimestamp) {
        this.text = text;
        this.createdUTCTimestamp = createdUTCTimestamp;
    }

    public String text() {
        return text;
    }

    public long createdUTCTimestamp() {
        return createdUTCTimestamp;
    }
}
