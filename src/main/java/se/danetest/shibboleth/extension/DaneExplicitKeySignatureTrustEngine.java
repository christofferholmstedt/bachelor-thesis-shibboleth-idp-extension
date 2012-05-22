package se.danetest.shibboleth.extension;

/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.KeyAlgorithmCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.trust.ExplicitKeyTrustEvaluator;
import org.opensaml.xml.security.trust.TrustedCredentialTrustEngine;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.BaseSignatureTrustEngine;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link SignatureTrustEngine} which evaluates the validity and trustworthiness of XML and raw
 * signatures.
 * 
 * <p>
 * Processing is first performed as described in {@link BaseSignatureTrustEngine}. If based on this processing, it is
 * determined that the Signature's KeyInfo is not present or does not contain a resolveable valid (and trusted) signing
 * key, then all trusted credentials obtained by the trusted credential resolver will be used to attempt to validate the
 * signature.
 * </p>
 */
public class DaneExplicitKeySignatureTrustEngine extends BaseSignatureTrustEngine<Iterable<Credential>> implements
        TrustedCredentialTrustEngine<Signature> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DaneExplicitKeySignatureTrustEngine.class);

    /** Resolver used for resolving trusted credentials. */
    private CredentialResolver credentialResolver;

    /** The external explicit key trust engine to use as a basis for trust in this implementation. */
    private ExplicitKeyTrustEvaluator keyTrust;

    /**
     * Constructor.
     * 
     * @param resolver credential resolver used to resolve trusted credentials.
     * @param keyInfoResolver KeyInfo credential resolver used to obtain the (advisory) signing credential from a
     *            Signature's KeyInfo element.
     */
    public DaneExplicitKeySignatureTrustEngine(CredentialResolver resolver, KeyInfoCredentialResolver keyInfoResolver) {
    	super(keyInfoResolver);
        log.debug("[DaneExtension] 'Super' constructor has completeted, keyInfoResolver = {}", keyInfoResolver);
        if (resolver == null) {
        	log.debug("[DaneExtension] resolver == 0");
            throw new IllegalArgumentException("Credential resolver may not be null");
        }
        log.debug("[DaneExtension] before credentialResolver = resolver");
        credentialResolver = resolver;
        log.debug("[DaneExtension] credentialResolver = resolver = {}", resolver);
        keyTrust = new ExplicitKeyTrustEvaluator();
        log.debug("[DaneExtension] New ExplicitKeyTrustEvaluator has been instantiated, keyTrust = {}", keyTrust);
    }

    /** {@inheritDoc} */
    public CredentialResolver getCredentialResolver() {
    	log.debug("[DaneExtension] returning credentialResolver = {}", credentialResolver);
        return credentialResolver;
    }

    /** {@inheritDoc} */
    public boolean validate(Signature signature, CriteriaSet trustBasisCriteria) throws SecurityException {
    	log.debug("[DaneExtension] validating with signature = {} and trustBasisCriteria = {}", signature, trustBasisCriteria);
        checkParams(signature, trustBasisCriteria);
        log.debug("[DaneExtension] signature = {} and trustBasisCriteria = {} parameters checked", signature, trustBasisCriteria);
        CriteriaSet criteriaSet = new CriteriaSet();
        log.debug("[DaneExtension] created new CriteriaSet called criteriaSet = {}", criteriaSet);
        criteriaSet.addAll(trustBasisCriteria);
        log.debug("[DaneExtension] added trustBasisCriteria to criteriaSet");
        if (!criteriaSet.contains(UsageCriteria.class)) {
        	log.debug("[DaneExtension] criteriaSet does not contain UsageCriteria.class");
            criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
            log.debug("[DaneExtension] added new UsageCriteria to criteriaSet");
        }
        /**
         * Some added to code to dig into the signature object.
         * Christoffer Holmstedt 2012-05-16
         */
        KeyInfo testKey = signature.getKeyInfo();
        log.debug("[DaneExtension] TestKey.getID {}, ", testKey.getID());
        
        /**
         * End of added code.
         */
        String jcaAlgorithm = SecurityHelper.getKeyAlgorithmFromURI(signature.getSignatureAlgorithm());
        log.debug("[DaneExtension] SecurityHelper has fetched jcaAlgorithm(KeyAlgorithm from URI) = {} with Signature Algorithm", jcaAlgorithm);
        if (!DatatypeHelper.isEmpty(jcaAlgorithm)) {
        	log.debug("[DaneExtension] DatatypeHelper does not contain jcaAlgorithm");
            criteriaSet.add(new KeyAlgorithmCriteria(jcaAlgorithm), true);
            log.debug("[DaneExtension] added new KeyAlgorithmCirteria(jcaAlgoritm) = {} to criteriaSet", jcaAlgorithm);
        }

        Iterable<Credential> trustedCredentials = getCredentialResolver().resolve(criteriaSet);
        log.debug("[DaneExtension] trustedCredentials is set to getCredentialResolver().resolve(criteriaSet) = {}", getCredentialResolver().resolve(criteriaSet));
        if (validate(signature, trustedCredentials)) {
        	log.debug("[DaneExtension] signature = {} and trustedCredentials = {} is validated, returns true", signature, trustBasisCriteria);
            return true;
        }

        // If the credentials extracted from Signature's KeyInfo (if any) did not verify the
        // signature and/or establish trust, as a fall back attempt to verify the signature with
        // the trusted credentials directly.
        log.debug("[DaneExtension] Attempting to verify signature using trusted credentials");

        /**
         * Some added to code to dig into the trustedCredentials Iterable object.
         * Christoffer Holmstedt 2012-05-16
         */
        log.debug("[DaneExtension] ---------------------");
        for (Credential trustedCredential : trustedCredentials) {
            log.debug("[DaneExtension] TEST {}", trustedCredential);
        }
        log.debug("[DaneExtension] ---------------------");
        /**
         * End of added code.
         */
        for (Credential trustedCredential : trustedCredentials) {
            log.debug("[DaneExtension] for(Credentials trustedCredential : trustedCredentials), signature = {}, trustedCredential = {}", signature, trustedCredential);
            if (verifySignature(signature, trustedCredential)) {
                log.debug("[DaneExtension] Successfully verified signature = {} using resolved trustedCredential = {}", signature, trustedCredentials);
                return true;
            }
        }
        log.debug("[DaneExtension] Failed to verify signature using either KeyInfo-derived or directly trusted credentials, return false");
        return false;
    }

    /** {@inheritDoc} */
    public boolean validate(byte[] signature, byte[] content, String algorithmURI, CriteriaSet trustBasisCriteria,
            Credential candidateCredential) throws SecurityException {
       
    	log.debug("[DaneExtension] validating");
        checkParamsRaw(signature, content, algorithmURI, trustBasisCriteria);
        log.debug("[DaneExtension] parameters signature = {}, content = {}, algorithmURI = {} and trustBasisCriteria = {} checked");
        CriteriaSet criteriaSet = new CriteriaSet();
        log.debug("[DaneExtension] created new CriteriaSet called criteriaSet = {}", criteriaSet);
        criteriaSet.addAll(trustBasisCriteria);
        log.debug("[DaneExtension] added trustBasisCreteria to criteriaSet");
        if (!criteriaSet.contains(UsageCriteria.class)) {
        	log.debug("[DaneExtension] criteriaSet does not contain UsageCriteria.class");
            criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
            log.debug("[DaneExtension] added new UsageCriteria to criteriaSet");
        }
        String jcaAlgorithm = SecurityHelper.getKeyAlgorithmFromURI(algorithmURI);
        log.debug("[DaneExtension] SecurityHelper has fetched jcaAlgorithm(KeyAlgorithm from URI) = {} with algorithmURI", jcaAlgorithm);
        if (!DatatypeHelper.isEmpty(jcaAlgorithm)) {
        	log.debug("[DaneExtension] DatatypeHelper does not contain jcaAlgorithm");
            criteriaSet.add(new KeyAlgorithmCriteria(jcaAlgorithm), true);
            log.debug("[DaneExtension] added new KeyAlgorithmCirteria(jcaAlgoritm) to criteriaSet");
        }

        Iterable<Credential> trustedCredentials = getCredentialResolver().resolve(criteriaSet);
        log.debug("[DaneExtension] trustedCredentials is set to getCredentialResolver().resolve(criteriaSet)=",trustedCredentials);

        // First try the optional supplied candidate credential
        if (candidateCredential != null) {
        	 log.debug("[DaneExtension] candidateCredentials != null");

            if (SigningUtil.verifyWithURI(candidateCredential, algorithmURI, signature, content)) {
                log.debug("[DaneExtension] Successfully verified signature using supplied candidate credential = {}, algorithmURO = {}, signature = {}, content = {}");
                log.debug("[DaneExtension] Attempting to establish trust of supplied candidate credential");
                if (evaluateTrust(candidateCredential, trustedCredentials)) {
                    log.debug("[DaneExtension]  Successfully established trustedCredentials = {} of supplied candidateCredential = {}, return true", trustedCredentials, candidateCredential);
                    return true;
                } else {
                    log.debug("[DaneExtension] Failed to established trustedCredentials = {} of supplied candidateCredential = {}", trustedCredentials, candidateCredential);
                }
            }
        }

        // If the candidate verification credential did not verify the
        // signature and/or establish trust, or if no candidate was supplied,
        // as a fall back attempt to verify the signature with the trusted credentials directly.
        log.debug("[DaneExtension] Attempting to verify signature using trusted credentials");

        for (Credential trustedCredential : trustedCredentials) {
        	log.debug("[DaneExtension] for(Credentials trustedCredential : trustedCredentials)");
            if (SigningUtil.verifyWithURI(trustedCredential, algorithmURI, signature, content)) {
                log.debug("[DaneExtension] Successfully verified signature = {} using resolved trustedCredentials = {}, algorithmURI = {}, content = {}, return true");
                return true;
            }
        }
        log.debug("[DaneExtension] Failed to verify signature using either supplied candidate credential"
                + " or directly trusted credentials, return false");
        return false;
    }

    /** {@inheritDoc} */
    protected boolean evaluateTrust(Credential untrustedCredential, Iterable<Credential> trustedCredentials)
            throws SecurityException {
         log.debug("[DaneExtension]  evaluateTrust(Credential untrustedCredential = {}, Iterable<Credential> trustedCredentials = {})", untrustedCredential, trustedCredentials);
        return keyTrust.validate(untrustedCredential, trustedCredentials);
    }
}
