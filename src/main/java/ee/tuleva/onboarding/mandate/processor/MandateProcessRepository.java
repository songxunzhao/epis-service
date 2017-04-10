package ee.tuleva.onboarding.mandate.processor;

import org.springframework.data.repository.CrudRepository;

public interface MandateProcessRepository extends CrudRepository<MandateProcess, Long> {

    public MandateProcess findOneByProcessId(String processId);

}
