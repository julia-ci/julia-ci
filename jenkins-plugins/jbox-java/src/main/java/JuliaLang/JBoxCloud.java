package JuliaLang;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.Scrambler;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import org.jvnet.hudson.plugins.SSHSite;

/**
 * {@link Cloud} implementation for JBox.
 *
 * @author Keno Fischer
 */
public class JBoxCloud extends Cloud {

  private static final Logger LOG = Logger.getLogger(JBoxCloud.class.getName());

  private final JBoxHost server;
  private final String configDir;

  /**
   * Lazily computed list of virtual machines from this host.
   */
  private transient List<JBoxMachine> jBoxMachines = null;

  public JBoxCloud(String displayName, JBoxHost server, String configDir) {
    super(displayName);
    this.server = server;
    this.configDir = configDir;
  }
  
  @DataBoundConstructor
  public JBoxCloud(String displayName, String hostname, String port, String username, String pass, String keyfile, String configDir) {
      super(displayName);
      this.server=new JBoxHost(hostname, port, username, pass, keyfile);
      this.configDir = configDir;
  }

  @Override
  public Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
    return Collections.emptyList();
  }

  @Override
  public boolean canProvision(Label label) {
    return false;
  }

  public synchronized List<JBoxMachine> refreshBoxList() {
    jBoxMachines = JBoxUtils.getMachines(this);
    return jBoxMachines;
  }

  public synchronized JBoxMachine getBox(String boxName) {
    if (null == jBoxMachines) {
      refreshBoxList();
    }
    for (JBoxMachine machine: jBoxMachines) {
      if (boxName.equals(machine.getName())) {
        return machine;
      }
    }
    return null;
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<Cloud> {
    @Override
    public String getDisplayName() {
      return Messages.JBoxHost_displayName();
    }

    /**
     * For UI.
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    public FormValidation doTestConnection(
        @QueryParameter String hostname,
        @QueryParameter String port,
        @QueryParameter String username,
        @QueryParameter String pass
    ) {
      LOG.log(Level.INFO, "Testing connection to {0} with username {1}", new Object[]{hostname, username});
      try {
        JBoxHost site = new JBoxHost(hostname,port,username,pass);
        site.testConnection();
        return FormValidation.ok(Messages.JBoxHost_success());
      } catch (Throwable e) {
        return FormValidation.error(e.getMessage());
      }
    }
  }
  
  public final String getHostname() {
      return this.server.hostname;
  }
  
  public final String getPort() {
      return ""+this.server.port;
  }
  
  public final String getUsername() {
      return this.server.username;
  }
  
  public final String getPass() {
      return this.server.password;
  }
  
  public JBoxHost getServer() {
      return this.server;
  }
  
  public final String getConfigDir() {
      return this.configDir;
  }
  
  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("VirtualBoxHost");
    sb.append("{host='").append(server.toString()).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
