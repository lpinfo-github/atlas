package com.taobao.android.builder.manager;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.FeatureExtension;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.api.LibraryVariantOutput;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.api.LibVariantContext;
import com.android.build.gradle.internal.api.LibraryVariantImpl;
import com.android.builder.core.AtlasBuilder;
import com.android.utils.ILogger;
import com.taobao.android.builder.extension.AtlasExtension;
import com.taobao.android.builder.extension.TBuildType;
import com.taobao.android.builder.tasks.library.AwbGenerator;
import com.taobao.android.builder.tasks.library.JarExtractTask;
import com.taobao.android.builder.tools.ideaplugin.AwoPropHandler;
import org.apache.commons.lang.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Zip;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * AtlasFeatureTaskManager
 *
 * @author zhayu.ll
 * @date 18/1/3
 * @time 下午8:15
 * @description  
 */
public class AtlasFeatureTaskManager extends AtlasBaseTaskManager {

    private FeatureExtension featureExtension;

    private ILogger logger = LoggerWrapper.getLogger(AtlasFeatureTaskManager.class);

    public AtlasFeatureTaskManager(AtlasBuilder androidBuilder, BaseExtension androidExtension, Project project, AtlasExtension atlasExtension) {
        super(androidBuilder, androidExtension, project, atlasExtension);
        this.featureExtension = (FeatureExtension) androidExtension;
    }

    @Override
    public void runTask() {

        if (featureExtension.getBaseFeature()) {
            return;
        }


        featureExtension.getFeatureVariants().forEach(featureVariant -> {




        });

        featureExtension.getLibraryVariants().forEach(libraryVariant -> {

            LibVariantContext libVariantContext = new LibVariantContext((LibraryVariantImpl) libraryVariant,
                    project,
                    atlasExtension,
                    featureExtension);

            TBuildType tBuildType = libVariantContext.getBuildType();
            if (null != tBuildType) {
                try {
                    new AwoPropHandler().process(tBuildType,
                            atlasExtension.getBundleConfig());
                } catch (Exception e) {
                    throw new GradleException("process awo exception", e);
                }
            }

            AwbGenerator awbGenerator = new AwbGenerator(atlasExtension);

            Collection<BaseVariantOutput> list = libVariantContext.getBaseVariant().getOutputs();

            if (null != list) {

                for (BaseVariantOutput libVariantOutputData : list) {

                    Zip zipTask = ((LibraryVariantOutput) (libVariantOutputData)).getPackageLibrary();

                    if (atlasExtension.getBundleConfig().isJarEnabled()) {
                        new JarExtractTask().generateJarArtifict(zipTask);
                    }

                    //Build the awb and extension
//                    if (atlasExtension.getBundleConfig().isAwbBundle()) {
                    awbGenerator.generateAwbArtifict(zipTask, libVariantContext);
//                    }

                    if (null != tBuildType && (StringUtils.isNotEmpty(tBuildType.getBaseApDependency())
                            || null != tBuildType.getBaseApFile()) &&

                            libraryVariant.getName().equals("debug")) {

                        atlasExtension.getTBuildConfig().setUseCustomAapt(true);

                        libVariantContext.setBundleTask(zipTask);

                        try {

                            libVariantContext.setAwbBundle(awbGenerator.createAwbBundle(libVariantContext));
                        } catch (IOException e) {
                            throw new GradleException("set awb bundle error");
                        }

                    }

                }


            }

        });


    }

}
