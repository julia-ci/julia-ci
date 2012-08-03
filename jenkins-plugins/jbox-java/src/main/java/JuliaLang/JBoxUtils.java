package JuliaLang;


import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author Mihai Serban
 */
public final class JBoxUtils {

  // public methods
  public static long startVm(JBoxMachine machine, OutputStream log) {
    return runJboxCommand(machine.getHost(), log, "worker up "+machine.getName());
    //return getVboxControl(machine.getHost(), log).startVm(machine, virtualMachineType, log);
  }

  public static long stopVm(JBoxMachine machine, JBoxLogger log) {
    return 0;
    //eturn getVboxControl(machine.getHost(), log).stopVm(machine, log);
  }

  public static List<JBoxMachine> getMachines(JBoxCloud host) {
    StringWriter out = new StringWriter();
    runJboxCommand(host, out, "worker list");
    String[] names = out.getBuffer().toString().split("\\n");
    List<JBoxMachine> list = new ArrayList<JBoxMachine>();
    for(String name: names) {
        list.add(new JBoxMachine(host, name));
    }
    return list;
  }

  private static long runJboxCommand(JBoxCloud host, Writer stream, String command)
  {
       return host.getServer().executeCommand("bash -l -c '"+host.getConfigDir()+"/jbox "+command+"'", stream);        
  }
  
  private static long runJboxCommand(JBoxCloud host, OutputStream stream, String command)
  {
       return host.getServer().executeCommand("bash -l -c '"+host.getConfigDir()+"/jbox "+command+"'", stream);        
  }
  
  public static String getMacAddress(JBoxMachine machine, JBoxLogger log) {
    return ""; //return getVboxControl(machine.getHost(), log).getMacAddress(machine, log);
  }

  public static void disconnectAll() {

  }

  // private methods
  private JBoxUtils() {
  }

  /**
   * Cache connections to VirtualBox hosts
   * TODO: keep the connections alive with a noop
   */
  /*
  private static HashMap<String, VirtualBoxControl> vboxControls = new HashMap<String, VirtualBoxControl>();

  private synchronized static VirtualBoxControl getVboxControl(VirtualBoxCloud host, JBoxLogger log) {
    VirtualBoxControl vboxControl = (VirtualBoxControl)vboxControls.get(host.toString());
    if (null != vboxControl) {
      if (vboxControl.isConnected()) {
        return vboxControl;
      }
      log.logInfo("Lost connection to " + host.getUrl() + ", reconnecting");
      vboxControls.remove(host.toString()); // force a reconnect
    }
    vboxControl = createVboxControl(host, log);

    vboxControls.put(host.toString(), vboxControl);
    return vboxControl;
  }

  private static VirtualBoxControl createVboxControl(VirtualBoxCloud host, JBoxLogger log) {
    VirtualBoxControl vboxControl = null;

    log.logInfo("Trying to connect to " + host.getUrl() + ", user " + host.getUsername());
    IWebsessionManager manager = new IWebsessionManager(host.getUrl());
    IVirtualBox vbox = manager.logon(host.getUsername(), host.getPassword());
    String version = vbox.getVersion();
    manager.disconnect(vbox);

    log.logInfo("Creating connection to VirtualBox version " + version);
    if (version.startsWith("4.1")) {
      vboxControl = new VirtualBoxControlV41(host.getUrl(), host.getUsername(), host.getPassword());
    } else if (version.startsWith("4.0")) {
      vboxControl = new VirtualBoxControlV40(host.getUrl(), host.getUsername(), host.getPassword());
    } else if (version.startsWith("3.")) {
      vboxControl = new VirtualBoxControlV31(host.getUrl(), host.getUsername(), host.getPassword());
    } else {
      log.logError("VirtualBox version " + version + " not supported.");
      throw new UnsupportedOperationException("VirtualBox version " + version + " not supported.");
    }

    log.logInfo("Connected to VirtualBox version " + version + " on host " + host.getUrl());
    return vboxControl;
  }
  */
}
