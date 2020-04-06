package org.mycompany;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.mycompany.installer.utils.applications.NetBeansRCPUtils;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.wizard.components.WizardComponent;
//normen - JDK launchers
import org.netbeans.installer.utils.system.launchers.LauncherResource;

public class ConfigurationLogic extends ProductConfigurationLogic {

    // constructor //////////////////////////////////////////////////////////////////
    public ConfigurationLogic() throws InitializationException {
    }

    @Override
    public List<WizardComponent> getWizardComponents() {
        return new ArrayList<>();
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();
        //final FilesList filesList = product.getInstalledFiles();
        String appName = ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name");

        if (SystemUtils.isMacOS()) {
            //normen: use parent folder of install dir for icon
            File f = new File(installLocation.getParentFile(), ICON_MACOSX);
            if(!f.exists()) {
                try {
                FileUtils.writeFile(f,
                        ResourceUtils.getResource(ICON_MACOSX_RESOURCE,
                        getClass().getClassLoader()));
                getProduct().getInstalledFiles().add(f);
                } catch (IOException e) {
                    LogManager.log(
                                "... cannot handle icns icon " + f, e); // NOI18N
                }
            }

            //normen: rename executable
            File shortcut = new File(installLocation.getParentFile().getParent() + "/MacOS/executable");
            if(shortcut.exists()){
                try {
                    shortcut.renameTo(new File(installLocation.getParentFile().getParent() + "/MacOS/" + appName));
                    getProduct().getInstalledFiles().add(shortcut.getAbsoluteFile());
                } catch (IOException e) {
                    LogManager.log(
                                "... cannot rename executable " + f, e); // NOI18N
                }
            }

            //normen: replace icon + app in Info.plist
            try {
                File plist=new File(installLocation.getParentFile().getParentFile(), "Info.plist");
                FileUtils.modifyFile(plist, "icon.icns", appName + ".icns");
                FileUtils.modifyFile(plist, "executable", appName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //normen - JDK install - uses package on OSX
        if (!SystemUtils.isMacOS()) {
            File javaHome = new File(System.getProperty("java.home"));
            File target = new File(installLocation, "jdk");
            try {
                FileUtils.copyFile(javaHome, target, true); //FileUtils is one of the NBI core classes, already imported in ConfigurationLogic.java
            } catch (IOException e) {
                throw new InstallationException("Cannot copy JDK",e);
            }
            // set permissions:
            // ADDED BY KIRILL: force correct permissions for JDK files
            LogManager.log("Setting JDK files as executable");
            setExecutableContents(target, "bin");
            setExecutableFile(target, "lib/jexec");
            setExecutableFile(target, "lib/amd64/libjawt.so");
            setExecutableFile(target, "lib/amd64/jli/libjli.so");
            setExecutableFile(target, "lib/visualvm/platform/lib/nbexec");
            // to add uninstaller logic:
            SystemUtils.getNativeUtils().addUninstallerJVM(new LauncherResource(false, target));
        }
    }
    private static void setExecutableContents(File parent, String path) {
        File binDir = new File(parent, path);
        File[] fileList = binDir.listFiles();
        for (File file : fileList) {
            try {
                file.setExecutable(true, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private static void setExecutableFile(File parent, String path) {
        File binFile = new File(parent, path);
        try {
            binFile.setExecutable(true, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void uninstall(Progress progress) throws UninstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();

        //NetBeansUtils.warnNetbeansRunning(installLocation);
        /////////////////////////////////////////////////////////////////////////////
        
        if (Boolean.getBoolean("remove.app.userdir")) {
            try {
                progress.setDetail(getString("CL.uninstall.remove.userdir")); // NOI18N
                LogManager.logIndent("Removing application's userdir... ");
                File userDir = NetBeansRCPUtils.getApplicationUserDirFile(installLocation);
                LogManager.log("... application userdir location : " + userDir);
                if (FileUtils.exists(userDir) && FileUtils.canWrite(userDir)) {
                    FileUtils.deleteFile(userDir, true);
                    FileUtils.deleteEmptyParents(userDir);
                }
                LogManager.log("... application userdir totally removed");
            } catch (IOException e) {
                LogManager.log("Can't remove application userdir", e);
            } finally {
                LogManager.unindent();
            }
        }

        //normen - JDK uninstall
        if (!SystemUtils.isMacOS()) {
            File jre = new File(installLocation, "jdk");
            if (jre.exists()) {
                try {
                    for (File file: FileUtils.listFiles(jre).toList()) {
                        FileUtils.deleteOnExit(file);
                    }
                    FileUtils.deleteOnExit(installLocation);
                } catch (IOException e) {
                    //ignore
                }
            }        
        } else {
            String appName = ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name");
            File exeLink = new File(installLocation.getParentFile().getParent() + "/MacOS/" + appName);
            try {
                FileUtils.deleteWithEmptyParents(exeLink);
            } catch (IOException ex) {
                LogManager.log("Error removing app Link: ", ex);
            }
        }
        /////////////////////////////////////////////////////////////////////////////
        //remove cluster/update files
        /*
        try {
        progress.setDetail(getString("CL.uninstall.update.files")); // NOI18N
        for(String cluster : CLUSTERS) {
        File updateDir = new File(installLocation, cluster + File.separator + "update");
        if ( updateDir.exists()) {
        FileUtils.deleteFile(updateDir, true);
        }
        }
        } catch (IOException e) {
        LogManager.log(
        getString("CL.uninstall.error.update.files"), // NOI18N
        e);
        }
         */
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    @Override
    public String getExecutable() {
        if (SystemUtils.isWindows()) {
            return EXECUTABLE_WINDOWS;
        } else {
            return EXECUTABLE_UNIX;
        }
    }

    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return ICON_WINDOWS;
        } else if (SystemUtils.isMacOS()) {
            return ICON_MACOSX;
        } else {
            return ICON_UNIX;
        }
    }

    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }

    @Override
    public boolean registerInSystem() {
        return true;
    }

    @Override
    public boolean requireLegalArtifactSaving() {
        return false;
    }

    @Override
    public boolean requireDotAppForMacOs() {
        return true;
    }

    @Override
    public boolean wrapForMacOs() {
        return true;
    }
    
    public static final String SHORTCUT_FILENAME =
            ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name") + ".desktop"; // NOI18N
    public static final String[] SHORTCUT_CATEGORIES = 
            ResourceUtils.getString(ConfigurationLogic.class, "CL.app.categories").split(","); // NOI18N
    public static final String BIN_SUBDIR =
            "bin/";
    public static final String EXECUTABLE_WINDOWS =
            BIN_SUBDIR
            + ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name") + (SystemUtils.isCurrentJava64Bit() ? "64" : "") + ".exe"; // NOI18N
    public static final String EXECUTABLE_UNIX =
            BIN_SUBDIR
            + ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name"); // NOI18N
    public static final String ICON_WINDOWS =
            EXECUTABLE_WINDOWS;
    public static final String ICON_UNIX =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.unix.icon.name"); // NOI18N
    public static final String ICON_UNIX_RESOURCE =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.unix.icon.resource"); // NOI18N
    public static final String ICON_MACOSX =
            ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name") + ".icns"; // NOI18N
    public static final String ICON_MACOSX_RESOURCE =
            "org/mycompany/" + ResourceUtils.getString(ConfigurationLogic.class, "CL.app.name") + ".icns"; // NOI18N
}
