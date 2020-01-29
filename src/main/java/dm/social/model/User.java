package dm.social.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.util.UUID;

public class User {
    @QuerySqlField
    private UUID userId;
    @QuerySqlField
    private String name;

    public User(UUID userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public UUID userId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
