package dm.social.model;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.Date;
import java.util.UUID;

public class Post {
    @QuerySqlField
    private UUID postId;

    @AffinityKeyMapped
    @QuerySqlField(index = true)
    private String userId;

    @QuerySqlField
    private String text;

    @QuerySqlField(index = true)
    private long createdUTCTimestamp;

    public Post(UUID postId, String userId, String text, long createdUTCTimestamp) {
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.createdUTCTimestamp = createdUTCTimestamp;
    }

    public UUID postId() {
        return postId;
    }

    public String userId() {
        return userId;
    }

    public String text() {
        return text;
    }

    public long createdUTCTimestamp() {
        return createdUTCTimestamp;
    }

    @Override
    public String toString() {
        Date date = new Date(createdUTCTimestamp);

        return "[" + date + "] " + userId + ": " + text;
    }
}
