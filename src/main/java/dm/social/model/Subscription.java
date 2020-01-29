package dm.social.model;

import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class Subscription {
    @AffinityKeyMapped
    @QuerySqlField(index = true)
    private String subscriberId;

    @QuerySqlField
    private String accountId;

    public Subscription(String subscriberId, String accountId) {
        this.subscriberId = subscriberId;
        this.accountId = accountId;
    }

    public String subscriberId() {
        return subscriberId;
    }

    public String accountId() {
        return accountId;
    }
}
