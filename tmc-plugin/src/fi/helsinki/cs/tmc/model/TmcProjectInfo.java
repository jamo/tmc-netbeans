package fi.helsinki.cs.tmc.model;

import fi.helsinki.cs.tmc.utilities.zip.RecursiveZipper;
import java.io.File;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Carries information about a project used in TMC.
 */
public class TmcProjectInfo {

    private Project project;

    /*package*/ TmcProjectInfo(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public String getProjectName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    public FileObject getProjectDir() {
        return project.getProjectDirectory();
    }

    public File getProjectDirAsFile() {
        return FileUtil.toFile(getProjectDir());
    }

    public String getProjectDirAbsPath() {
        return FileUtil.toFile(getProjectDir()).getAbsolutePath();
    }

    public boolean isOpen() {
        return OpenProjects.getDefault().isProjectOpen(project);
    }

    public TmcProjectFile getTmcProjectFile() {
        return TmcProjectFile.forProject(FileUtil.toFile(getProjectDir()));
    }

    //TODO: a more robust/elegant/extensible project type recognition system
    public TmcProjectType getProjectType() {
        String pd = getProjectDirAbsPath();
        if (new File(pd + File.separatorChar + "pom.xml").exists()) {
            return TmcProjectType.JAVA_MAVEN;
        } else if (new File(pd + File.separatorChar + "Makefile").exists()) {
            return TmcProjectType.MAKEFILE;
        } else {
            return TmcProjectType.JAVA_SIMPLE;
        }
    }

    public RecursiveZipper.ZippingDecider getZippingDecider() {
        if (getProjectType() == TmcProjectType.JAVA_MAVEN) {
            return new MavenZippingDecider(this);
        } else {
            return new DefaultZippingDecider(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TmcProjectInfo) {
            return this.project.equals((TmcProjectInfo) obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return project.hashCode();
    }

    private abstract static class AbstractZippingDecider implements RecursiveZipper.ZippingDecider {
        protected TmcProjectInfo projectInfo;

        public AbstractZippingDecider(TmcProjectInfo projectInfo) {
            this.projectInfo = projectInfo;
        }

        @Override
        public boolean shouldZip(String zipPath) {
            File dir = new File(projectInfo.getProjectDirAsFile().getParentFile(), zipPath);
            if (!dir.isDirectory()) {
                return true;
            }
            
            return !new File(dir, ".tmcnosubmit").exists();
        }
    }

    private static class DefaultZippingDecider extends AbstractZippingDecider {

        public DefaultZippingDecider(TmcProjectInfo projectInfo) {
            super(projectInfo);
        }

        @Override
        public boolean shouldZip(String zipPath) {
            if (!super.shouldZip(zipPath)) {
                return false;
            }

            if (projectInfo.getTmcProjectFile().getExtraStudentFiles().contains(withoutRootDir(zipPath))) {
                return true;
            } else {
                return zipPath.contains("/src/");
            }
        }

        private String withoutRootDir(String zipPath) {
            int i = zipPath.indexOf('/');
            if (i != -1) {
                return zipPath.substring(i + 1);
            } else {
                return "";
            }
        }
    }

    private static class MavenZippingDecider extends AbstractZippingDecider {

        private static final Pattern rejectPattern = Pattern.compile("^[^/]+/(target|lib/testrunner)/.*");

        public MavenZippingDecider(TmcProjectInfo projectInfo) {
            super(projectInfo);
        }

        @Override
        public boolean shouldZip(String zipPath) {
            if (!super.shouldZip(zipPath)) {
                return false;
            }

            return !rejectPattern.matcher(zipPath).matches();
        }
    }
}
