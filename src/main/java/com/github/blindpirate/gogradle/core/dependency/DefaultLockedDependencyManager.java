package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.parse.NotationParser;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import java.util.Optional;
import org.gradle.api.Project;
import org.gradle.api.internal.DynamicObjectUtil;
import org.gradle.api.invocation.Gradle;
import org.gradle.internal.metaobject.GetPropertyResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.CollectionUtils.flatten;
import static org.codehaus.groovy.runtime.InvokerHelper.EMPTY_ARGS;
import static org.codehaus.groovy.runtime.InvokerHelper.invokeMethod;

@Singleton
public class DefaultLockedDependencyManager implements LockedDependencyManager {

    private static final String GRADLE_EXT_LOCK = "gradle.ext.lock";
    private final Project project;
    private final NotationParser notationParser;

    // gradle.ext.lock=[[:],[:]]
    private static final String EXT = "ext";
    private static final String LOCK = "lock";
    private static final String SETTINGS_FILE = "settings.gradle";

    @Inject
    public DefaultLockedDependencyManager(Project project, NotationParser notationParser) {
        this.project = project;
        this.notationParser = notationParser;
    }

    @Override
    public Optional<GolangDependencySet> getLockedDependencies() {
        Gradle gradle = project.getGradle();
        GetPropertyResult ext = getDynamicProperty(gradle, EXT);
        GetPropertyResult lock = getDynamicProperty(ext.getValue(), LOCK);
        if (!lock.isFound()) {
            return Optional.empty();
        }

        if (!(lock.getValue() instanceof List)) {
            return Optional.empty();
        }

        GolangDependencySet result = parseList((List<Map<String, Object>>) lock.getValue());

        return Optional.of(result);
    }

    private GolangDependencySet parseList(List<Map<String, Object>> lock) {
        GolangDependencySet ret = new GolangDependencySet();
        for (Map<String, Object> notaion : lock) {
            ret.add(notationParser.parse(notaion));
        }
        return ret;
    }


    private GetPropertyResult getDynamicProperty(Object target, String propertyName) {
        GetPropertyResult result = new GetPropertyResult();
        DynamicObjectUtil.asDynamicObject(target).getProperty(propertyName, result);
        return result;
    }

    @Override
    public void lock(GolangDependencySet flatDependencies) {
        List<Map<String, String>> notations = toNotations(flatDependencies);
        writeToSettingsDotGradle(notations);
    }

    private void writeToSettingsDotGradle(List<Map<String, String>> notations) {
        File file = project.getRootDir().toPath().resolve(SETTINGS_FILE).toFile();
        IOUtils.touch(file);
        String fileContent = IOUtils.toString(file);
        List<String> lines = StringUtils.splitToLines(fileContent);
        int startLineIndex = findStartLineIndex(lines);
        List<String> contentBeforeLock = lines.subList(0, startLineIndex);
        List<String> lockContent = toString(notations);
        List<String> resultFileContent = flatten(contentBeforeLock, lockContent);
        IOUtils.write(file, StringUtils.join(resultFileContent, "\n"));
    }

    // TODO should be more strict
    private List<String> toString(List<Map<String, String>> notations) {
        List<String> result = new ArrayList<>();
        result.add("gradle.ext.lock=// The following lines are auto-generated by gogradle,"
                + " you should NEVER modify them manually.");
        result.add("[");
        for (Map<String, String> notation : notations) {
            result.add(toString(notation) + ",");
        }
        result.add("]");
        return result;
    }

    private String toString(Map<String, String> notation) {
        return invokeMethod(notation, "inspect", EMPTY_ARGS).toString();
//        if (notation.isEmpty()) {
//            return "[:]\n";
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        for (Map.Entry<String, String> entry : notation.entrySet()) {
//            sb.append("'").append(entry.getKey()).append("'").append(":");
//            sb.append("'").append(entry.getValue()).append("'").append(",");
//        }
//        sb.append("]");
//        return sb.toString();
    }

    // return start line index or lines.size()
    private int findStartLineIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (line != null && line.trim().startsWith(GRADLE_EXT_LOCK)) {
                return i;
            }
        }
        return lines.size();
    }

    private List<Map<String, String>> toNotations(GolangDependencySet flatDependencies) {
        List<Map<String, String>> ret = new ArrayList<>();
        for (GolangDependency dependency : flatDependencies) {
            if (dependency instanceof LockEnabled) {
                LockEnabled lockEnabled = (LockEnabled) dependency;
                ret.add(lockEnabled.toLockNotation());
            }
        }
        return ret;
    }
}
