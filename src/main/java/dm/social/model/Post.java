package dm.social.model;

import java.util.Date;

public class Post {
    private PostKey key;
    private PostContent content;

    public Post(PostKey key, PostContent content) {
        this.key = key;
        this.content = content;
    }

    public PostKey key() {
        return key;
    }

    public PostContent content() {
        return content;
    }

    @Override
    public String toString() {
        Date date = new Date(content.createdUTCTimestamp());

        return "[" + date + "] @" + key.userId() + ": " + content.text();
    }
}
