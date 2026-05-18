package xiaogj.xzagent.model;

/**
 * 远程工具风险等级。
 *
 * <p>风险等级用于表达工具对远程系统的影响范围。后续接入审批、
 * 更细粒度权限控制或审计策略时，可以直接基于该枚举分流处理。
 */
public enum RemoteToolRiskLevel {
    READ,
    WRITE,
    DANGEROUS
}

