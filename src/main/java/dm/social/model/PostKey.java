package dm.social.model;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.UUID;

public class PostKey {
    @QuerySqlField(index = true)
    private UUID postId;

    @AffinityKeyMapped
    @QuerySqlField(index = true)
    private String userId;

    public PostKey(UUID postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public UUID postId() {
        return postId;
    }

    public String userId() {
        return userId;
    }
}
