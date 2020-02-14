package dm.social;

import dm.social.model.PostContent;
import dm.social.model.PostKey;
import dm.social.model.Subscription;
import org.apache.ignite.cache.CacheKeyConfiguration;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class ConfigurationManager {
    public static final String POSTS_CACHE = "posts";
    public static final String SUBSCRIPTIONS_CACHE = "subscriptions";

    public static final String SQL_SCHEMA = "PUBLIC";

    private static final ConfigurationManager INSTANCE = new ConfigurationManager();

    public static ConfigurationManager instance() {
        return INSTANCE;
    }

    public CacheConfiguration<PostKey, PostContent> postsCacheConfiguration() {
        CacheConfiguration<PostKey, PostContent> cacheCfg = new CacheConfiguration<>(POSTS_CACHE);
        cacheCfg.setBackups(1);

        QueryEntity qe = new QueryEntity(PostKey.class, PostContent.class);
        qe.setTableName(POSTS_CACHE);
        cacheCfg.setQueryEntities(Collections.singleton(qe));
        cacheCfg.setSqlSchema(SQL_SCHEMA);

        cacheCfg.setKeyConfiguration(keyConfiguration(PostKey.class, "userId"));

        return cacheCfg;
    }

    public CacheConfiguration<Subscription, Boolean> subscriptionsCacheConfiguration() {
        CacheConfiguration<Subscription, Boolean> cacheCfg = new CacheConfiguration<>(SUBSCRIPTIONS_CACHE);
        cacheCfg.setBackups(1);

        QueryEntity qe = new QueryEntity(Subscription.class, Boolean.class);

        // Make the cache value one of the columns.
        // This is required since an SQL table must have at least one column in the value.
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

        qe.setTableName(SUBSCRIPTIONS_CACHE);

        cacheCfg.setQueryEntities(Collections.singleton(qe));
        cacheCfg.setSqlSchema(SQL_SCHEMA);

        cacheCfg.setKeyConfiguration(keyConfiguration(Subscription.class, "subscriberId"));

        return cacheCfg;
    }

    private CacheKeyConfiguration keyConfiguration(Class<?> keyClass, String affinityFieldName) {
        // Workaround for https://issues.apache.org/jira/browse/IGNITE-5795
        CacheKeyConfiguration keyConfig = new CacheKeyConfiguration();
        keyConfig.setTypeName(keyClass.getName());
        keyConfig.setAffinityKeyFieldName(affinityFieldName);

        return keyConfig;
    }
}
