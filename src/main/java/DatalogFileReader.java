import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DatalogFileReader {
    private final String filePath;
    private final List<String> queryToConvert = new ArrayList<>();
    private final LinkedHashMap<String, List<String>> tNameAndCName = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> tNameAndAttrNum = new LinkedHashMap<>();

    DatalogFileReader(String filePath) throws IOException {
        this.filePath = "input/" + filePath;
    }

    public void read() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.filePath));
        String line = br.readLine();
        while (line != null) {
            line = line.replaceAll("\\s", ""); // remove '%' and possible spaces
            if (line.contains("%")) { // table structure
                line = line.substring(1); // remove '%'
                String tableName = line.split("\\(")[0];

                // e.g. "cName,cAge)"
                List<String> columnsName = new ArrayList<>();
                Collections.addAll(columnsName, line.split("\\(")[1]
                        .replaceAll(",", " ")
                        .replaceAll("\\)", "")
                        .split(" "));
                this.tNameAndCName.put(tableName, columnsName);
                this.tNameAndAttrNum.put(tableName, columnsName.size());
            }else {
                this.queryToConvert.add(line.replaceAll("\\.", ""));
            }
            line = br.readLine();
        }
    }

    public List<String> getQueryToConvert() {
        return this.queryToConvert;
    }

    public LinkedHashMap<String, List<String>> getTNameAndCName() {
        return this.tNameAndCName;
    }

    public LinkedHashMap<String, Integer> getTNameAndAttrNum() {
        return this.tNameAndAttrNum;
    }
}
