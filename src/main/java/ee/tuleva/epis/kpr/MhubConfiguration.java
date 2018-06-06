package ee.tuleva.epis.kpr;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.net.ssl.SSLContext;
import java.security.Security;


@Configuration
@Slf4j
public class MhubConfiguration {

    @Value("${mhub.host}")
    private String host;

    @Value("${mhub.port}")
    private int port;

    @Value("${mhub.queueManager}")
    private String queueManager;

    @Value("${mhub.channel}")
    private String channel;

    @Value("${mhub.peerName}")
    private String peerName;

    @Value("${mhub.inboundQueue}")
    private String inboundQueue;

    @Value("${mhub.outboundQueue}")
    private String outboundQueue;

    @Value("${mhub.trustStore}")
    private String trustStore;

    @Value("${mhub.trustStorePassword}")
    private String trustStorePassword;

    @Value("${mhub.keyStore}")
    private String keyStore;

    @Value("${mhub.keyStorePassword}")
    private String keyStorePassword;

    @Value("${mhub.userid}")
    private String userid;

    @Value("${mhub.password}")
    private String password;

    @Bean
    public MQQueueConnectionFactory createMQConnectionFactory() {
        // it requires SSLv3 to be enabled but integrated with some app that has already brought up the JCA provider
        // it may be too late for that here - do it earlier
        Security.setProperty("jdk.tls.disabledAlgorithms", "");

        SSLContext sslContext = KeyUtils.createSSLContext(
                keyStore,
                keyStorePassword,
                trustStore,
                trustStorePassword);

        try {
            MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
            factory.setSSLSocketFactory(sslContext.getSocketFactory());
            factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            factory.setHostName(this.host);
            factory.setPort(this.port);
            factory.setQueueManager(this.queueManager);
            factory.setChannel(this.channel);
            factory.setCCSID(WMQConstants.CCSID_UTF8);
            // only cipher that works
            factory.setSSLCipherSuite("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
            factory.setSSLPeerName(this.peerName);
            factory.setSSLFipsRequired(false);
            
            factory.setStringProperty(WMQConstants.USERID, userid);
            factory.setStringProperty(WMQConstants.PASSWORD, password);
            factory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);

            return factory;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }


    @Bean
    public JmsTemplate createJmsTemplate(MQQueueConnectionFactory factory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        SingleConnectionFactory singleConnectionFactory = new SingleConnectionFactory();
        singleConnectionFactory.setTargetConnectionFactory(factory);
        jmsTemplate.setConnectionFactory(singleConnectionFactory);
        jmsTemplate.setDefaultDestinationName(this.outboundQueue);
        jmsTemplate.setMessageConverter(messageConverter());
        return jmsTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        MarshallingMessageConverter converter = new MarshallingMessageConverter();
        converter.setMarshaller(marshaller());
        converter.setUnmarshaller(marshaller());
        return converter;
    }

    @Bean
    Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setSchemas(
            new ClassPathResource("epis-wsdl/old_soap_envelope.xsd"),
            new ClassPathResource("epis-wsdl/epis.xsd"),
            new ClassPathResource("epis-wsdl/head.001.001.01.xsd"),
            new ClassPathResource("epis-wsdl/mhub.xsd"),
            new ClassPathResource("epis-wsdl/x-road.xsd"));
        marshaller.setPackagesToScan(
                "org.xmlsoap.schemas.soap.envelope",
                "ee.x_road",
                "mhub.xsd.envelope._01",
                "iso.std.iso._20022.tech.xsd.head_001_001");
        return marshaller;
    }

    @Bean
    public DefaultMessageListenerContainer createMessageListenerContainer(MQQueueConnectionFactory factory,
                                                                          MessageListener messageListener) {
        log.info("MessageListener found in Spring context, creating DefaultMessageListenerContainer too.");
        DefaultMessageListenerContainer defaultMessageListenerContainer = new DefaultMessageListenerContainer();
        defaultMessageListenerContainer.setConnectionFactory(factory);
        defaultMessageListenerContainer.setDestinationName(this.inboundQueue);
        defaultMessageListenerContainer.setRecoveryInterval(4000); // todo
        defaultMessageListenerContainer.setMessageListener(messageListener);
        return defaultMessageListenerContainer;
    }


}
