package org.axonframework.sample.lmg.cqrs.eventhandling.spring;
/**
 * Created by RAMI on 04/07/2017.
 */
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sqs.eventhandling.spring")
public class corePropertiesConfiguration {

    private String paramAssumedRole;
    private String paramRegion;
    private String paramQueueName;
    private String paramQueueUrl;
    private String paramQueueNameNew;
    private String paramSName;


    private String paramProxyHost ;
    private String paramProxyPort ;
    private String paramProxyUserName ;
    private String paramProxyPassWord ;


    private String paramSNSARN ;

    public String getParamAssumedRole() {
        return paramAssumedRole;
    }


    public void setParamAssumedRole(String paramAssumedRole) {
        this.paramAssumedRole = paramAssumedRole;
    }


    public String getParamRegion() {
        return paramRegion;
    }


    public void setParamRegion(String paramRegion) {
        this.paramRegion = paramRegion;
    }


    public String getParamQueueName() {
        return paramQueueName;
    }


    public void setParamQueueName(String paramQueueName) {
        this.paramQueueName = paramQueueName;
    }


    public String getParamQueueUrl() {
        return paramQueueUrl;
    }


    public void setParamQueueUrl(String paramQueueUrl) {
        this.paramQueueUrl = paramQueueUrl;
    }


    public String getParamQueueNameNew() {
        return paramQueueNameNew;
    }


    public void setParamQueueNameNew(String paramQueueNameNew) {
        this.paramQueueNameNew = paramQueueNameNew;
    }


    public String getParamSName() {
        return paramSName;
    }


    public void setParamSName(String paramSName) {
        this.paramSName = paramSName;
    }


    public String getParamProxyHost() {
        return paramProxyHost;
    }


    public void setParamProxyHost(String paramProxyHost) {
        this.paramProxyHost = paramProxyHost;
    }


    public String getParamProxyPort() {
        return paramProxyPort;
    }


    public void setParamProxyPort(String paramProxyPort) {
        this.paramProxyPort = paramProxyPort;
    }


    public String getParamProxyUserName() {
        return paramProxyUserName;
    }


    public void setParamProxyUserName(String paramProxyUserName) {
        this.paramProxyUserName = paramProxyUserName;
    }


    public String getParamSNSARN() {
        return paramSNSARN;
    }


    public void setParamSNSARN(String paramSNSARN) {
        this.paramSNSARN = paramSNSARN;
    }


    public String getParamProxyPassWord() {
        return paramProxyPassWord;
    }


    public void setParamProxyPassWord(String paramProxyPassWord) {
        this.paramProxyPassWord = paramProxyPassWord;
    }


    public void affiche()
    {

        System.out.println("----------------------x----------------------");
        System.out.println("param_assumed_role->" + paramAssumedRole);
        System.out.println("param_region->" + paramRegion);
        System.out.println("param_sName->" + paramSName);
        System.out.println("param_queue_name->" + paramQueueName);
        System.out.println("param_queue_url->" + paramQueueUrl);
        System.out.println("param_queue_name_new->" + paramQueueNameNew) ;
        System.out.println("--------------------------------------------");
        System.out.println("param_proxyHost->" + paramProxyHost) ;
        System.out.println("param_proxyPort->" + paramProxyPort) ;
        System.out.println("param_proxyUserName->" + paramProxyUserName) ;
        System.out.println("param_proxyPassWord->" + paramProxyPassWord) ;
        System.out.println("--------------------------------------------");
        System.out.println("paramSNSARN->" + paramSNSARN) ;
        System.out.println("--------------------------------------------");

    }
}
