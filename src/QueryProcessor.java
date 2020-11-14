import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;

import java.util.ArrayList;


//QueryProcessor class -  Searches the document base with the given query
public class QueryProcessor {

    private PositionalIndex pi;


    /* Returns an ArrayList consisting of top k documents that are relevant to the query */
    public ArrayList<String> topKDocs(String query, int k, String folder){
        pi = new PositionalIndex(folder);
        String[] topKDocsArray = new String[k];
        double[] topKDocsRelevanceScores = new double[k];
        String[] allDocs = pi.getAllDocs();

        for(int i=0; i<k; i++){
            topKDocsRelevanceScores[i] = -1.0;
        }

        for(String doc : allDocs){
            double relevance = pi.Relevance(query, doc);

            for(int i=0; i<k; i++){
                if(topKDocsRelevanceScores[i] == -1.0){
                    topKDocsRelevanceScores[i] = relevance;
                    topKDocsArray[i] = doc;
                    break;
                }
                else if(topKDocsRelevanceScores[i] < relevance){
                    for(int j = k-1; j > i; j--){
                        topKDocsRelevanceScores[j] = topKDocsRelevanceScores[j-1];
                        topKDocsArray[j] = topKDocsArray[j-1];
                    }
                    topKDocsRelevanceScores[i] = relevance;
                    topKDocsArray[i] = doc;
                    break;
                }

            }

        }

        ArrayList<String> topKDocsList = new ArrayList<String>();

        for(String s : topKDocsArray){
            topKDocsList.add(s);
        }

        return topKDocsList;
    }

    public static void main(String[] args) {
        QueryProcessor qp = new QueryProcessor();
        int k=10;
        ArrayList<String> topdocs = qp.topKDocs("Baseball World Cup", k, "/Users/harshavk/Desktop/gitrepos/PA3_Docs/IR");

        System.out.println("Top "+k+" documents:");
        System.out.println("----------------");

        int rank = 1;
        for(String topdoc : topdocs) {
            Double[] scores = qp.pi.getAllScores().get(topdoc);
            System.out.println(rank+". "+topdoc+"   [ TPScore : "+scores[0]+"   , VSScore : "+scores[1]+"  , Relevance Score : "+scores[2]+" ]");
            System.out.println();
            rank++;
        }
    }

}
