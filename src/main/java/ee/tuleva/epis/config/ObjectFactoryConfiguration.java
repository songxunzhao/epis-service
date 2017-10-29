package ee.tuleva.epis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class ObjectFactoryConfiguration {

  @Component
  public static class MhubEnvelopeFactory extends mhub.xsd.envelope._01.ObjectFactory {
  }

  @Component
  public static class EnvelopeHeadFactory extends iso.std.iso._20022.tech.xsd.head_001_001.ObjectFactory {
  }

  @Component
  public static class XRoadFactory extends ee.x_road.xsd.x_road.ObjectFactory {
  }

  @Component
  public static class SoapEnvelopeFactory extends org.xmlsoap.schemas.soap.envelope.ObjectFactory {
  }

  @Component
  public static class EpisMessageFactory extends ee.x_road.epis.producer.ObjectFactory {
  }

}
