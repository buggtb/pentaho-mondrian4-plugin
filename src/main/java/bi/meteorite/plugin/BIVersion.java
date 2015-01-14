package bi.meteorite.plugin;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by bugg on 13/01/15.
 */

@XmlRootElement
public class BIVersion {
  private String version;

  public BIVersion() {
  }

  public BIVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
