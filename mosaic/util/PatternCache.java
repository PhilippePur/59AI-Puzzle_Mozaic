package mosaic.util;

import java.util.List;
import java.util.ArrayList;

public class PatternCache {
    private static PatternCache instance;
    
    public static PatternCache getInstance() {
        if (instance == null) instance = new PatternCache();
        return instance;
    }
    
    public List<boolean[][]> getValidPatterns(int value) {
        List<boolean[][]> patterns = new ArrayList<>();
        return patterns;
    }
}