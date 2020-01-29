package dm.social;

import dm.social.model.Post;
import dm.social.service.ContentService;
import dm.social.service.SubscriptionService;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.NullLogger;

import java.util.List;
import java.util.Scanner;

public class ClientNode {
    private static final int POSTS_LIMIT = 10;

    private final String userId;
    private final ContentService contentService;

    public ClientNode(Ignite ignite, String userId) {
        this.userId = userId;
        this.contentService = new ContentService(ignite);
    }

    public static void main(String[] args) {
        Ignition.setClientMode(true);

        IgniteConfiguration igniteCfg = Ignition.loadSpringBean(args[0], "ignite.cfg");
        igniteCfg.setGridLogger(new NullLogger());

        try (Ignite ignite = Ignition.start(igniteCfg);
             Scanner inputScanner = new Scanner(System.in)) {
            System.out.print("Username: ");
            String userId = inputScanner.nextLine().trim();
            if (userId.contains(" ")) {
                System.err.println("User names can't contain spaces.");
            }

            ClientNode clientNode = new ClientNode(ignite, userId);

            while (true) {
                try {
                    System.out.print("> ");
                    if (!inputScanner.hasNext()) {
                        break;
                    }
                    String line = inputScanner.nextLine();

                    String[] tokens = line.split(" ");
                    String cmd = tokens[0];

                    switch (cmd.toLowerCase()) {
                        case "get":
                            if (tokens.length > 1) {
                                clientNode.get(tokens[1]);
                            } else {
                                System.err.println("User ID is missing.");
                            }
                            break;
                        case "post":
                            if (line.length() > cmd.length()) {
                                clientNode.post(line.substring(cmd.length() + 1));
                            } else {
                                System.err.println("Post text is missing.");
                            }
                            break;
                        default:
                            System.err.println("Unrecognized command: " + cmd);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to execute the command.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void post(String text) {
        contentService.createPost(userId, text);
    }

    private void get(String userId) {
        List<Post> posts = contentService.fetchPosts(userId, POSTS_LIMIT);
        for (Post post : posts) {
            System.out.println(post);
        }
    }
}
