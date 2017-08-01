package org.axonframework.sample.lmg.cqrs.eventhandling.spring;

/**
 * Created by RAMI on 04/07/2017.
 */
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;



public class AWSCredentialProviderLMG implements AWSCredentialsProvider {


   // private static final Logger LOGGER = LoggerFactory.getLogger(AWSCredentialProviderLMG.class);

    // Credentials long terme fourni par les admins
    AWSCredentials Lcredentials = null;
    // Credential temporaire
    AWSCredentials Tcredentials = null;
    //
    clientConfigurationLMG cCLMG = null;


    public AWSCredentialProviderLMG()
    {

    }


    public AWSCredentialProviderLMG(AWSCredentials awsCre, corePropertiesConfiguration propertie,clientConfigurationLMG cC)
    {
        Lcredentials = awsCre;
        cCLMG = cC;
        ClientConfiguration clientC = cC.factory();
        AWSSecurityTokenServiceClient stsClient = new  AWSSecurityTokenServiceClient(Lcredentials,clientC);


       // LOGGER.info("Creation du client STS ..............");
        propertie.affiche();
        //
        AssumeRoleRequest assumeRequest = new AssumeRoleRequest()
                .withRoleArn(propertie.getParamAssumedRole())
                .withDurationSeconds(3600)
                .withRoleSessionName(propertie.getParamSName()

                );
    /*    LOGGER.info("Exec requête pour le role ..............");
        LOGGER.info("Exec requ�te pour le role ..............");
        LOGGER.info("ATTENTION SI ON PLANTE ICI C EST PARCE QUE ");
        LOGGER.info("LES VALEURS DE CONNEXION SONT FAUSSES ");*/

        //
        AssumeRoleResult assumeResult = stsClient.assumeRole(assumeRequest);
        //LOGGER.info("Creation du nouveau credential  ..............");
        Tcredentials =
                new BasicSessionCredentials(
                        assumeResult.getCredentials().getAccessKeyId(),
                        assumeResult.getCredentials().getSecretAccessKey(),
                        assumeResult.getCredentials().getSessionToken());
    }

    @Override
    public AWSCredentials getCredentials() {
        // TODO Auto-generated method stub
        return Tcredentials;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }


}