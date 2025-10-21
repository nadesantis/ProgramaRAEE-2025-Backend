package com.products.backup.application;

import java.time.Instant;

public class BackupOptions {
  private boolean includeProducts = true;
  private boolean includeClients  = true;
  private boolean includeOrders   = true;
  private boolean includeUsers    = true;
  private boolean includeAudit    = false;
  private Instant auditFrom;
  private Instant auditTo;

  public boolean isIncludeProducts() { return includeProducts; }
  public boolean isIncludeClients()  { return includeClients; }
  public boolean isIncludeOrders()   { return includeOrders; }
  public boolean isIncludeUsers()    { return includeUsers; }
  public boolean isIncludeAudit()    { return includeAudit; }
  public Instant getAuditFrom()      { return auditFrom; }
  public Instant getAuditTo()        { return auditTo; }

  public BackupOptions setIncludeProducts(boolean v){ this.includeProducts=v; return this; }
  public BackupOptions setIncludeClients(boolean v) { this.includeClients=v;  return this; }
  public BackupOptions setIncludeOrders(boolean v)  { this.includeOrders=v;   return this; }
  public BackupOptions setIncludeUsers(boolean v)   { this.includeUsers=v;    return this; }
  public BackupOptions setIncludeAudit(boolean v)   { this.includeAudit=v;    return this; }
  public BackupOptions setAuditFrom(Instant v)     { this.auditFrom=v;       return this; }
  public BackupOptions setAuditTo(Instant v)       { this.auditTo=v;         return this; }
}
