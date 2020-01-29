package dm.social;

import dm.social.model.Post;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Collections;
import java.util.UUID;

public class ConfigurationManager {
    public static final String POSTS_CACHE = "posts";

    public static final String SQL_SCHEMA = "PUBLIC";

    private static final ConfigurationManager INSTANCE = new ConfigurationManager();

    public static ConfigurationManager instance() {
        return INSTANCE;
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
