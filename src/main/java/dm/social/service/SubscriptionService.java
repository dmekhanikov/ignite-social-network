package dm.social.service;

import dm.social.ConfigurationManager;
import dm.social.model.Subscription;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionService {
    private final Ignite ignite;

    public SubscriptionService(Ignite ignite) {
        this.ignite = ignite;
        ignite.getOrCreateCache(ConfigurationManager.instance().postsCacheConfiguration());
    }

    public Subscription createSubscription(String subscriber, String account) {
        Subscription subscription = new Subscription(subscriber, account);

        IgniteCache<Subscription, Boolean> subscriptionsCache =
                ignite.getOrCreateCache(ConfigurationManager.instance().subscriptionsCacheConfiguration());

        subscriptionsCache.put(subscription, true);

        return subscription;
    }

    public void removeSubscription(String subscriber, String account) {
        Subscription subscription = new Subscription(subscriber, account);

        IgniteCache<Subscription, Boolean> subscriptionsCache =
                ignite.getOrCreateCache(ConfigurationManager.instance().subscriptionsCacheConfiguration());

        subscriptionsCache.remove(subscription);
    }

    public List<String> getSubscriptions(String subscriberId) {
        return ignite.compute().affinityCall(ConfigurationManager.SUBSCRIPTIONS_CACHE,
                subscriberId, new FetchSubscriptionsJob(subscriberId));
    }

    public static class FetchSubscriptionsJob implements IgniteCallable<List<String>> {
        private final String subscriberId;

        @IgniteInstanceResource
        private Ignite ignite;

        public FetchSubscriptionsJob(String subscriberId) {
            this.subscriberId = subscriberId;
        }

        @Override
        public List<String> call() throws Exception {
            Query<List<?>> q =
                    new SqlFieldsQuery("SELECT accountId FROM subscriptions WHERE subscriberId='" + subscriberId + "'");

            q.setLocal(true);

            IgniteCache<Subscription, Boolean> cache =
                    ignite.getOrCreateCache(ConfigurationManager.instance().subscriptionsCacheConfiguration());
            List<List<?>> rows = cache.query(q).getAll();
            List<String> result = new ArrayList<>();

            for (List<?> row : rows) {
                result.add((String) row.get(0));
            }

            return result;
        }
    }
}
