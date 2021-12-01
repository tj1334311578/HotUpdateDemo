package com.melancholy.hot.update.plugin.launch

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessMultiApkApplicationManifest
import com.melancholy.hot.update.plugin.utils.Logger
import com.melancholy.hot.update.plugin.task.ReplaceManifestTask
import com.melancholy.hot.update.plugin.utils.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

class PluginLaunch implements Plugin<Project> {
    private String variantName

    @Override
    void apply(Project target) {
        def isApp = target.plugins.hasPlugin(AppPlugin.class)
        if (isApp) {
            Logger.make(target)
            Logger.i(Constants.ENABLE_INFO)
            getVariantNameInBuild(target)
            //如果不是一个有效的variant,则直接返回
            if (!isValidVariantName()) {
                return
            }
            addTaskForVariantAfterEvaluate(target)
        }
    }

    /**
     * 获取当前变体名
     * (1)在执行build任务的时候,
     * project.gradle.getStartParameter().getTaskRequests()返回的内容:[DefaultTaskExecutionRequest{args=[:wjplugin:assemble, :wjplugin:testClasses, :manifestplugin:assemble, :manifestplugin:testClasses, :firstplugin:assemble, :firstplugin:testClasses, :app:assembleHuaweiDebug],projectPath='null'}]
     * 可从该字符串中截取当前的variant，然后在该变体基础上创建各个task.
     * (2)在执行sync任务的时候,
     * project.gradle.getStartParameter().getTaskRequests()返回的内容:[DefaultTaskExecutionRequest{args=[],projectPath='null'}]
     * 解决方案:通过project.extensions.findByType(AppExtension.class)找到一个可用的变体(因为会将所有的变体task都加入到任务队列中),将该变体作为变体名来执行完sync任务(仅仅为了完成sync任务,没有任何意义,在执行build任务的时候还会通过{@link #getVariantNameInBuild}替换掉逻辑)
     * 但是最理想的解决方案是该在sync的时候,可以不执行该插件(判断逻辑就是获取的variantName为null的时候,{@link #apply()}直接返回即可)
     *
     * TODO 需要验证在debug release多个变体打包过程
     */
    void getVariantNameInBuild(Project project) {
        String parameter = project.gradle.getStartParameter().getTaskRequests().toString()

        String regex = parameter.contains("assemble") ? "assemble(\\w+)" : "generate(\\w+)"
        Pattern pattern = Pattern.compile(regex)
        Matcher matcher = pattern.matcher(parameter)
        if (matcher.find()) {
            //group（0）就是指的整个串，group（1） 指的是第一个括号里的东西，group（2）指的第二个括号里的东西
            variantName = matcher.group(1)
        }
        //但是sync时返回的内容:[DefaultTaskExecutionRequest{args=[],projectPath='null'}].
        //所以此时走注释中的(2),实现"则直接但是最理想的解决方案是该在sync的时候,可以不执行该插件"这种方案,则直接隐藏下面的代码
        if (!isValidVariantName()) {
            //从AppExtension中获取所有变体,作为获取当前变体的备用方案
            getValidVariantNameFromAllVariant(project)
        }
    }

    /**
     * 获取所有的变体中的一个可用的变体名,仅仅用来保证sync任务可执行而已
     * project.extensions.findByType()有执行时机,所以会出现在getVariantNameInBuild()中直接调用getVariantNameFromAllVariant()将无法更新variantName
     *
     * @param project
     */
    void getValidVariantNameFromAllVariant(Project project) {
        if (isValidVariantName()) {
            return
        }
        //但是sync时返回的内容:[DefaultTaskExecutionRequest{args=[],projectPath='null'}],其实该过程可以不执行该插件也可以
        //直接从所有的变体中取一个可用的变体名,返回
        //
        project.extensions.findByType(AppExtension.class).variantFilter {
            variantName = it.name.capitalize()
            System.out.println(String.format("Fake variant name from all variant is \" %s \"", variantName))
            if (isValidVariantName()) {
                return true
            }
        }
    }

    boolean isValidVariantName() {
        variantName != null && variantName.length() > 0
    }

    /**
     * 在项目配置完成之后添加task
     * @param project
     */
    void addTaskForVariantAfterEvaluate(Project project) {
        //创建一个task
        ReplaceManifestTask task = project.getTasks().create(ReplaceManifestTask.TAG,
                ReplaceManifestTask)
        //在项目配置完成后,添加自定义Task
        project.afterEvaluate {
            //为当前变体的task都加入到这个任务队列中。
            //所以通过project.getTasks().each {}去匹配每个task的startsWith&&endsWith的逻辑是一致的
            //并且这种性能会更高
            //直接通过task的名字找到ProcessApplicationManifest这个task
            //addExportTaskForPackageManifest(project, task)
            addVersionTaskForMergedManifest(project, task)
        }
    }

    /**
     * 为所有依赖的包的AndroidManifest添加android:exported
     * processHuaweiDebugMainManifest:合并所有依赖包以及主module中的AndroidManifest文件
     * processDebugManifest:为所有变体生成最终AndroidManifest文件
     * 不能使用ProcessDebugManifest.因为processHuaweiDebugMainManifest执行的时候就报错,还未执行到ProcessDebugManifest
     * @param project
     */
    void addExportTaskForPackageManifest(Project project, ReplaceManifestTask task) {
        //找到processHuaweiDebugMainManifest
        ProcessApplicationManifest processManifestTask = project.getTasks()
                .getByName(String.format("process%sMainManifest", variantName))
        File manifestFile = processManifestTask.getMainManifest().get()
        task.setManifestFile(manifestFile)
        Logger.w("manifest path ${manifestFile.absolutePath}")
        processManifestTask.dependsOn(task)
    }

    /**
     * 添加处理版本信息的Task
     * @param project
     */
    void addVersionTaskForMergedManifest(Project project, ReplaceManifestTask task) {
        //在项目配置完成后,添加自定义Task
        //方案一:直接通过task的名字找到ProcessMultiApkApplicationManifest这个task
        //直接找到ProcessDebugManifest,然后在执行后之后执行该Task
        ProcessMultiApkApplicationManifest processManifestTask = project.getTasks()
                .getByName(String.format("process%sManifest", variantName))
        File manifestFile = processManifestTask.getMainMergedManifest().asFile.get()
        task.setManifestFile(manifestFile)
        task.setManifestFileOutDirectory(processManifestTask.multiApkManifestOutputDirectory.getAsFile().get())

        Logger.w("manifest path ${manifestFile.absolutePath}  ${processManifestTask.multiApkManifestOutputDirectory.getAsFile().get().absolutePath}")
        processManifestTask.finalizedBy(task)

        //processManifestTask.dependsOn(task)
    }
}