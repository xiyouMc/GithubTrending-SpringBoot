package com.xiyoumc.bean;

public class ServerErrorBean extends BaseBean{

  public static ServerErrorBean newInstance(int errorCode, String errorMsg) {
    ServerErrorBean serverErrorBean = new ServerErrorBean();
    serverErrorBean.errorCode = errorCode;
    serverErrorBean.errorMsg = errorMsg;
    return serverErrorBean;
  }

  private int errorCode;
  private String errorMsg;

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }
}
