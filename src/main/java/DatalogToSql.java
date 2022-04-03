import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class DatalogToSql {
    public static void main(String[] args) throws IOException {
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
        DatalogTranslator datalogTranslator = new DatalogTranslator(problemsAndTypes);
        datalogTranslator.translate();

        // write to files (*.sql)
        System.out.println("--------------SQL file outputting...--------------");
        DatalogFileWriter datalogFileWriter = new DatalogFileWriter(args[1], queries);
        datalogFileWriter.write();
    }
}
