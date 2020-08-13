package site.kason.kalang.intellij;

import com.intellij.lang.Language;

/**
 * @author KasonYang
 */
public class KalangLanguage extends Language {

    public static final KalangLanguage INSTANCE = new KalangLanguage();

    protected KalangLanguage() {
        super("Kalang");
    }
}
