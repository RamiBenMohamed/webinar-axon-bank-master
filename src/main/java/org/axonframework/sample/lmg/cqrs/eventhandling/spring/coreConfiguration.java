package org.axonframework.sample.lmg.cqrs.eventhandling.spring;

/**
 * Created by RAMI on 04/07/2017.
 */
import javax.annotation.PostConstruct;

/*
 *
 * Cette classe chargée par spring, permet de configurer
 * les accès AWS et autres couches d'intermédiation entre le code Appli
 * et l'environnement de AWS
 *
 *
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;

import com.amazonaws.regions.Regions;

@Configuration
@EnableConfigurationProperties(corePropertiesConfiguration.class)
public class coreConfiguration {

    /*
     * Le credential provider qui pourra être utilisé
     *
     */
    private AWSCredentialProviderLMG AWSPlmg;
    private clientConfigurationLMG ccLmg;

    private static final Logger LOGGER = LoggerFactory.getLogger(coreConfiguration.class);

    @Autowired
    private corePropertiesConfiguration coreProperties;


    @PostConstruct
    public void init() {
        AWSCredentials Lcredentials;
        try {

            Lcredentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "------------------>On ne peut pas charger les key AWS du fichier profile. " +
                            "Assurez vous que le fichier existe.  " +
                            "location Unix (~/.aws/credentials), and is in valid format." +
                            "location Windows (c:\\users\\USERNAME\\.aws\\credentials)",
                    e);
        }
        try {
            ccLmg = new clientConfigurationLMG(coreProperties);
            AWSPlmg = new AWSCredentialProviderLMG(Lcredentials,coreProperties,ccLmg);
        } catch ( Exception e)
        {
            throw new AmazonClientException(
                    "------------->Erreur lors de la creation du CredentialProvider",
                    e);
        }
        LOGGER.info("initialisation des clients AWS OK ..............");

    }

    public AWSCredentialsProvider getCredentialProvider()
    {
        return AWSPlmg;
    }
    public ClientConfiguration getClientConfiguration()
    {
        return ccLmg.factory();
    }
    public  Region getRegion() {
        return Region.getRegion(Regions.fromName(coreProperties.getParamRegion()));
    }

    public String getQueueName()
    {
        return coreProperties.getParamQueueName();
    }
    public String getQueueUrl()
    {
        return coreProperties.getParamQueueUrl();
    }

    public void affiche()
    {
        coreProperties.affiche();
    }

    //------------ POUR SNS
    public String getSNSTopic()
    {
        return coreProperties.getParamSNSARN();
    }



}