package JuliaLang;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Evgeny Mandrikov
 */
public class JBoxBuildWrapper extends BuildWrapper {
  private String hostName;
  private String virtualMachineName;

  @DataBoundConstructor
  public JBoxBuildWrapper(String hostName, String boxName) {
    super();
    this.hostName = hostName;
    this.virtualMachineName = boxName;
  }

  @Override
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    JBoxMachine machine = JBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
    listener.getLogger().println(Messages.JBoxLauncher_startVM(machine));
 //   JBoxUtils.startVm(machine, "headless", new JBoxTaskListenerLog(listener, "[VirtualBox] ")); // TODO type

    class EnvironmentImpl extends Environment {
      @Override
      public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        JBoxMachine machine = JBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
        listener.getLogger().println(Messages.JBoxLauncher_stopVM(machine));
        JBoxUtils.stopVm(machine, new JBoxTaskListenerLog(listener, "[JBox] "));
        return true;
      }
    }

    return new EnvironmentImpl();
  }

  public String getHostName() {
    return hostName;
  }

  public String getVirtualMachineName() {
    return virtualMachineName;
  }

  // TODO enable wrapper
//  @Extension
//  public static final class DescriptorImpl extends Descriptor<BuildWrapper> {
//    @Override
//    public String getDisplayName() {
//      return Messages.VirtualBoxBuildWrapper_displayName();
//    }
//
//    /**
//     * For UI.
//     *
//     * @see VirtualBoxPlugin#getHost(String)
//     */
//    @SuppressWarnings({"UnusedDeclaration"})
//    public List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
//      return VirtualBoxPlugin.getDefinedVirtualMachines(hostName);
//    }
//
//    /**
//     * For UI.
//     *
//     * @see VirtualBoxPlugin#getHosts()
//     */
//    @SuppressWarnings({"UnusedDeclaration"})
//    public List<VirtualBoxHost> getHosts() {
//      return VirtualBoxPlugin.getHosts();
//    }
//  }
}
