import java.sql.SQLOutput;
import java.util.*;

public class DatalogTranslator {
    private final LinkedHashMap<String, String> problemsAndTypes;
    private final LinkedHashMap<String, List<String>> tablesAndColumns;
    private final List<String> results;

    DatalogTranslator(LinkedHashMap<String, String> problemsAndTypes, LinkedHashMap<String, List<String>> tablesAndColumns) {
        this.problemsAndTypes = problemsAndTypes;
        this.tablesAndColumns = tablesAndColumns;
        this.results = new ArrayList<>();
    }

    public void translate() {
        // translate
        Translator translator;
        for (Map.Entry<String, String> currentQuery : this.problemsAndTypes.entrySet()) {
            switch (currentQuery.getValue()) {
                case "join":
                    translator = new JoinTranslator();
                    break;
                case "union":
                    translator = new UnionTranslator();
                    break;
                case "select":
                    translator = new SelectTranslator();
                    break;
                case "difference":
                    translator = new DifferenceTranslator();
                    break;
                case "projection":
                    translator = new ProjectionTranslator();
                    break;
                default:
                    translator = new FactTranslator();
            }

            String currentResult = translator.translate(currentQuery.getKey());
            System.out.println(currentResult);
            this.results.add(currentResult);
        }
    }

    public List<String> getResults() {
        return this.results;
    }

    private abstract class Translator {
        public abstract String translate(String query);
    }

    private class ProjectionTranslator extends Translator {
        @Override
        public String translate(String query) {
            // e.g. PROJECTION_RULE(cName) :- Customer(cName)
            String viewName = query.split(":-")[0].split("\\(")[0];
            return  "DROP VIEW IF EXISTS "
                    + viewName
                    + " CASCADE;\nCREATE VIEW "
                    + viewName
                    + " AS\nSELECT "
                    + String.join(", ", query.split(":-")[1].split("\\(")[1].replaceAll("\\)", "").split(","))
                    + "\nFROM "
                    + query.split(":-")[1].split("\\(")[0]
                    + ";";
        }
    }

    private class JoinTranslator extends Translator {
        @Override
        public String translate(String query) {
            String joinType = query.split(":-")[1].split("\\(")[0];
            Map<String, String> joinTypesMap = new HashMap<>() {{
                put("ij", "INNER JOIN");
                put("fj", "FULL JOIN");
                put("lj", "LEFT JOIN");
                put("rj", "RIGHT JOIN");
            }};

            return query;
        }
    }

    private class UnionTranslator extends Translator {
        @Override
        public String translate(String query) {
            return query;
        }
    }

    private class SelectTranslator extends Translator {
        @Override
        public String translate(String query) {
            return query;
        }
    }

    private class DifferenceTranslator extends Translator {
        @Override
        public String translate(String query) {
            return query;
        }
    }

    private class FactTranslator extends Translator {
        @Override
        public String translate(String query) {
            // e.g. Customer('Robert', 18).
            String tableName = query.split("\\(")[0];
            return "INSERT INTO "
                    + tableName
                    + " ("
                    + String.join(", ", DatalogTranslator.this.tablesAndColumns.get(tableName))
                    + ") VALUES ("
                    + query.split("\\(")[1].replaceAll(",", ", ")
                    + ";";
        }
    }
}
