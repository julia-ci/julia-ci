package JuliaLang;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.slaves.Cloud;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * TODO see https://jax-ws.dev.java.net/issues/show_bug.cgi?id=554
 *
 * @author Evgeny Mandrikov
 */
public class JBoxPlugin extends Plugin {

  private static final Logger LOG = Logger.getLogger(JBoxPlugin.class.getName());

  @Override
  public void start() throws Exception {
    LOG.log(Level.INFO, "Starting {0}", getClass().getSimpleName());
    super.start();
  }

  @Override
  public void stop() throws Exception {
    LOG.log(Level.INFO, "Stopping {0}", getClass().getSimpleName());
    super.stop();
    // close VirtualBox WEB sessions
    JBoxUtils.disconnectAll();
  }

  /**
   * @return all registered {@link VirtualBoxCloud}
   */
  public static List<JBoxCloud> getHosts() {
    List<JBoxCloud> result = new ArrayList<JBoxCloud>();
    for (Cloud cloud : Hudson.getInstance().clouds) {
      if (cloud instanceof JBoxCloud) {
        result.add((JBoxCloud) cloud);
      }
    }
    return result;
  }

  /**
   * @param hostName host name
   * @return {@link VirtualBoxCloud} by specified name, null if not found
   */
  public static JBoxCloud getHost(String hostName) {
    if (hostName == null) {
      return null;
    }
    for (JBoxCloud host : getHosts()) {
      if (hostName.equals(host.getDisplayName())) {
        return host;
      }
    }
    return null;
  }

  /**
   * @param hostName host name
   * @return all registered {@link VirtualBoxMachine} from specified host, empty list if unknown host
   */
  public static List<JBoxMachine> getDefinedBoxes(String hostName) {
    JBoxCloud host = getHost(hostName);
    if (host == null) {
      return Collections.emptyList();
    }
    return host.refreshBoxList();
  }

  /**
   * @param hostName           host name
   * @param virtualMachineName virtual machine name
   * @return {@link VirtualBoxMachine} from specified host with specified name, null if not found
   */
  public static JBoxMachine getVirtualBoxMachine(String hostName, String virtualMachineName) {
    if (virtualMachineName == null) {
      return null;
    }
    JBoxCloud host = JBoxPlugin.getHost(hostName);
    if (host == null) {
      return null;
    }
    return host.getBox(virtualMachineName);
  }

  /**
   * For UI.
   */
  @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
  public void doComputerNameValues(StaplerRequest req, StaplerResponse resp, @QueryParameter("hostName") String hostName)
      throws IOException, ServletException {
    ListBoxModel m = new ListBoxModel();
    List<JBoxMachine> virtualMachines = getDefinedBoxes(hostName);
    if (virtualMachines != null && virtualMachines.size() > 0) {
      for (JBoxMachine vm : virtualMachines) {
        m.add(new ListBoxModel.Option(vm.getName(), vm.getName()));
      }
      m.get(0).selected = true;
    }
    m.writeTo(req, resp);
  }

  /**
   * Used for discovering {@link JBoxSlave} with specified MAC Address.
   * HTTP 404 Error will be returned, if slave can't be found.
   * <p>
   * For example: if slave named "virtual" has MAC Adress 080027E852CC, then
   * http://localhost:8080/hudson/plugin/virtualbox/getSlaveAgent?macAddress=080027E852CC
   * redirects to
   * http://localhost:8080/hudson/computer/virtual/slave-agent.jnlp
   * </p>
   *
   * @param req        request
   * @param resp       response
   * @param macAddress MAC Address
   * @throws IOException if something wrong
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public void doGetSlaveAgent(StaplerRequest req, StaplerResponse resp, @QueryParameter("macAddress") String macAddress)
      throws IOException {
   /* LOG.log(Level.INFO, "Searching VirtualBox machine with MacAddress {0}", macAddress);
    for (Node node : Hudson.getInstance().getNodes()) {
      if (node instanceof JBoxSlave) {
        JBoxSlave slave = (JBoxSlave) node;
        JBoxMachine vbox = getVirtualBoxMachine(slave.getHostName(), slave.getVirtualMachineName());

        String vboxMacAddress = JBoxUtils.getMacAddress(vbox, new JBoxSystemLog(LOG, "[VirtualBox] "));
        LOG.log(Level.INFO, "MacAddress for {0} is {1}", new Object[]{slave.getNodeName(), vboxMacAddress});

        if (macAddress.equalsIgnoreCase(vboxMacAddress)) {
          String url = Hudson.getInstance().getRootUrl() + "/computer/" + slave.getNodeName() + "/slave-agent.jnlp";
          LOG.log(Level.INFO, "Found {0} for Mac Address {1}, sending redirect to {2}", new Object[]{slave, macAddress, url});
          resp.sendRedirect(url);
          return;
        }
      }
    }
    resp.sendError(404);*/
  }
}
