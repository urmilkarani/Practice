/**
 * Implement an autocomplete system. That is, given a query string s and a set of all possible query strings,
 * return all strings in the set that have s as a prefix.
 *
 * For example, given the query string de and the set of strings [dog, deer, deal], return [deer, deal].
 */
package dailycodingproblems;


class TrieNodeContainer {
    static final int ALPHABET_SIZE = 26;
     public TrieNodeContainer[] trieNodes = new TrieNodeContainer[ALPHABET_SIZE];
     public boolean isEndOfWord;
}
public class DailyCodingProblem11 {
    public static void main (String[] args) {
        TrieNodeContainer start = new TrieNodeContainer();
        insert(start, "hello");
        insert(start, "hallo");
        insert(start, "afternoon");
        insert(start, "alter");
        insert(start, "afterall");
        insert(start, "break");
        insert(start, "breakdown");
        insert(start, "california");
        insert(start, "callous");
        System.out.println(search(start,"hello"));
        System.out.println(search(start, "halo"));
        printAutoSuggestions(start, "");
    }

    private static void printAutoSuggestions(TrieNodeContainer start, String substring) {
        for(char character : substring.toCharArray()) {
            start = start.trieNodes[(int)character-97];
        }
        autoSuggestions(start,substring);
    }

    private static void autoSuggestions(TrieNodeContainer start, String s) {
        if(start == null){
            return;
        }
        if(start.isEndOfWord){
            System.out.println(s);
        }
        for(int i = 0; i < 26; i++) {
            if(start.trieNodes[i] != null) {
                autoSuggestions(start.trieNodes[i],s+(char)(i+97));
            }
        }
    }

    public static void insert(TrieNodeContainer node, String word) {
        for(char character : word.toCharArray()) {
            if(node.trieNodes[(int)character-97] == null) {
                node.trieNodes[(int)character-97] = new TrieNodeContainer();
            }
            node = node.trieNodes[(int)character-97];
        }
        node.isEndOfWord = true;
    }

    public static boolean search(TrieNodeContainer node, String word) {
        if(node == null || word.isEmpty()) {
            return false;
        }
        for(char character : word.toCharArray()) {
            if(node.trieNodes[(int)character-97] == null) {
                return false;
            }
            node = node.trieNodes[(int)character-97];
        }
        return node != null && node.isEndOfWord;
    }
}
