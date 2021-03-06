package cn.zjcscut.compile;

/**
 * @author zhangjinci
 * @version 2016/7/13 17:05
 * @function
 */

import java.io.IOException;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class JavaStringObject extends SimpleJavaFileObject {
    private String code;

    public JavaStringObject(String name, String code) {
        //super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        super(URI.create(name + ".java"), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return code;
    }
}
