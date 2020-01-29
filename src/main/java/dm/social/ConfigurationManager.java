package dm.social;

import dm.social.model.Post;
import dm.social.model.Subscription;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.*;

public class ConfigurationManager {
    public static final String POSTS_CACHE = "posts";
    public static final String SUBSCRIPTIONS_CACHE = "subscriptions";

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

    public CacheConfiguration<Subscription, Boolean> subscriptionsCacheConfiguration() {
        CacheConfiguration<Subscription, Boolean> cacheCfg = new CacheConfiguration<>(SUBSCRIPTIONS_CACHE);
        cacheCfg.setBackups(1);

        QueryEntity qe = new QueryEntity(Subscription.class, Boolean.class);

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("accountId", String.class.getName());
        fields.put("subscriberId", String.class.getName());
        fields.put("active", Boolean.class.getName());

        Set<String> keyFields = new HashSet<>();
        keyFields.add("accountId");
        keyFields.add("subscriberId");

        qe.setFields(fields);
        qe.setKeyFields(keyFields);
        qe.setValueFieldName("active");
        qe.setTableName("subscriptions");

        cacheCfg.setQueryEntities(Collections.singleton(qe));
        cacheCfg.setSqlSchema(SQL_SCHEMA);

        return cacheCfg;
    }
}
