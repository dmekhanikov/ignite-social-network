package dm.social;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import dm.social.model.Post;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DataLoader {
    private static final int ID_COL = 1;
    private static final int DATE_COL = 2;
    private static final int USER_COL = 4;
    private static final int TEXT_COL = 5;

    // Example: Wed Jun 17 16:50:14 PDT 2009
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    private final Ignite ignite;

    public DataLoader(Ignite ignite) {
        this.ignite = ignite;
    }

    public void load(File sourceFile) throws IOException, CsvException {
        ConfigurationManager cfgManager = ConfigurationManager.instance();

        ignite.getOrCreateCache(cfgManager.postsCacheConfiguration());

        BufferedReader br = new BufferedReader(new FileReader(sourceFile));
        try (CSVReader csvReader = new CSVReader(br);
             IgniteDataStreamer<UUID, Post> streamer = ignite.dataStreamer(ConfigurationManager.POSTS_CACHE)) {
            streamer.allowOverwrite(true);

            ignite.log().info("Started loading data.");

            int rowsProcessed = 0;
            for (String[] row = csvReader.readNext(); row != null; row = csvReader.readNext()) {
                try {
                    long createdTimestamp = dateToTimestamp(row[DATE_COL]);
                    String userId = row[USER_COL];
                    UUID postId = UUID.randomUUID();
                    String text = row[TEXT_COL];
                    Post post = new Post(postId, userId, text, createdTimestamp);

                    streamer.addData(postId, post);
                } catch (ParseException e) {
                    ignite.log().error("Failed to parse date of a tweet. Skipping. Tweet ID: " + row[ID_COL]);
                }

                rowsProcessed++;
                if (rowsProcessed != 0 && rowsProcessed % 100_000 == 0) {
                    ignite.log().info(rowsProcessed + " rows processed.");
                }
            }
        }
    }

    private long dateToTimestamp(String dateString) throws ParseException {
        Date date = DATE_FORMAT.parse(dateString);
        return date.getTime();
    }

    public static void main(String[] args) throws Exception {
        String configFile = args[0];
        File sourceFile = new File(args[1]);

        Ignition.setClientMode(true);

        try (Ignite ignite = Ignition.start(configFile)) {
            DataLoader loader = new DataLoader(ignite);
            loader.load(sourceFile);
        }
    }
}
