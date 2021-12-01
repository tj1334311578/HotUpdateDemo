package com.melancholy.hot.update.plugin.task


import com.melancholy.hot.update.plugin.utils.Logger
import com.melancholy.hot.update.plugin.utils.Constants
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 执行任务
 */
class ReplaceManifestTask extends DefaultTask {
    final static String TAG = "ReplaceManifestTask"
    private File mManifestFile
    private File mManifestOutDirectory

    public void setManifestFile(File manifestFile) {
        mManifestFile = manifestFile
    }

    public void setManifestFileOutDirectory(File manifestOutDirectory) {
        mManifestOutDirectory = new File(manifestOutDirectory, Constants.MANIFEST_FILE_NAME)
    }

    /**
     * 具体执行任务的方法
     */
    @TaskAction
    void doTaskAction() {
        Logger.i("start ReplaceManifestTask")
        XmlParser xmlParser = new XmlParser()
        def node = xmlParser.parse(mManifestFile)
        addOrReplaceApplicationName(node)
        String result = XmlUtil.serialize(node)
        mManifestOutDirectory.write(result, "utf-8")
        Logger.i("finish ReplaceManifestTask")
    }

    private static void addOrReplaceApplicationName(Node node) {
        for (Node childNode: node.children()) {
            if(childNode.name() == "application") {
                def iterator = childNode.attributes().iterator()
                while (iterator.hasNext()) {
                    def entry = iterator.next()
                    if(entry.key.toString().endsWith("name")) {
                        def applicationName = entry.value
                        //替换application中的android:name
                        childNode.attributes().replace(entry.key, Constants.REPLACE_APPLICATION_NAME)
                        Logger.w("replace application android:name")
                        def attributes = new HashMap<String, String>()
                        attributes.put("android:name", Constants.META_DATA_NAME)
                        attributes.put("android:value", applicationName)
                        new Node(childNode, "meta-data", attributes)
                        return
                    }
                }
                //没有添加android:name属性
                childNode.attributes().put("android:name", Constants.REPLACE_APPLICATION_NAME)
                Logger.w("add application android:name")
                return
            }
        }
    }
}