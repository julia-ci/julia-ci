package JuliaLang;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ComputerLauncher} for JBox that waits for the instance to really come up before processing to
 * the real user-specified {@link ComputerLauncher}.
 * <p>
 * TODO check relaunch during launch
 * </p>
 *
 * @author Keno Fischer
 */
public class JBoxComputerLauncher extends ComputerLauncher {
  private static final Logger LOG = Logger.getLogger(JBoxComputerLauncher.class.getName());

  private static final int SECOND = 1000;
  
  private final ComputerLauncher delegateLauncher;
  
  public JBoxComputerLauncher(ComputerLauncher delegateLauncher) {
      this.delegateLauncher=delegateLauncher;
  }

  @Override
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
    JBoxSlave slave = ((JBoxComputer) computer).getNode();
    log(listener, "Launching node " + slave.getBoxName());
    try {
      // Connect to VirtualBox host
      JBoxMachine machine = JBoxPlugin.getVirtualBoxMachine(slave.getHostName(), slave.getBoxName());
      if (machine == null) {
        listener.fatalError("Unable to find specified machine");
        return;
      }
      log(listener, Messages.JBoxLauncher_startVM(machine));
      long result = JBoxUtils.startVm(machine, listener.getLogger());
      log(listener, Messages.JBoxLauncher_startedVM(machine));
      if (result != 0) {
        listener.fatalError("Unable to launch");
        return;
      }
    } catch (Throwable e) {
      listener.fatalError(e.getMessage(), e);
      e.printStackTrace(listener.getLogger());
      LOG.log(Level.WARNING, e.getMessage(), e);
      return;
    }
    // Stage 2 of the launch. Called after the VirtualBox instance comes up.
    boolean successful = false;
    int attempt = 0;
    while (!successful) {
      attempt++;
      log(listener, "Sleep before stage 2 launcher, attempt " + attempt);
      Thread.sleep(10 * SECOND);
      successful = delegateLaunch(computer, listener);
      if (!successful && attempt > 10) {
        log(listener, "Maximum number of attempts reached");
        return;
      }
    }
  }
  
  /**
* @param computer {@link hudson.model.Computer} for which agent should be launched
* @param listener The progress of the launch, as well as any error, should be sent to this listener.
* @return true, if successfully launched, otherwise false
*/
  protected boolean delegateLaunch(SlaveComputer computer, TaskListener listener) {
    try {
      log(listener, "Starting stage 2 launcher (" + delegateLauncher.getClass().getSimpleName() + ")");
      getCore().launch(computer, listener);
      log(listener, "Stage 2 launcher completed");
      return computer.isOnline();
    } catch (IOException e) {
      log(listener, "Unable to launch: " + e.getMessage());
      return false;
    } catch (InterruptedException e) {
      log(listener, "Unable to launch: " + e.getMessage());
      return false;
    }
  }


  public ComputerLauncher getCore() {
      return this.delegateLauncher;
  }
  
  private static void log(TaskListener listener, String message) {
    listener.getLogger().println("[VirtualBox] " + message);
  }

  @Override
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
    log(listener, "Starting stage 2 beforeDisconnect");
    getCore().beforeDisconnect(computer, listener);
    log(listener, "Stage 2 beforeDisconnect completed");
  }

  @Override
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
    JBoxSlave slave = ((JBoxComputer) computer).getNode();
    // Stage 2 of the afterDisconnect
    log(listener, "Starting stage 2 afterDisconnect");
    getCore().afterDisconnect(computer, listener);
    log(listener, "Stage 2 afterDisconnect completed");

    try {
      // Connect to VirtualBox host
      JBoxMachine machine = JBoxPlugin.getVirtualBoxMachine(slave.getHostName(), slave.getBoxName());
      if (machine == null) {
        listener.fatalError("Unable to find specified machine");
      }
      log(listener, Messages.JBoxLauncher_stopVM(machine));
      long result = JBoxUtils.stopVm(machine, new JBoxTaskListenerLog(listener, "[JBox] "));
      if (result != 0) {
        listener.fatalError("Unable to stop");
      }
    } catch (Throwable e) {
      listener.fatalError(e.getMessage(), e);
    }
  }

  @Override
  public Descriptor<ComputerLauncher> getDescriptor() {
    // Don't allow creation of launcher from UI
    throw new UnsupportedOperationException();
  }
}
