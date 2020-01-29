package dm.social.service;

import dm.social.ConfigurationManager;
import dm.social.model.Post;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

import java.util.*;

public class ContentService {
    private final Ignite ignite;

    public ContentService(Ignite ignite) {
        this.ignite = ignite;
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

    private static IgniteCache<UUID, Post> postsCache(Ignite ignite) {
        CacheConfiguration<UUID, Post> cacheCfg = ConfigurationManager.instance().postsCacheConfiguration();
        return ignite.getOrCreateCache(cacheCfg);
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
