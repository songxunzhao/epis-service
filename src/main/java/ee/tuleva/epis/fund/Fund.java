package ee.tuleva.epis.fund;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Fund {

  private String isin;
  private String name;
  private String shortName;
  private int pillar;
  private FundStatus status;

  public enum FundStatus {
    ACTIVE("A"), // Aktiivne
    LIQUIDATED("L"), // Likvideeritud
    SUSPENDED("P"), // Peatatud
    CONTRIBUTIONS_FORBIDDEN("S"), // Sissemaksed keelatud
    PAYOUTS_FORBIDDEN("V"); // Väljamaksed keelatud

    private String status;

    FundStatus(String status) {
      this.status = status;
    }

    public static FundStatus parse(String status) {
      for (FundStatus fundStatus : values()) {
        if (fundStatus.status.equalsIgnoreCase(status)) {
          return fundStatus;
        }
      }
      return null;
    }
  }
}