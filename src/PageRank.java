import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class PageRank {

    private String fileName = "";
    private double epsilon; // approximation
    private double beta = 0.85;  // teleportation
    private HashMap<String, ArrayList<String>> pageRankGraph = new HashMap<>();
    private HashMap<String, Integer> stringToInt = new HashMap<>();
    private HashMap<Integer, String> intToString = new HashMap<>();
    private double[] pageRank;
    private int n; // number of vertices
//    private int iteration;

    // Constructor

    public PageRank(String fileName, double epsilon) throws FileNotFoundException {
        this.fileName = fileName;
        this.epsilon = epsilon;

//        this.preProcess();

        File f = new File(this.fileName);
        if (f.isFile()) {
            Scanner sc = new Scanner(f);
            String firstLine = sc.nextLine();
            this.n = Integer.parseInt(firstLine);

            while(sc.hasNextLine()) {

                String line = sc.nextLine();
                String arr[] = line.split(" ", 2);
                String nodename = arr[0];
                String link = arr[1];

                ArrayList<String> nodeLinks = pageRankGraph.get(nodename);

                if (nodeLinks == null) {
                    nodeLinks = new ArrayList<String>();
                    nodeLinks.add(link);
                    pageRankGraph.put(nodename, nodeLinks);

                } else {
                    nodeLinks.add(link);
                    pageRankGraph.put(nodename, nodeLinks);
                }
            }

        }

        mapVertextoId();
//        mapIdtoVertex();

        pageRank = computePageRank();

    }

    // Methods

    private void mapVertextoId(){
        Integer i = 0;
        for (String vertex : pageRankGraph.keySet()){
//            Integer hashCode = (Integer)vertex.hashCode();
            stringToInt.put(vertex, i);
            intToString.put(i, vertex);
            i++;
        }
    }

//    private void mapIdtoVertex(){
//        Integer i = 0;
//        for (String vertex : pageRankGraph.keySet()){
//            i = stringToInt.get(vertex);
//            intToString.put(i, vertex);
//        }
//    }


    // Computer Page Rank
    // Input: G, P_t, approximation parameter -> compute P_{t+1}
    private double[] computePageRank() {

        //pre compute
        double[] pNext = new double[n];
        double p0 = (double) 1 / n;
        for (int i = 0; i < n; i++) {
            pNext[i] = p0;
        }

        int count = 0;
        boolean converge = false;

        double[] pCurrent;

        while(!converge){
            pCurrent = pNext;
            pNext = computePNext(pCurrent);

            if(compare(pCurrent, pNext, epsilon)){
                converge = true;
            }
            count++;
        }

        System.out.println("\n Number of iterations of converge: " + count +"\n");
        return pNext;
    }


    // Compute P_{t+1}
    // Input: G, P_t -> compute P_{t+1}
    private double[] computePNext(double[] currentRank) {
        double[] rank = new double[n];

        double firstTerm = (1 - beta) / n;
//        Arrays.fill(rank, firstTerm);
        for (String vertex : pageRankGraph.keySet()) {
            int outDegree = outDegreeOf(vertex);
            if (outDegree != 0) {
                List<String> nextVertices = pageRankGraph.get(vertex);
                double secondTerm = beta * (currentRank[stringToInt.get(vertex)] / outDegree);
                for (String v : nextVertices) {
                    rank[stringToInt.get(v)] += secondTerm;
                }
            }

            if (outDegree == 0){
                double secondTerm = beta * (currentRank[stringToInt.get(vertex)] / n);
                for (String v : pageRankGraph.keySet()) {
                    rank[stringToInt.get(v)] += secondTerm;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            rank[i] += firstTerm;
        }
//        Arrays.fill(rank, firstTerm);

        return rank;
    }

    int outDegreeOf(String vertex) {
        return pageRankGraph.get(vertex).size();
    }

    private boolean compare(double[] pCurrent, double[] pNext, double approxError) {

        boolean foo = false;
        double norm = 0;
        for (int i = 0; i < n; i++) {
            norm += Math.abs(pNext[i] - pCurrent[i]);
            if(norm < approxError){
                foo = true;
                break;
            }
        }
        return foo;

    }


    // gets an int representing a vertex of the graph as parameter and returns its page rank which is a double
    public double pageRankOf (int idx){

        return pageRank[idx];
    }

    // returns number of edges of the graph
    public int numEdges (){
        int edges = 0;
        for (String vertex: pageRankGraph.keySet()){
            edges += pageRankGraph.get(vertex).size();
        }
        return edges;
    }

    // gets an integer k as parameter and returns an array (of ints) of pages with top k page ranks
    public int[] topKPageRank(int k) {

        String[] stringRank = new String[k];
        int[] intRank = new int[k];
        Set<String> taken = new HashSet<>();
        for (int i = 0; i < k; i++) {
            int max = 0;
            while (taken.contains(intToString.get(max)))
                max++;

            for (int j = max + 1; j < n; j++) {
                if (!taken.contains(intToString.get(j)) && pageRankOf(max) < pageRankOf(j)) {
                    max = j;
                }
            }
            taken.add(intToString.get(max));
            intRank[i] = stringToInt.get(max);
        }

//        for (int i = 0; i < k; i++) {
//
//        }

        return intRank;
    }

    public static void main(String[] args) throws FileNotFoundException {
        PageRank pageRank = new PageRank("G:\\535\\PA3\\WikiSportsGraph.txt", 0.01);
        System.out.println(pageRank.pageRank);
    }

}
