package se.danetest.shibboleth.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;
import edu.internet2.middleware.shibboleth.common.config.security.*;

public class DaneExplicitKeySignatureTrustEngineNamespaceHandler extends BaseSpringNamespaceHandler {
	
	private final Logger log = LoggerFactory.getLogger(DaneExplicitKeySignatureTrustEngineNamespaceHandler.class);

	public static final String NAMESPACE = "urn:mace:danetest:danetrustengine";

    public void init() {
    	log.debug("[DaneExtension] Register the DaneSignatureTrustEngineBeanDefinitionParser.");
    	
    	registerBeanDefinitionParser(DaneStaticExplicitKeySignatureTrustEngineBeanDefinitionParser.SCHEMA_TYPE, 
    									new DaneStaticExplicitKeySignatureTrustEngineBeanDefinitionParser());
    	
    	log.debug("[DaneExtension] DaneExplicitKeySignatureTrustEngineNamespaceHandler has been registered. ");
    	
    	/***
    	 * COPY PASTE BELOW ~ Christoffer Holmstedt 2012-05-16
    	 */
    	registerBeanDefinitionParser(FilesystemX509CredentialBeanDefinitionParser.SCHEMA_TYPE,
                new FilesystemX509CredentialBeanDefinitionParser());

        registerBeanDefinitionParser(InlineX509CredentialBeanDefinitionParser.SCHEMA_TYPE,
                new InlineX509CredentialBeanDefinitionParser());

        registerBeanDefinitionParser(FilesystemBasicCredentialBeanDefinitionParser.SCHEMA_TYPE,
                new FilesystemBasicCredentialBeanDefinitionParser());

        registerBeanDefinitionParser(InlineBasicCredentialBeanDefinitionParser.SCHEMA_TYPE,
                new InlineBasicCredentialBeanDefinitionParser());

        registerBeanDefinitionParser(FilesystemPKIXValidationInformationBeanDefinitionParser.SCHEMA_TYPE,
                new FilesystemPKIXValidationInformationBeanDefinitionParser());

        registerBeanDefinitionParser(InlinePKIXValidationInformationBeanDefinitionParser.SCHEMA_TYPE,
                new InlinePKIXValidationInformationBeanDefinitionParser());
        
        BeanDefinitionParser pkixOptionsParser = new PKIXValidationOptionsBeanDefinitionParser();
        registerBeanDefinitionParser(PKIXValidationOptionsBeanDefinitionParser.ELEMENT_NAME, pkixOptionsParser);
        registerBeanDefinitionParser(PKIXValidationOptionsBeanDefinitionParser.SCHEMA_TYPE, pkixOptionsParser);
        
        registerBeanDefinitionParser(CertPathPKIXValidationOptionsBeanDefinitionParser.SCHEMA_TYPE,
                new CertPathPKIXValidationOptionsBeanDefinitionParser());

        BeanDefinitionParser parser = new ShibbolethSecurityPolicyBeanDefinitionParser();
        registerBeanDefinitionParser(ShibbolethSecurityPolicyBeanDefinitionParser.ELEMENT_NAME, parser);
        registerBeanDefinitionParser(ShibbolethSecurityPolicyBeanDefinitionParser.SCHEMA_TYPE, parser);

        registerBeanDefinitionParser(ChainingTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new ChainingTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(ChainingSignatureTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new ChainingSignatureTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(MetadataExplicitKeyTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new MetadataExplicitKeyTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(MetadataPKIXX509CredentialTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new MetadataPKIXX509CredentialTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(MetadataExplicitKeySignatureTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new MetadataExplicitKeySignatureTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(MetadataPKIXSignatureTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new MetadataPKIXSignatureTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(StaticExplicitKeyTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new StaticExplicitKeyTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(StaticExplicitKeySignatureTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new StaticExplicitKeySignatureTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(StaticPKIXX509CredentialTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new StaticPKIXX509CredentialTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(StaticPKIXSignatureTrustEngineBeanDefinitionParser.SCHEMA_TYPE,
                new StaticPKIXSignatureTrustEngineBeanDefinitionParser());

        registerBeanDefinitionParser(ClientCertAuthRuleBeanDefinitionParser.SCHEMA_TYPE,
                new ClientCertAuthRuleBeanDefinitionParser());

        registerBeanDefinitionParser(MandatoryMessageAuthenticationRuleBeanDefinitionParser.SCHEMA_TYPE,
                new MandatoryMessageAuthenticationRuleBeanDefinitionParser());
        
    	log.debug("[DaneExtension] All beans has been parsed.");

    	/***
    	 * COPY PASTE ABOVE
    	 */
    }
}
