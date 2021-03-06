package cn.framework.java;

import cn.framework.common.*;
import cn.framework.entity.CharSequenceJavaFileObject;
import cn.framework.entity.ExecuteResult;
import cn.framework.util.ClassUtils;
import cn.framework.util.FileUtils;
import cn.pp.utils.MD5;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author zhangjinci
 * @version 2016/7/15 11:02
 * @function Java动态编译引擎
 */
public class JavaEngine extends AbstractScriptEngine {

    private static final Logger log = LoggerFactory.getLogger(JavaEngine.class);

    private String savePath = "";
    private String classPath = "";

    private Map<String, JavaContext> cache = new ConcurrentHashMap<>();  //缓存

    private URL[] loaderUrl;

    private static final String JAVA_SUBFFIX = ".java";  //源文件后缀


    public JavaEngine() throws Exception {
        String dirPath = System.getProperty("user.dir");  //获取当前工作目录
        savePath = dirPath.concat("/compiled/");
        classPath = System.getProperty("java.class.path");
        loaderUrl = new URL[]{new URL("file://" + savePath)};
    }

    @Override
    protected String getScriptName() {
        return "java";
    }

    @Override
    public void init(String... contents) throws Exception {

    }

    @Override
    public ScriptContext getScriptContext(String id) {
        return cache.get(id);
    }

    //为了防止本地JVM中有同名的类，应该用sourceCode的内容作为签名，把前面添加到类名
    @Override
    public boolean compile(String id, String sourceCode) throws Exception {
        String name = ClassUtils.getClassNameFromSourceCode(sourceCode);
        String packageName = ClassUtils.getPachageNameFromSourceCode(sourceCode);
        if (StringUtils.isNotEmpty(packageName)) {
            name = packageName + "." + name;
        }
        assert name != null;
        String fileName = savePath + name.replace(".", "/") + JAVA_SUBFFIX;
        File file = new File(fileName);  //创建源文件
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileUtils.writeStringToFile(sourceCode, fileName, "UTF-8");

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        List<JavaFileObject> jfiles = new ArrayList<>();
        jfiles.add(new CharSequenceJavaFileObject(savePath, name, sourceCode));
        StandardJavaFileManager fm = javaCompiler.getStandardFileManager(null, null, null);
        List<String> options = new ArrayList<>();
        options.add("-d");
        options.add(savePath);  //编译后的classes
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(classPath);
        if (log.isDebugEnabled()) {
            log.debug("javac [{}] -> {}", fileName, options.stream().reduce((s, s2) -> s + " " + s2).get());
            log.debug("java Engine start compile\n" + sourceCode);
        }
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fm, diagnosticCollector, options, null, jfiles);
        boolean success = task.call();  //执行编译
        if (success) {
            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(loaderUrl);
            Class<?> clazz = dynamicClassLoader.loadClass(name);
            JavaContext context = new JavaContext(id, MD5.getMD5(sourceCode), clazz);  //封装上下文
            cache.put(id, context);  //写入缓存
            return clazz != null;
        } else {
            StringBuilder builder = new StringBuilder();
            for (Diagnostic<?> diagnostic : diagnosticCollector.getDiagnostics()) {  //写入编译异常信息
                builder.append(diagnostic).append("\n");
            }
            throw new ScriptException(builder.toString());
        }
    }

    @Override
    public ExecuteResult execute(String id, Map<String, Object> params) {
        long startTime = System.currentTimeMillis();
        ExecuteResult result = new ExecuteResult();
        JavaContext context = cache.get(id);
        try {
            if (context != null) {
                handleListenerBefore(context);
                Class<?> clazz = context.getSourceClass();
                StringBuilder consoleStr = new StringBuilder();
                StringBuilder returnStr = new StringBuilder();
                //遍历clazz里面所有方法
                if (clazz != null) {
                    Object instance = clazz.newInstance();
                    Method[] methods = clazz.getDeclaredMethods();
                    for (int i = 0; i < methods.length; i++) {
                        ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
                        PrintStream cacheStream = new PrintStream(bao);
                        System.setOut(cacheStream); //重定向标准输出流
                        returnStr.append(methods[i].invoke(instance, (Object) null));  //尝试收集返回值
                        String message = bao.toString();
                        consoleStr.append(message);  //尝试收集控制台输出值
                        bao.close();
                        cacheStream.close();
                    }
                }
                result.setSuccess(true);
                result.setResult("Console:=> " + consoleStr.toString() + "\tReturn:=> " + returnStr.toString());
                result.setMsg("java engine execute id =>:" + id + " successfully");
            } else {
                result.setSuccess(false);
                result.setResult(null);
                result.setMsg(String.format("class(%s): %s not found!", id, "java"));
            }
        } catch (Exception e) {
            result.setException(e);
        }
        result.setCostTime((System.currentTimeMillis() - startTime));
        handleListenerAfter(context, result);
        return result;
    }

    @Override
    public ExecuteResult execute(String id) {
        return execute(id, new HashMap<>());
    }

    @Override
    public boolean remove(String id) {
        return cache.remove(id) != null;
    }


    //Java编译上下文
    protected class JavaContext extends ScriptContext {
        private Class sourceClass;

        public JavaContext(String id, String sign, Class sourceClass) {
            super(id, sign);
            this.sourceClass = sourceClass;
        }

        public Class getSourceClass() {
            return sourceClass;
        }

    }
}
