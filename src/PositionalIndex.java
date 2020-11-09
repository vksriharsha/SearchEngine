import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class PositionalIndex {

    private String folderName;
    private HashMap<String, HashMap<Integer, ArrayList<Integer>>> invertedIndex;
    private String[] allDocs;
    private HashMap<String, Integer> allDocsHash;

    public PositionalIndex(String folderName){
        this.folderName = folderName;
        this.invertedIndex = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
        preprocess();
    }

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

    public int docFrequency(String term){

        HashMap<Integer, ArrayList<Integer>> postings = this.invertedIndex.get(term);

        if(postings != null){
            return postings.size();
        }

        return 0;
    }

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

    public int TPScore(String query, String doc){
        return 0;
    }

    public int VSScore(String query, String doc){
        return 0;
    }

    public int Relevance(String query, String doc){
        return 0;
    }


    public static void main(String[] args) {
        PositionalIndex pi = new PositionalIndex("/Users/harshavk/Desktop/gitrepos/PA3_Docs/IR");

        System.out.println(pi.invertedIndex);
        System.out.println("\n\n");
        System.out.println(pi.docFrequency("flea"));
        System.out.println(pi.termFrequency("reform—a", pi.allDocs[1167]));
        System.out.println(pi.invertedIndex.get("reform—a"));

        System.out.println(pi.postingsList("flew"));

    }

}
