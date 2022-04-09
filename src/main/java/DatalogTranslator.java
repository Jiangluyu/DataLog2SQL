import com.sun.source.tree.WhileLoopTree;

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

            List<String> selectedAttrs = new ArrayList<>();
            List<String> tables = new ArrayList<>();
            List<List<String>> attrs = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            String viewName = query.split(":-")[0].split("\\(")[0];
            String queryRule = query.split(":-")[0].split("\\(")[1].replaceAll("\\)", "");
            String queryBody = query.split(":-")[1].substring(2);

            // find all attrs in query rule
            Collections.addAll(selectedAttrs, queryRule.split(","));

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
                    + " AS\nSELECT "
                    + String.join(", ", selectedAttrs));

            res.append("\nFROM ").append(tables.get(0))
                    .append(" ")
                    .append(joinTypesMap.get(joinType))
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
            String unionLeft = query.split(";")[0].split(":-")[1];
            String unionRight = query.split(";")[1];
            String viewName = query.split(":-")[0].split("\\(")[0];
            String selectName1 = unionLeft.split("\\(")[0];
            String selectPara1 = unionLeft.split("\\(")[1];
            selectPara1 = selectPara1.substring(0, selectPara1.length()-1);
            String selectName2 = unionRight.split("\\(")[0];
            String selectPara2 = unionRight.split("\\(")[1];
            selectPara2 = selectPara2.substring(0, selectPara1.length());
            return "DROP VIEW IF EXISTS "
                    + viewName
                    + " CASCADE;\nCREATE VIEW "
                    + viewName
                    + " AS\nSELECT " +  selectPara1 + " FROM " + selectName1
                    + "\nUNION"
                    + "\nSELECT " +  selectPara2 + " FROM " + selectName2 + ";";
        }
    }

    //SELECTION_RULE1(cName, cAge) :- Customer(cName, cAge), cName = 'Jake', cage = 23.
    //SELECTION_RULE2(cName) :- Customer(cName, cAge), Seller(cName, sName, sPrice), sPrice = 560.
    private class SelectTranslator extends Translator {
        @Override
        public String translate(String query) {
            // e.g. SELECTION_RULE(cName, c) :- Customer(cName, b, c), cName='Jake'
            if(query.contains("<") || query.contains(">") || query.contains("=")) {
                String viewName = query.split(":-")[0].split("\\(")[0];
                String[] viewPara = query.split(":-")[0].split("\\(")[1].split("\\)")[0].split(",");
                String[] selectName = query.split(":-")[1].split("=")[0].split("\\),");
                String selection = query.split(":-")[1];
                Map<String, String> selectNameAndPara = new HashMap<>();
                for (int i = 0; i < selectName.length - 1; i++) {
                    selectNameAndPara.put(selectName[i].split("\\(")[0] + " " + selectName[i].split("\\(")[0].substring(0, 1).toLowerCase(), selectName[i].split("\\(")[1]);
                }
                String[] selections = selection.split("\\),");
                selection = selections[selections.length - 1];
                String selectNames = "";
                for (int i = 0; i < selectNameAndPara.keySet().size(); i++) {
                    String name = (String) (selectNameAndPara.keySet().toArray()[i]);
                    if (i < selectNameAndPara.keySet().size() - 1) selectNames += name + ',';
                    else selectNames += name;
                }
                String viewParas = "";
                for (int i = 0; i < viewPara.length; i++) {
                    String para = viewPara[i];
                    for (String selectN : selectNameAndPara.keySet()) {
                        if (selectNameAndPara.get(selectN).contains(para)) {
                            if (i < viewPara.length - 1)
                                viewParas += selectN.substring(0, 1).toLowerCase() + "." + para + ", ";
                            else viewParas += selectN.substring(0, 1).toLowerCase() + "." + para;
                            break;
                        }
                    }
                }
                String selectionPlus = "";
                for (int i = 0; i < selectNameAndPara.keySet().size(); i++) {
                    Object[] selectNameArr = selectNameAndPara.keySet().toArray();
                    String selectNameCurrent = (String) selectNameArr[i];
                    String[] selectParaCurrent = selectNameAndPara.get(selectNameCurrent).split(",");
                    for (int j = i + 1; j < selectNameAndPara.keySet().size(); j++) {
                        String selectNameNew = (String) selectNameArr[j];
                        String[] selectParaNew = selectNameAndPara.get(selectNameNew).split(",");
                        for (String s : selectParaCurrent) {
                            for (String ss : selectParaNew) {
                                if (s.equals(ss)) selectionPlus += selectNameCurrent.substring(0, 1).toLowerCase()
                                        + "." + s + "=" + selectNameNew.substring(0, 1).toLowerCase() + "." + ss + ",";
                            }
                        }
                    }
                }
                String selectionResult = "";
                selections = selection.split(",");
                int idx = 0;
                for (int i = 0; i < selectNameAndPara.keySet().size(); i++) {
                    if(idx == selections.length) break;
                    Object[] selectNameArr = selectNameAndPara.keySet().toArray();
                    String selectNameCurrent = (String) selectNameArr[i];
                    String[] selectParaCurrent = selectNameAndPara.get(selectNameCurrent).split(",");
                    //not perfect->need para in selections to be ordered in each selectPara
                    for(String s: selectParaCurrent) {
                        if(selections[idx].contains(s) && idx<selections.length-1) {
                            selectionResult += selectNameCurrent.substring(0,1).toLowerCase() + "." + selections[idx] + ",";
                            idx++;
                        }
                        else if(selections[idx].contains(s) && idx==selections.length-1) {
                            selectionResult += selectNameCurrent.substring(0, 1).toLowerCase() + "." + selections[idx];
                            idx++;
                        }
                        if(idx == selections.length) break;
                    }
                }
                return "DROP VIEW IF EXISTS "
                        + viewName
                        + " CASCADE;\nCREATE VIEW "
                        + viewName
                        + " AS\nSELECT "
                        +  viewParas
                        + "\nFROM "
                        + selectNames
                        + "\nWHERE "
                        + selectionPlus + selectionResult
                        + ";";
            }
            else {
                String viewName = query.split(":-")[0].split("\\(")[0];
                String[] viewPara = query.split(":-")[0].split("\\(")[1].split("\\)")[0].split(",");
                String[] selectName = query.split(":-")[1].split("=")[0].split("\\),");
                Map<String, String> selectNameAndPara = new HashMap<>();
                for (int i = 0; i < selectName.length; i++) {
                    selectNameAndPara.put(selectName[i].split("\\(")[0] + " " + selectName[i].split("\\(")[0].substring(0, 1).toLowerCase(), selectName[i].split("\\(")[1]);
                }
                String selectNames = "";
                for (int i = 0; i < selectNameAndPara.keySet().size(); i++) {
                    String name = (String) (selectNameAndPara.keySet().toArray()[i]);
                    if (i < selectNameAndPara.keySet().size() - 1) selectNames += name + ',';
                    else selectNames += name;
                }
                String viewParas = "";
                for (int i = 0; i < viewPara.length; i++) {
                    String para = viewPara[i];
                    for (String selectN : selectNameAndPara.keySet()) {
                        if (selectNameAndPara.get(selectN).contains(para)) {
                            if (i < viewPara.length - 1)
                                viewParas += selectN.substring(0, 1).toLowerCase() + "." + para + ", ";
                            else viewParas += selectN.substring(0, 1).toLowerCase() + "." + para;
                            break;
                        }
                    }
                }
                String selectionPlus = "";
                for (int i = 0; i < selectNameAndPara.keySet().size(); i++) {
                    Object[] selectNameArr = selectNameAndPara.keySet().toArray();
                    String selectNameCurrent = (String) selectNameArr[i];
                    String[] selectParaCurrent = selectNameAndPara.get(selectNameCurrent).split(",");
                    for (int j = i + 1; j < selectNameAndPara.keySet().size(); j++) {
                        String selectNameNew = (String) selectNameArr[j];
                        String[] selectParaNew = selectNameAndPara.get(selectNameNew).split(",");
                        for (String s : selectParaCurrent) {
                            for (String ss : selectParaNew) {
                                if (s.equals(ss)) selectionPlus += selectNameCurrent.substring(0, 1).toLowerCase()
                                        + "." + s + "=" + selectNameNew.substring(0, 1).toLowerCase() + "." + ss + ",";
                            }
                        }
                    }
                }
                return "DROP VIEW IF EXISTS "
                        + viewName
                        + " CASCADE;\nCREATE VIEW "
                        + viewName
                        + " AS\nSELECT "
                        +  viewParas
                        + "\nFROM "
                        + selectNames
                        + "\nWHERE "
                        + selectionPlus.substring(0, selectionPlus.length()-1)
                        + ";";
            }
        }
    }

    private class DifferenceTranslator extends Translator {
        @Override
        public String translate(String query) {
            String notLeft = query.split(",not")[0].split(":-")[1];
            String notRight = query.split("not")[1];
            String viewName = query.split(":-")[0].split("\\(")[0];
            String selectName1 = notLeft.split("\\(")[0];
            String selectPara1 = notLeft.split("\\(")[1];
            selectPara1 = selectPara1.substring(0, selectPara1.length()-1);
            String selectName2 = notRight.split("\\(")[0];
            String selectPara2 = notRight.split("\\(")[1];
            selectPara2 = selectPara2.substring(0, selectPara1.length());
            return "DROP VIEW IF EXISTS "
                    + viewName
                    + " CASCADE;\nCREATE VIEW "
                    + viewName
                    + " AS\nSELECT " +  selectPara1 + " FROM " + selectName1
                    + "\nEXCEPT"
                    + "\nSELECT " +  selectPara2 + " FROM " + selectName2 + ";";
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
