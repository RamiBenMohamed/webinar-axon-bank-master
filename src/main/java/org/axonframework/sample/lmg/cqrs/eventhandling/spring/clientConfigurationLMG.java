package org.axonframework.sample.lmg.cqrs.eventhandling.spring;

/**
 * Created by RAMI on 04/07/2017.
 */

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;



public class clientConfigurationLMG {
    boolean proxyMode = false;
  //  private static final Logger LOGGER = LoggerFactory.getLogger(clientConfigurationLMG.class);
    ClientConfiguration clientConfig;

    private String sportP = null;
    private int portP = 0;
    private String hostP = null;
    private String userP = null;
    private String pwdP = null;

    public clientConfigurationLMG(boolean pm)
    {
        proxyMode = pm;
    }

    /*
     * Ce constructeur va permettre de passer en mode proxy ou non de manière
     * Automatique
     * C'est la valeur du Host qui est utilisé pour ce switch
     * Si Host est à valeur NULL ( chaine explicite) alors le proxy est annulé
     *
     * Ceci est utile pour Unix AWS pour lequel on a pas besoin de proxy
     * com.lmg.paramProxyHost=NULL
        com.lmg.paramProxyPort=NULL
        com.lmg.paramProxyUserName=NULL
        com.lmg.paramProxyPassWord=NULL
     *
     *
     */
    public clientConfigurationLMG(corePropertiesConfiguration propertie)
    {

        if ( propertie != null)
        {
            //propertie.affiche();
            if ( !propertie.getParamProxyHost().equals(null) &&  ! propertie.getParamProxyHost().equals("NULL" ) )
            {
                hostP = propertie.getParamProxyHost();
                proxyMode = true;
            }
            if ( !propertie.getParamProxyPort().equals(null) &&  !propertie.getParamProxyPort().equals("NULL" )  )
            {
                sportP = propertie.getParamProxyPort();
                portP = Integer.parseInt(sportP);
            }
            if ( !propertie.getParamProxyUserName().equals(null) &&  !propertie.getParamProxyUserName().equals("NULL" ))
            {
                userP = propertie.getParamProxyUserName();
            }
            if ( !propertie.getParamProxyPassWord().equals(null) &&   !propertie.getParamProxyPassWord().equals("NULL" ) )
            {
                pwdP = propertie.getParamProxyPassWord();
            }
        }
    }
    private boolean isProxyMode()
    {
        return proxyMode;
    }

    public void basculerHTTP()
    {
        clientConfig.setProtocol(Protocol.HTTP);
    }
    public void basculerHTTPS()
    {
        clientConfig.setProtocol(Protocol.HTTPS);
    }

    public ClientConfiguration factory()
    {
        clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);

        if (isProxyMode()) {
       /*     LOGGER.info("CLIENT EN MODE PROXY --> ");
            LOGGER.info("ProxyHost : " + hostP + "  ProxyPort : " + portP);
            LOGGER.info("UserName  : " + userP );*/
            clientConfig.setProxyHost(hostP);
            clientConfig.setProxyPort(portP);
            if (userP != null && userP.length() > 0)  clientConfig.setProxyUsername(userP);
            if (pwdP != null && pwdP.length() > 0)    clientConfig.setProxyPassword(pwdP);
        } else {
          //  LOGGER.info("CLIENT SANS PROXY --> ");
        }
        return clientConfig;
    }
}