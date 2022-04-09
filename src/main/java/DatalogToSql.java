import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class DatalogToSql {
    public static void main(String[] args) throws IOException {
        // read datalog files (*.dl)
        String input = "input.dl";
        String output = "output.sql";
        DatalogFileReader datalogFileReader = new DatalogFileReader(input);
        datalogFileReader.read();
        List<String> queries = datalogFileReader.getQueryToConvert();
        System.out.println("--------------Information from file " + input + "--------------");
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
        DatalogFileWriter datalogFileWriter = new DatalogFileWriter(output, results);
        datalogFileWriter.write();
    }
}
