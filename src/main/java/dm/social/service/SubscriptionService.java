package dm.social.service;

import dm.social.ConfigurationManager;
import dm.social.model.Subscription;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

public class SubscriptionService {
    private final Ignite ignite;

    public SubscriptionService(Ignite ignite) {
        this.ignite = ignite;
    }

    public Subscription createSubscription(String subscriber, String account) {
        Subscription subscription = new Subscription(subscriber, account);

        IgniteCache<Subscription, Boolean> subscriptionsCache =
                ignite.getOrCreateCache(ConfigurationManager.instance().subscriptionsCacheConfiguration());

        subscriptionsCache.put(subscription, true);

        return subscription;
    }
}
