package JuliaLang;

import hudson.model.Slave;
import hudson.slaves.SlaveComputer;
import java.io.IOException;
import org.kohsuke.stapler.HttpResponse;

/**
 * @author Evgeny Mandrikov
 */
public class JBoxComputer extends SlaveComputer {
  public JBoxComputer(Slave slave) {
    super(slave);
  }

  @Override
  public JBoxSlave getNode() {
    return (JBoxSlave) super.getNode();
  }

  @Override
  public HttpResponse doDoDelete() throws IOException {
    // TODO powerOff on delete
    return super.doDoDelete();
  }
}
