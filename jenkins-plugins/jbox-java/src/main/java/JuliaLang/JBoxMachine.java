package JuliaLang;

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Evgeny Mandrikov
 */
public class JBoxMachine implements Serializable, Comparable<JBoxMachine> {

  private final JBoxCloud host;
  private final String name;

  @DataBoundConstructor
  public JBoxMachine(JBoxCloud host, String name) {
    this.host = host;
    this.name = name;
  }

  public JBoxCloud getHost() {
    return host;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JBoxMachine)) {
      return false;
    }
    JBoxMachine that = (JBoxMachine) obj;
    if (host != null ? !host.equals(that.host) : that.host != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    return 31 * result + (host != null ? host.hashCode() : 0);
  }

  public int compareTo(JBoxMachine obj) {
    // TODO Godin compare host ? check on null?
    return name.compareTo(obj.getName());
  }

  @Override
  public String toString() {
    return new StringBuffer()
        .append("VirtualBoxMachine{")
        .append("host=").append(host).append(",")
        .append("name='").append(name).append("'")
        .append("}").toString();
  }
}
