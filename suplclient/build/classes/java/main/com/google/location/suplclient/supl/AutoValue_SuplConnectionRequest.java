
package com.google.location.suplclient.supl;

import javax.annotation.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
 final class AutoValue_SuplConnectionRequest extends SuplConnectionRequest {

  private final String serverHost;
  private final int serverPort;
  private final boolean sslEnabled;
  private final boolean loggingEnabled;
  private final boolean messageLoggingEnabled;

  private AutoValue_SuplConnectionRequest(
      String serverHost,
      int serverPort,
      boolean sslEnabled,
      boolean loggingEnabled,
      boolean messageLoggingEnabled) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.sslEnabled = sslEnabled;
    this.loggingEnabled = loggingEnabled;
    this.messageLoggingEnabled = messageLoggingEnabled;
  }

  @Override
  public String getServerHost() {
    return serverHost;
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public boolean isSslEnabled() {
    return sslEnabled;
  }

  @Override
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  @Override
  public boolean isMessageLoggingEnabled() {
    return messageLoggingEnabled;
  }

  @Override
  public String toString() {
    return "SuplConnectionRequest{"
         + "serverHost=" + serverHost + ", "
         + "serverPort=" + serverPort + ", "
         + "sslEnabled=" + sslEnabled + ", "
         + "loggingEnabled=" + loggingEnabled + ", "
         + "messageLoggingEnabled=" + messageLoggingEnabled
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SuplConnectionRequest) {
      SuplConnectionRequest that = (SuplConnectionRequest) o;
      return (this.serverHost.equals(that.getServerHost()))
           && (this.serverPort == that.getServerPort())
           && (this.sslEnabled == that.isSslEnabled())
           && (this.loggingEnabled == that.isLoggingEnabled())
           && (this.messageLoggingEnabled == that.isMessageLoggingEnabled());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.serverHost.hashCode();
    h *= 1000003;
    h ^= this.serverPort;
    h *= 1000003;
    h ^= this.sslEnabled ? 1231 : 1237;
    h *= 1000003;
    h ^= this.loggingEnabled ? 1231 : 1237;
    h *= 1000003;
    h ^= this.messageLoggingEnabled ? 1231 : 1237;
    return h;
  }

  static final class Builder extends SuplConnectionRequest.Builder {
    private String serverHost;
    private Integer serverPort;
    private Boolean sslEnabled;
    private Boolean loggingEnabled;
    private Boolean messageLoggingEnabled;
    Builder() {
    }
    @Override
    public SuplConnectionRequest.Builder setServerHost(String serverHost) {
      if (serverHost == null) {
        throw new NullPointerException("Null serverHost");
      }
      this.serverHost = serverHost;
      return this;
    }
    @Override
    public SuplConnectionRequest.Builder setServerPort(int serverPort) {
      this.serverPort = serverPort;
      return this;
    }
    @Override
    public SuplConnectionRequest.Builder setSslEnabled(boolean sslEnabled) {
      this.sslEnabled = sslEnabled;
      return this;
    }
    @Override
    public SuplConnectionRequest.Builder setLoggingEnabled(boolean loggingEnabled) {
      this.loggingEnabled = loggingEnabled;
      return this;
    }
    @Override
    public SuplConnectionRequest.Builder setMessageLoggingEnabled(boolean messageLoggingEnabled) {
      this.messageLoggingEnabled = messageLoggingEnabled;
      return this;
    }
    @Override
    public SuplConnectionRequest build() {
      String missing = "";
      if (this.serverHost == null) {
        missing += " serverHost";
      }
      if (this.serverPort == null) {
        missing += " serverPort";
      }
      if (this.sslEnabled == null) {
        missing += " sslEnabled";
      }
      if (this.loggingEnabled == null) {
        missing += " loggingEnabled";
      }
      if (this.messageLoggingEnabled == null) {
        missing += " messageLoggingEnabled";
      }
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_SuplConnectionRequest(
          this.serverHost,
          this.serverPort,
          this.sslEnabled,
          this.loggingEnabled,
          this.messageLoggingEnabled);
    }
  }

}
