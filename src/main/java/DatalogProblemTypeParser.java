import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class DatalogProblemTypeParser {
    private final LinkedHashMap<String, String> problemTypes = new LinkedHashMap<>();
    private final static List<String> joinWords = Arrays.asList("ij", "fj", "lj", "rj");

    DatalogProblemTypeParser(List<String> problems) {
        for (String query : problems) {
            parse(query);
        }
    }

    private void parse(String query) {
        String problemType;
        if (!query.contains(":-")) {
            problemType = "facts";
        } else {
            if (joinWords.contains(query.split(":-")[1].split("\\(")[0])) {
                problemType = "join";
            } else if (query.contains(";")) {
                problemType = "union";
            } else if (query.contains("group_by")) {
                problemType = "groupby";
            } else if (query.contains("<") || query.contains(">") || query.contains("=")) {
                problemType = "select";
            } else if (query.contains("not")) {
                problemType = "difference";
            } else {
                problemType = "projection";
            }
        }
        problemTypes.put(query, problemType);
    }

    public LinkedHashMap<String, String> getProblemTypeParser() {
        return problemTypes;
    }
}
