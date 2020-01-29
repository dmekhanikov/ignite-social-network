package dm.social.model;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.UUID;

public class Post {
    @QuerySqlField
    private UUID postId;

    @AffinityKeyMapped
    @QuerySqlField
    private UUID userId;

    @QuerySqlField
    private String text;

    @QuerySqlField(index = true)
    private long createdUTCTimestamp;

    public Post(UUID postId, UUID userId, String text, long createdUTCTimestamp) {
        this.postId = postId;
        this.userId = userId;
        this.text = text;
        this.createdUTCTimestamp = createdUTCTimestamp;
    }

    public UUID postId() {
        return postId;
    }

    public UUID userId() {
        return userId;
    }

    public String text() {
        return text;
    }

    public long createdUTCTimestamp() {
        return createdUTCTimestamp;
    }
}
