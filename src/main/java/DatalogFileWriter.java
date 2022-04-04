import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatalogFileWriter {
    private final String filePath;
    private final List<String> sqls;

    DatalogFileWriter(String filePath, List<String> queries) {
        this.filePath = "output/" + filePath;
        this.sqls = queries;
    }

    public void write() throws IOException {
        File outputDir = new File("output/");
        boolean dirCreateState = outputDir.mkdir();
        if (dirCreateState) {
            System.out.println("Directory output/ created.");
        }

        File file = new File(this.filePath);
        boolean fileCreateState = file.createNewFile();
        if (fileCreateState) {
            System.out.println("File " + this.filePath + " created.");
        } else {
            System.out.println("File " + this.filePath + " modified.");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(this.filePath));
        for (String sql : this.sqls) {
            bw.write(sql);
            bw.newLine();
            bw.newLine(); // line breaker, to be more clear
            bw.flush();
        }
        bw.close();
    }
}
