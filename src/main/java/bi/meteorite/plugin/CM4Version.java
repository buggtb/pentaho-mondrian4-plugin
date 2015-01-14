package bi.meteorite.plugin;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by bugg on 14/01/15.
 */
@XmlRootElement
public class CM4Version {
  private String version;

  public CM4Version() {
  }

  public CM4Version(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
