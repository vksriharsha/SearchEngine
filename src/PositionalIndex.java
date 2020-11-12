import java.io.File;
import java.util.*;

public class PositionalIndex {

    private String folderName;
    private HashMap<String, HashMap<Integer, ArrayList<Integer>>> invertedIndex;
    private String[] allDocs;
    private HashMap<String, Integer> allDocsHash;
    private HashMap<String,Double[]> allScores;

    /* Constructor */
    public PositionalIndex(String folderName){
        this.folderName = folderName;
        this.invertedIndex = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
        this.allScores = new HashMap<String,Double[]>();
        preprocess();
    }

    public String[] getAllDocs(){
        return this.allDocs;
    }

    public HashMap<String, Integer> getAllDocsHash(){
        return this.allDocsHash;
    }

    public HashMap<String,Double[]> getAllScores(){
        return this.allScores;
    }


    /* Preprocess the terms in all the documents and store them in an Inverted Index */
    public void preprocess(){

        try {
            File docsFolder = new File(this.folderName);
            this.allDocs = docsFolder.list();

            this.allDocsHash = new HashMap<>();

            for(int i = 0 ; i < allDocs.length; i++){
                this.allDocsHash.put(allDocs[i],(Integer)i);
            }


            for (int i=0; i<this.allDocs.length; i++) {

                String doc = this.allDocs[i];
                File f = new File(this.folderName + File.separator + doc);
                if (f.isFile()) {
                    Scanner scanner = new Scanner(f);
                    int wordIndex = 0;

                    while (scanner.hasNext()) {

                        String nextword = scanner.next();
                        String nextwordlcs = nextword.toLowerCase();

                        nextwordlcs = nextwordlcs.replace(",","")
                                .replace("”","")
                                .replace("“","")
                                .replace("?","")
                                .replace("[","")
                                .replace("]","")
                                .replace("′","")
                                .replace("'","")
                                .replace("{","")
                                .replace("}","")
                                .replace(":","")
                                .replace(";","")
                                .replace("(","")
                                .replace(")","");

                        String regex = "^\\d*\\.\\d+|\\d+\\.\\d*$";

                        if(!nextwordlcs.matches(regex)) {
                        }
                        else {
                            nextwordlcs = nextwordlcs.replace(".","");
                        }

                        if(nextwordlcs.length() > 0){

                            if(invertedIndex.get(nextwordlcs) == null){
                                HashMap<Integer, ArrayList<Integer>> postings = new HashMap<Integer, ArrayList<Integer>>();
                                ArrayList<Integer> positions = new ArrayList<Integer>();

                                positions.add(wordIndex);
                                postings.put(i,positions);
                                invertedIndex.put(nextwordlcs,postings);

                            }
                            else{
                                HashMap<Integer, ArrayList<Integer>> postings = invertedIndex.get(nextwordlcs);
                                ArrayList<Integer> positions = postings.get(i);

                                if(positions == null){
                                    positions = new ArrayList<Integer>();
                                }
                                positions.add(wordIndex);
                                postings.put(i,positions);
                                invertedIndex.put(nextwordlcs,postings);

                            }

                        }

                        wordIndex++;
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


    /* Returns the frequency of the particular term in the given document */
    public int termFrequency(String term, String Doc){

        int docID = this.allDocsHash.get(Doc);

        HashMap<Integer, ArrayList<Integer>> postings = this.invertedIndex.get(term);

        if(postings != null){
            ArrayList<Integer> positions = postings.get(docID);

            if(positions != null){
                return positions.size();
            }
        }

        return 0;
    }


    /* Returns the number of documents in which term appears */
    public int docFrequency(String term){

        HashMap<Integer, ArrayList<Integer>> postings = this.invertedIndex.get(term);

        if(postings != null){
            return postings.size();
        }

        return 0;
    }


    /* Returns string representation of the postings(t) */
    public String postingsList(String t){
        HashMap<Integer, ArrayList<Integer>> postings = this.invertedIndex.get(t);
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        Set<Integer> keys = postings.keySet();
        for(Integer doc : keys){
            sb.append("<");
            sb.append(this.allDocs[doc]);
            sb.append(" : ");
            ArrayList<Integer> positions = postings.get(doc);

            for(Integer pos : positions){
                sb.append(pos);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append(">");
            sb.append(",");
        }

        if(sb.charAt(sb.length()-1) == ','){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("]");

        return sb.toString();
    }



    /* Returns the TPScore of the query with respect to a given document */
    public double TPScore(String query, String doc){

        String[] terms = getTerms(query);
        int l = terms.length;
        int docIndex = this.allDocsHash.get(doc);
        int tpscore = 0;

        if(l == 1) return 0;

        for(int i=0; i<terms.length-1; i++){

            if(invertedIndex.get(terms[i]) == null ||
                    invertedIndex.get(terms[i+1]) == null ||
                    invertedIndex.get(terms[i]).get(docIndex) == null ||
                    invertedIndex.get(terms[i+1]).get(docIndex) == null){
                tpscore += 17;
            }
            else if(Collections.min(invertedIndex.get(terms[i]).get(docIndex)) >= Collections.max(invertedIndex.get(terms[i+1]).get(docIndex))){
                tpscore += 17;
            }
            else{
                ArrayList<Integer> p = invertedIndex.get(terms[i]).get(docIndex);
                ArrayList<Integer> r = invertedIndex.get(terms[i+1]).get(docIndex);

                int min = 17;
                for(int j=0; j<r.size(); j++){
                    for(int k=0; k<p.size(); k++){
                        int a = r.get(j);
                        int b = p.get(k);
                        if(a > b && a-b < min){
                             min = a-b;
                        }
                    }
                }
                tpscore += min;
            }

        }

        return l*1.0/tpscore*1.0;
    }



    /* Returns the VSScore of the query with respect to a given document */
    public double VSScore(String query, String doc){

        String [] allTerms = this.invertedIndex.keySet().toArray(new String[invertedIndex.size()]);
        int N = this.allDocs.length;
        HashMap<String,Integer> queryTermCount = new HashMap<String,Integer>();
        String [] queryTerms = getTerms(query);
        double [] Vq = new double[allTerms.length];
        double [] Vd = new double[allTerms.length];


        for(String qt : queryTerms){
            if(queryTermCount.get(qt) == null){
                Integer count = 1;
                queryTermCount.put(qt,count);
            }
            else{
                Integer count = queryTermCount.get(qt);
                count++;
                queryTermCount.put(qt,count);
            }
        }

        for(int i=0; i<allTerms.length; i++){
            int df_t = this.invertedIndex.get(allTerms[i]).size();

            int tf_q = 0;
            if(queryTermCount.get(allTerms[i]) != null){
                tf_q = queryTermCount.get(allTerms[i]);
            }

            int tf_d = 0;
            if(this.invertedIndex.get(allTerms[i]).get(this.allDocsHash.get(doc)) != null){
                tf_d = this.invertedIndex.get(allTerms[i]).get(this.allDocsHash.get(doc)).size();
            }

            Vq[i] = Math.sqrt(tf_q*1.0)*Math.log10((N*1.0)/(df_t*1.0));
            Vd[i] = Math.sqrt(tf_d*1.0)*Math.log10((N*1.0)/(df_t*1.0));

        }

        return CosineSimilarity(Vq, Vd);
    }




    /* Returns the Relevance of the query with respect to a given document */
    public double Relevance(String query, String doc){

        Double [] scores = new Double[3];
        scores[0] = TPScore(query,doc);
        scores[1] = VSScore(query,doc);
        scores[2] = 0.6 * scores[0] + 0.4 * scores[1];

        this.allScores.put(doc, scores);
        return scores[2];
    }


    /* Helper method to find cosine similarity between two vectors */
    public double CosineSimilarity(double[] V1, double[] V2){
        double dotProd = 0.0;
        double dist1 = 0.0;
        double dist2 = 0.0;
        for (int i = 0; i < V1.length; i++) {
            dotProd += V1[i] * V2[i];
            dist1 += Math.pow(V1[i], 2);
            dist2 += Math.pow(V2[i], 2);
        }
        return dotProd / (Math.sqrt(dist1) * Math.sqrt(dist2));
    }



    /* Get all the preprocessed terms of a query */
    public String[] getTerms(String query) {

        Scanner scanner = new Scanner(query);
        int wordIndex = 0;
        ArrayList<String> termsList = new ArrayList<>();

        while (scanner.hasNext()) {

            String nextword = scanner.next();
            String nextwordlcs = nextword.toLowerCase();

            nextwordlcs = nextwordlcs.replace(",", "")
                    .replace("”", "")
                    .replace("“", "")
                    .replace("?", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("′", "")
                    .replace("'", "")
                    .replace("{", "")
                    .replace("}", "")
                    .replace(":", "")
                    .replace(";", "")
                    .replace("(", "")
                    .replace(")", "");

            String regex = "^\\d*\\.\\d+|\\d+\\.\\d*$";

            if (!nextwordlcs.matches(regex)) {
            } else {
                nextwordlcs = nextwordlcs.replace(".", "");
            }

            if (nextwordlcs.length() > 0) {
                termsList.add(nextwordlcs);
            }

            wordIndex++;
        }

        String[] terms = new String[termsList.size()];

        for(int i=0; i<termsList.size(); i++){
            terms[i] = termsList.get(i);
        }
        return terms;
    }


    public static void main(String[] args) {
        PositionalIndex pi = new PositionalIndex("/Users/harshavk/Desktop/gitrepos/PA3_Docs/IR");

        System.out.println(pi.invertedIndex);
        System.out.println("\n\n");
        System.out.println("Size of inverted index : "+pi.invertedIndex.size());
        double relevance = pi.Relevance("The team's primary color, blue, alludes to the former","Rockland_Boulders.txt");

        System.out.println("Relevance = "+relevance);

    }

}
