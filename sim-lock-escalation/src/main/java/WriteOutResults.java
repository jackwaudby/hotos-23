import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteOutResults {

    public static void writeOutResults(int keys, int transactionSize, int rangeSize, int communities, long rangeLocked, long communityLocked) {

        String[] headers = {"keys", "transactionSize", "rangeSize", "communities", "rangeLocked", "communityLocked"};

        StringBuilder headerStringBuilder = new StringBuilder();
        for (String header : headers) {
            headerStringBuilder.append(header).append(",");
        }
        String headerString = headerStringBuilder.toString();
        if (headerString.length() > 0) // remove trailing comma
        {
            headerString = headerString.substring(0, headerString.length() - 1);
        }

        BufferedWriter outputStream = null;
        FileWriter fileWriter;
        try {
            File file = new File("results.csv");
            String format = String.format("%s,%s,%s,%s,%s,%s", keys, transactionSize, rangeSize, communities, rangeLocked, communityLocked);
            if (!file.exists()) {
                fileWriter = new FileWriter(file, true);
                outputStream = new BufferedWriter(fileWriter);
                outputStream.append(headerString);
                outputStream.append("\n");
                outputStream.append(format);
                outputStream.append("\n");
            } else {
                fileWriter = new FileWriter(file, true);
                outputStream = new BufferedWriter(fileWriter);
                try {
                    outputStream.append(format);
                    outputStream.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}