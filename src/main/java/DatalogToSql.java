import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DatalogToSql {
    public static void main(String[] args) throws IOException, SQLException {
        // read datalog files (*.dl)
        DatalogFileReader datalogFileReader = new DatalogFileReader(args[0]);
        datalogFileReader.read();
        List<String> queries = datalogFileReader.getQueryToConvert();
        System.out.println("--------------Information from file " + args[0] + "--------------");
        System.out.println(datalogFileReader.getQueryToConvert());
        System.out.println(datalogFileReader.getTNameAndAttrNum());
        System.out.println(datalogFileReader.getTNameAndCName());

        // detect problem types
        System.out.println("--------------Queries types hashmap--------------");
        DatalogProblemTypeParser datalogProblemTypeParser = new DatalogProblemTypeParser(queries);
        LinkedHashMap<String, String> problemsAndTypes = datalogProblemTypeParser.getProblemTypeParser();
        System.out.println(datalogProblemTypeParser.getProblemTypeParser());

        // translate according to problem types
        System.out.println("--------------Queries translating...--------------");
        DatalogTranslator datalogTranslator = new DatalogTranslator(problemsAndTypes, datalogFileReader.getTNameAndCName());
        datalogTranslator.translate();
        List<String> results = datalogTranslator.getResults();

        // write to files (*.sql)
        System.out.println("--------------SQL file outputting...--------------");
        DatalogFileWriter datalogFileWriter = new DatalogFileWriter(args[1], results, true);
        datalogFileWriter.write();

        String sql;
        if (!args[2].equals("")) {
            List<String> sqlResults = new ArrayList<>();
            sql = "DELETE FROM customer;\nDELETE FROM seller;\nDELETE FROM selling;";
            SQLResultTracer sqlPrepTemp = new SQLResultTracer(sql);
            sqlPrepTemp.getStatement().executeUpdate();
            sqlPrepTemp.close();

            for (String s : results) {
                if (s.split(" ")[0].equals("INSERT")) {
                    sql = s;
                    SQLResultTracer sqlResultTracer = new SQLResultTracer(sql);
                    sqlResultTracer.getStatement().executeUpdate();
                    sqlResultTracer.close();
                } else {
                    sql = s.split("AS\n")[1];
                    sqlResults.add(sql);
                    sqlResults.add("\nAnswer:\n");

                    SQLResultTracer sqlResultTracer = new SQLResultTracer(sql);
                    ResultSet resultSet = sqlResultTracer.getStatement().executeQuery();
                    while (resultSet.next()) {
                        StringBuilder resTemp = new StringBuilder();
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            resTemp.append(resultSet.getString(i)).append('\t');
                        }
                        sqlResults.add(resTemp.toString());
                    }
                    sqlResults.add("---");
                    sqlResultTracer.close();
                }
            }

            DatalogFileWriter sqlResultFileWriter = new DatalogFileWriter(args[2], sqlResults, false);
            sqlResultFileWriter.write();
        }
    }
}
