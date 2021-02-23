<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-02-05 15:56:53
 * @LastEditTime : 2021-02-05 15:57:43
 * @Copyright 2021 SUPERZHC
-->
# 通过 ApplicationID 监控 Yarn 的状态

```java
ApplicationReport report = yarnClient.getApplicationReport(appId);

LOG.info("Got application report from ASM for" //
        + ", appId=" + appId.getId() //
        + ", clientToAMToken=" + report.getClientToAMToken() //
        + ", appDiagnostics=" + report.getDiagnostics() //
        + ", appMasterHost=" + report.getHost() //
        + ", appQueue=" + report.getQueue() //
        + ", appMasterRpcPort=" + report.getRpcPort()//
        + ", appStartTime=" + report.getStartTime() //
        + ", yarnAppState=" + report.getYarnApplicationState().toString() //
        + ", distributedFinalState=" + report.getFinalApplicationStatus().toString() //
        + ", appTrackingUrl=" + report.getTrackingUrl()//
        + ", appUser=" + report.getUser());

YarnApplicationState state = report.getYarnApplicationState();
FinalApplicationStatus jbossStatus = report.getFinalApplicationStatus();
if (YarnApplicationState.FINISHED == state) {
    if (FinalApplicationStatus.SUCCEEDED == jbossStatus) {
        LOG.info("Application has completed successfully. Breaking monitoring loop");
        return true;
    }
    else {
        LOG.info("Application did finished unsuccessfully." + " YarnState=" + state.toString()
                + ", JBASFinalStatus=" + jbossStatus.toString() + ". Breaking monitoring loop");
        return false;
    }
}
else if (YarnApplicationState.KILLED == state || YarnApplicationState.FAILED == state) {
    LOG.info("Application did not finish." + " YarnState=" + state.toString() + ", JBASFinalStatus="
            + jbossStatus.toString() + ". Breaking monitoring loop");
    return false;
}
```