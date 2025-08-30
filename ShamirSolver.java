import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ShamirSolver {

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("input.json"));

            int k = root.get("keys").get("k").asInt();

            List<Share> allShares = new ArrayList<>();
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                if (key.equals("keys")) {
                    continue;
                }

                JsonNode node = root.get(key);
                BigInteger x = new BigInteger(key);
                int base = Integer.parseInt(node.get("base").asText());
                String valueStr = node.get("value").asText();
                BigInteger y = new BigInteger(valueStr, base);
                allShares.add(new Share(x, y));
            }

            allShares.sort(Comparator.comparing(share -> share.x));
            List<Share> sharesToUse = allShares.subList(0, k);

            System.out.println("Using the following " + k + " shares to reconstruct the secret:");
            for (Share share : sharesToUse) {
                System.out.println("Share ID (x): " + share.x + ", Value (y): " + share.y);
            }

            BigInteger secret = lagrangeInterpolation(sharesToUse, BigInteger.ZERO);
            System.out.println("\nReconstructed Secret: " + secret);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BigInteger lagrangeInterpolation(List<Share> points, BigInteger x) {
        BigInteger numeratorSum = BigInteger.ZERO;
        BigInteger denominatorSum = BigInteger.ONE;

        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            BigInteger termNum = yi;
            BigInteger termDen = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    BigInteger xj = points.get(j).x;
                    termNum = termNum.multiply(x.subtract(xj));
                    termDen = termDen.multiply(xi.subtract(xj));
                }
            }

            numeratorSum = numeratorSum.multiply(termDen).add(termNum.multiply(denominatorSum));
            denominatorSum = denominatorSum.multiply(termDen);
        }

        return numeratorSum.divide(denominatorSum);
    }

    static class Share {
        BigInteger x;
        BigInteger y;

        public Share(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}
