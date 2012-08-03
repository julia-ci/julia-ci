package JuliaLang;

import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * {@link Slave} running on JBox.
 *
 * @author Evgeny Mandrikov
 */
public class JBoxSlave extends Slave {
  private static final Logger LOG = Logger.getLogger(JBoxSlave.class.getName());

  private final String hostName;
  private final String boxName;

  @DataBoundConstructor
  public JBoxSlave(
      String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString,
       RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
      String hostName, String boxName, String virtualMachineType, ComputerLauncher delegateLauncher
  ) throws Descriptor.FormException, IOException {
    super(
        name,
        nodeDescription,
        remoteFS,
        numExecutors,
        mode,
        labelString,
        new JBoxComputerLauncher(delegateLauncher),
        retentionStrategy,
        nodeProperties
    );
    this.hostName = hostName;
    this.boxName = boxName;
  }

  @Override
  public Computer createComputer() {
    return new JBoxComputer(this);
  }

  /**
   * @return host name
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * @return virtual machine name
   */
  public String getBoxName() {
    return boxName;
  }

  @Override
  public JBoxComputerLauncher getLauncher() {
    return (JBoxComputerLauncher) super.getLauncher();
  }

  /**
* For UI.
*
* @return original launcher
*/
  @SuppressWarnings({"UnusedDeclaration"})
  public ComputerLauncher getDelegateLauncher() {
    return getLauncher().getCore();
  }
  
  @Extension
  public static final class DescriptorImpl extends SlaveDescriptor {
    @Override
    public String getDisplayName() {
      return Messages.JBoxSlave_displayName();
    }

    /**
     * For UI.
     *
     * @see JBoxPlugin#getHost(String)
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public List<JBoxMachine> getDefinedBoxes(@QueryParameter String hostName) {
      return JBoxPlugin.getDefinedBoxes(hostName);
    }

    /**
     * For UI.
     *
     * @see JBoxPlugin#getHosts()
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public List<JBoxCloud> getHosts() {
      return JBoxPlugin.getHosts();
    }

    /**
     * For UI.
     * TODO Godin: doesn't work
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public FormValidation doCheckHostName(@QueryParameter String value) {
      LOG.info("Perform on the fly check - hostName");
      if (Util.fixEmptyAndTrim(value) == null) {
        return FormValidation.error("JBox Host is mandatory");
      }
      return FormValidation.ok();
    }

    /**
     * For UI.
     * TODO Godin: doesn't work
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public FormValidation doCheckVirtualMachineName(@QueryParameter String value) {
      LOG.info("Perform on the fly check - virtualMachineName");
      if (Util.fixEmptyAndTrim(value) == null) {
        return FormValidation.error("Box Name is mandatory");
      }
      return FormValidation.ok();
    }
  }

}
