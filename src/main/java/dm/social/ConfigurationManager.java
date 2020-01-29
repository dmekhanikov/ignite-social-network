package dm.social;

import dm.social.model.Post;
import dm.social.model.User;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Collections;
import java.util.UUID;

public class ConfigurationManager {
    public static final String USERS_CACHE = "users";
    public static final String USER_NAMES_CACHE = "user_names";
    public static final String POSTS_CACHE = "posts";

    public static final String SQL_SCHEMA = "PUBLIC";

    private static final ConfigurationManager INSTANCE = new ConfigurationManager();

    public static ConfigurationManager instance() {
        return INSTANCE;
    }

    public CacheConfiguration<UUID, User> usersCacheConfiguration() {
        CacheConfiguration<UUID, User> cacheCfg = new CacheConfiguration<>(USERS_CACHE);
        cacheCfg.setBackups(1);

        QueryEntity qe = new QueryEntity(UUID.class, User.class);
        qe.setKeyFieldName("userId");
        qe.setTableName("users");
        cacheCfg.setQueryEntities(Collections.singleton(qe));
        cacheCfg.setSqlSchema(SQL_SCHEMA);

        return cacheCfg;
    }

    public CacheConfiguration<String, UUID> userNamesCacheConfiguration() {
        CacheConfiguration<String, UUID> cacheCfg = new CacheConfiguration<>(USER_NAMES_CACHE);
        cacheCfg.setBackups(1);

        return cacheCfg;
    }

    public CacheConfiguration<UUID, Post> postsCacheConfiguration() {
        CacheConfiguration<UUID, Post> cacheCfg = new CacheConfiguration<>(POSTS_CACHE);
        cacheCfg.setBackups(1);

        QueryEntity qe = new QueryEntity(UUID.class, Post.class);
        qe.setKeyFieldName("postId");
        qe.setTableName("posts");
        cacheCfg.setQueryEntities(Collections.singleton(qe));
        cacheCfg.setSqlSchema(SQL_SCHEMA);

        return cacheCfg;
    }
}
