import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//    private int count(String s, char ch) {
//        HashMap<Character, Integer> charRefs = new HashMap<>();
//        for (int i = 0; i < s.length(); i++) {
//            if (!charRefs.containsKey(s.charAt(i))) {
//                charRefs.put(s.charAt(i), 1);
//            } else {
//                charRefs.put(s.charAt(i), charRefs.get(s.charAt(i)) + 1);
//            }
//        }
//
//        return charRefs.getOrDefault(ch, 0);
//    }

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
        // e.g. INNER_JOIN_RULE(cName) :- ij(Customer(cName, cAge), Seller(sName, sAge), cName = sName, cAge = sAge).
        public String translate(String query) {
            String joinType = query.split(":-")[1].split("\\(")[0];
            Map<String, String> joinTypesMap = new HashMap<>() {{
                put("ij", "INNER JOIN");
                put("fj", "FULL JOIN");
                put("lj", "LEFT JOIN");
                put("rj", "RIGHT JOIN");
            }};

            List<String> tables = new ArrayList<>();
            List<List<String>> attrs = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            String viewName = query.split(":-")[0].split("\\(")[0];
            String queryBody = query.split(":-")[1].substring(2);

            // find all attributes
            Pattern p = Pattern.compile("(\\([^()]*)\\w+([^()]*\\))");
            Matcher m = p.matcher(queryBody);

            while (m.find()) {
                attrs.add(Arrays.asList(queryBody.substring(m.start(), m.end()).replaceAll("[()*]", "").split(",")));
            }

            // find all table names
            p = Pattern.compile("\\w+\\(");
            m = p.matcher(queryBody);

            while (m.find()) {
                tables.add(queryBody.substring(m.start(), m.end()).replaceAll("\\(", ""));
            }

            // find all conditions
            for (String sentence : queryBody.split(",")) {
                if (!(sentence.contains("(") && sentence.contains(")")) && sentence.contains("=")) {
                    conditions.add(sentence.replaceAll("[()*]", ""));
                }
            }

            // update attribute names (add namespace)
            for (int i = 0; i < attrs.size(); i++) {
                for (int j = 0; j < attrs.get(i).size(); j++) {
                    attrs.get(i).set(j, tables.get(i) + "." + attrs.get(i).get(j));
                }
            }

            System.out.println(tables);
            System.out.println(attrs);
            System.out.println(conditions);

            // construct result
            StringBuilder res = new StringBuilder("DROP VIEW IF EXISTS "
                    + viewName
                    + " CASCADE;\nCREATE VIEW "
                    + viewName
                    + " AS\nSELECT ");

            res.append(query.split(":-")[0]);
//            for (int i = 0; i < attrs.size(); i++) {
//                res.append(String.join(", ", attrs.get(i)));
//                if (i < attrs.size() - 1) {
//                    res.append(", ");
//                }
//            }

            res.append("\nFROM ").append(tables.get(0))
                    .append(" ")
                    .append(joinTypesMap.get(query.split(":-")[1].split("\\(")[0]))
                    .append(" ")
                    .append(tables.get(1))
                    .append("\n")
                    .append("ON ");

            for (int i = 0; i < conditions.size(); i++) {
                List<String> conditionList = Arrays.asList(conditions.get(i).split("="));
                res.append(tables.get(0))
                        .append(".")
                        .append(conditionList.get(0))
                        .append(" = ").append(tables.get(1))
                        .append(".")
                        .append(conditionList.get(1));
                if (i < conditions.size() - 1) {
                    res.append(" and ");
                }
            }

            res.append(";");

            return res.toString();
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
