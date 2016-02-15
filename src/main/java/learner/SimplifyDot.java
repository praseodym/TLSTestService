package learner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simplify Graphviz dot file
 */
public class SimplifyDot {
    public final static Pattern p = Pattern.compile("^\\s*(\\w+ -> \\w+) \\[label=\\\"(.+)\\\"\\];$");

    public static void main(String[] args) throws Exception {
        String path = "output/openssl_client/learnedModel";
        List<String> lines = Files.readAllLines(Paths.get(path + ".dot"));
        List<String> simpified = simplifyDot(lines);
        //System.out.println(simpified);
        simpified.stream().forEach(a -> System.out.println(a + "\n"));
        //Files.write(Paths.get(path + "_simple.dot"), simpified, Charset.defaultCharset());
    }

    public static List<String> simplifyDot(List<String> lines) {
        List<String> output = new ArrayList<>(lines.size());
        String oldEdge = "", newEdge, label = "";
        for (String line : lines) {
            Matcher matcher = p.matcher(line);
            String newLabel;
            if (matcher.matches()) {
                newEdge = matcher.group(1);
                newLabel = matcher.group(2);
                // TODO: ignore ConnectionClosed?
            } else {
                newEdge = "";
                newLabel = "";
            }

            if (oldEdge.isEmpty() || oldEdge.equals(newEdge)) {
                // TODO: more intelligent label merging (Strings.commonPrefix?)
                label += (!label.isEmpty() ? " | " : "") + newLabel;
            } else {
                output.add("\t" + oldEdge + " [label=\"" + label.trim() + "\"];");
                label = newLabel;
            }

            if (!matcher.matches()) {
                output.add(line);
            }
            oldEdge = newEdge;
        }

        return output;
    }

}
