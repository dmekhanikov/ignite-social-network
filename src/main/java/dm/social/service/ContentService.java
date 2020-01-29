package dm.social.service;

import dm.social.ConfigurationManager;
import dm.social.model.Post;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskAdapter;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

import java.util.*;

public class ContentService {
    private final Ignite ignite;
    private final SubscriptionService subscriptionService;

    public ContentService(Ignite ignite) {
        this.ignite = ignite;
        this.subscriptionService = new SubscriptionService(ignite);
        ignite.getOrCreateCache(ConfigurationManager.instance().postsCacheConfiguration());
    }

    public Post createPost(String userId, String text) {
        Post post = new Post(UUID.randomUUID(), userId, text, new Date().getTime());
        IgniteCache<UUID, Post> postsCache = postsCache(ignite);
        postsCache.put(post.postId(), post);

        return post;
    }

    public List<Post> fetchPosts(String userId, int limit) {
        return ignite.compute().affinityCall(ConfigurationManager.POSTS_CACHE,
                userId,
                new FetchContentJob(Collections.singletonList(userId), limit));
    }

    public List<Post> fetchFeed(String userId, int limit) {
        List<String> subscriptions = subscriptionService.getSubscriptions(userId);
        try {
            return ignite.compute().execute(new FetchContentTask(subscriptions, limit), null);
        } catch (IgniteException e) {
            return Collections.emptyList();
        }
    }

    private static IgniteCache<UUID, Post> postsCache(Ignite ignite) {
        CacheConfiguration<UUID, Post> cacheCfg = ConfigurationManager.instance().postsCacheConfiguration();
        return ignite.getOrCreateCache(cacheCfg);
    }

    public static class FetchContentTask extends ComputeTaskAdapter<Object, List<Post>> {
        @IgniteInstanceResource
        private Ignite ignite;

        private final List<String> userIds;

        private final int limit;

        public FetchContentTask(List<String> userIds, int limit) {
            this.userIds = userIds;
            this.limit = limit;
        }

        @Override
        public Map<FetchContentJob, ClusterNode> map(
                List<ClusterNode> subgrid, Object args) throws IgniteException {
            Map<ClusterNode, List<String>> subsByNode = new HashMap<>();
            Affinity<String> affinity = ignite.affinity(ConfigurationManager.POSTS_CACHE);

            for (String userId : userIds) {
                ClusterNode node = affinity.mapKeyToNode(userId);

                List<String> nodeReqs = subsByNode.computeIfAbsent(node, k -> new ArrayList<>());
                nodeReqs.add(userId);
            }

            if (subsByNode.isEmpty()) {
                return null;
            }

            Map<FetchContentJob, ClusterNode> jobs = new HashMap<>();

            for (Map.Entry<ClusterNode, List<String>> e : subsByNode.entrySet()) {
                ClusterNode node = e.getKey();
                FetchContentJob job = new FetchContentJob(e.getValue(), limit);

                jobs.put(job, node);
            }

            return jobs;
        }

        @Override
        public List<Post> reduce(List<ComputeJobResult> results) throws IgniteException {
            List<Post> posts = new ArrayList<>();

            for (ComputeJobResult res : results) {
                posts.addAll(res.getData());
            }

            posts.sort(Comparator.comparing(Post::createdUTCTimestamp));
            Collections.reverse(posts);
            while (posts.size() > limit) {
                posts.remove(posts.size() - 1);
            }

            return posts;
        }
    }

    public static class FetchContentJob implements ComputeJob, IgniteCallable<List<Post>> {
        private final List<String> users;
        private final int limit;

        @IgniteInstanceResource
        private Ignite ignite;

        public FetchContentJob(List<String> users, int limit) {
            this.users = users;
            this.limit = limit;
        }

        @Override
        public void cancel() {
        }

        @Override
        public Object execute() throws IgniteException {
            try {
                return call();
            } catch (Exception e) {
                throw new IgniteException("Failed to fetch posts.", e);
            }
        }

        private String joinedUsersList() {
            StringBuilder sb = new StringBuilder();

            for (String userId : users) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("'").append(userId).append("'");
            }

            return sb.toString();
        }

        @Override
        public List<Post> call() throws Exception {
            SqlFieldsQuery q = new SqlFieldsQuery(
                    "SELECT postId, userId, text, createdUTCTimestamp " +
                            "FROM posts " +
                            "WHERE userId IN (" + joinedUsersList() + ") " +
                            "ORDER BY createdUTCTimestamp DESC " +
                            "LIMIT " + limit);
            q.setLocal(true);

            List<List<?>> queryResult = postsCache(ignite).query(q).getAll();
            List<Post> posts = new ArrayList<>(queryResult.size());
            for (List<?> row : queryResult) {
                UUID postId = (UUID) row.get(0);
                String userId = (String) row.get(1);
                String text = (String) row.get(2);
                long createdTimestamp = (Long) row.get(3);
                Post post = new Post(postId, userId, text, createdTimestamp);

                posts.add(post);
            }

            return posts;
        }
    }
}
