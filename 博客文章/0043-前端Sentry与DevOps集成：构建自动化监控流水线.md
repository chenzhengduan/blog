# 前端Sentry与DevOps集成：构建自动化监控流水线

在现代软件开发中，DevOps实践已成为提升开发效率和产品质量的关键。将Sentry监控系统与DevOps流水线深度集成，可以实现自动化的错误监控、性能分析和质量保障。本文将详细介绍如何构建一个完整的Sentry DevOps集成方案。

## 1. DevOps集成架构设计

### 1.1 集成架构概览

```javascript
// DevOps集成架构管理器
class SentryDevOpsIntegration {
  constructor(options = {}) {
    this.options = {
      environment: 'production', // 环境标识
      release: null, // 发布版本
      deploymentStage: 'deploy', // 部署阶段
      cicdProvider: 'github', // CI/CD提供商
      notificationChannels: ['slack', 'email'], // 通知渠道
      qualityGates: {
        errorRate: 0.01, // 错误率阈值 1%
        performanceScore: 80, // 性能分数阈值
        coverageThreshold: 0.8 // 覆盖率阈值 80%
      },
      ...options
    };
    
    this.pipeline = new Map();
    this.stages = new Map();
    this.metrics = new Map();
    this.notifications = new Map();
    
    this.init();
  }
  
  init() {
    this.setupPipelineStages();
    this.initializeMetricsCollection();
    this.setupNotificationChannels();
    this.registerEventHandlers();
  }
  
  setupPipelineStages() {
    // 定义流水线阶段
    const stages = [
      {
        name: 'build',
        description: '构建阶段',
        actions: ['compile', 'test', 'analyze'],
        sentryActions: ['setup-release', 'upload-sourcemaps']
      },
      {
        name: 'test',
        description: '测试阶段',
        actions: ['unit-test', 'integration-test', 'e2e-test'],
        sentryActions: ['error-tracking', 'performance-monitoring']
      },
      {
        name: 'deploy',
        description: '部署阶段',
        actions: ['deploy-staging', 'deploy-production'],
        sentryActions: ['deployment-tracking', 'release-health']
      },
      {
        name: 'monitor',
        description: '监控阶段',
        actions: ['health-check', 'performance-check', 'error-check'],
        sentryActions: ['real-time-monitoring', 'alerting']
      },
      {
        name: 'feedback',
        description: '反馈阶段',
        actions: ['collect-metrics', 'generate-reports', 'notify-team'],
        sentryActions: ['metrics-analysis', 'quality-assessment']
      }
    ];
    
    stages.forEach(stage => {
      this.stages.set(stage.name, new PipelineStage(stage));
    });
  }
  
  initializeMetricsCollection() {
    this.metricsCollector = new DevOpsMetricsCollector({
      sentryDsn: this.options.sentryDsn,
      environment: this.options.environment,
      release: this.options.release
    });
  }
  
  setupNotificationChannels() {
    this.options.notificationChannels.forEach(channel => {
      switch (channel) {
        case 'slack':
          this.notifications.set('slack', new SlackNotifier({
            webhookUrl: this.options.slackWebhook,
            channel: this.options.slackChannel
          }));
          break;
        case 'email':
          this.notifications.set('email', new EmailNotifier({
            smtpConfig: this.options.smtpConfig,
            recipients: this.options.emailRecipients
          }));
          break;
        case 'teams':
          this.notifications.set('teams', new TeamsNotifier({
            webhookUrl: this.options.teamsWebhook
          }));
          break;
      }
    });
  }
  
  registerEventHandlers() {
    // 注册流水线事件处理器
    this.on('stage:start', this.handleStageStart.bind(this));
    this.on('stage:complete', this.handleStageComplete.bind(this));
    this.on('stage:failed', this.handleStageFailed.bind(this));
    this.on('pipeline:complete', this.handlePipelineComplete.bind(this));
    this.on('quality:gate:failed', this.handleQualityGateFailed.bind(this));
  }
  
  // 执行流水线
  async executePipeline(context = {}) {
    const pipelineId = this.generatePipelineId();
    const startTime = Date.now();
    
    try {
      this.emit('pipeline:start', { pipelineId, context, startTime });
      
      // 按顺序执行各个阶段
      for (const [stageName, stage] of this.stages) {
        await this.executeStage(pipelineId, stageName, stage, context);
      }
      
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      this.emit('pipeline:complete', {
        pipelineId,
        duration,
        success: true,
        context
      });
      
      return { success: true, pipelineId, duration };
      
    } catch (error) {
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      this.emit('pipeline:failed', {
        pipelineId,
        error,
        duration,
        context
      });
      
      throw error;
    }
  }
  
  async executeStage(pipelineId, stageName, stage, context) {
    const stageStartTime = Date.now();
    
    try {
      this.emit('stage:start', {
        pipelineId,
        stageName,
        startTime: stageStartTime,
        context
      });
      
      // 执行阶段前的Sentry操作
      await this.executeSentryActions(stage.sentryActions, 'before', context);
      
      // 执行阶段操作
      const stageResult = await stage.execute(context);
      
      // 执行阶段后的Sentry操作
      await this.executeSentryActions(stage.sentryActions, 'after', {
        ...context,
        stageResult
      });
      
      // 质量门检查
      const qualityCheck = await this.performQualityGateCheck(stageName, stageResult);
      
      if (!qualityCheck.passed) {
        throw new Error(`Quality gate failed for stage ${stageName}: ${qualityCheck.reason}`);
      }
      
      const stageEndTime = Date.now();
      const stageDuration = stageEndTime - stageStartTime;
      
      this.emit('stage:complete', {
        pipelineId,
        stageName,
        duration: stageDuration,
        result: stageResult,
        qualityCheck,
        context
      });
      
      return stageResult;
      
    } catch (error) {
      const stageEndTime = Date.now();
      const stageDuration = stageEndTime - stageStartTime;
      
      this.emit('stage:failed', {
        pipelineId,
        stageName,
        error,
        duration: stageDuration,
        context
      });
      
      throw error;
    }
  }
  
  async executeSentryActions(actions, timing, context) {
    for (const action of actions) {
      try {
        await this.executeSentryAction(action, timing, context);
      } catch (error) {
        console.error(`Failed to execute Sentry action ${action}:`, error);
        // 记录错误但不中断流水线
      }
    }
  }
  
  async executeSentryAction(action, timing, context) {
    switch (action) {
      case 'setup-release':
        if (timing === 'before') {
          await this.setupSentryRelease(context);
        }
        break;
        
      case 'upload-sourcemaps':
        if (timing === 'after') {
          await this.uploadSourceMaps(context);
        }
        break;
        
      case 'deployment-tracking':
        if (timing === 'before') {
          await this.trackDeployment(context);
        }
        break;
        
      case 'release-health':
        if (timing === 'after') {
          await this.monitorReleaseHealth(context);
        }
        break;
        
      case 'error-tracking':
        await this.setupErrorTracking(context);
        break;
        
      case 'performance-monitoring':
        await this.setupPerformanceMonitoring(context);
        break;
        
      case 'real-time-monitoring':
        await this.enableRealTimeMonitoring(context);
        break;
        
      case 'alerting':
        await this.setupAlerting(context);
        break;
        
      case 'metrics-analysis':
        await this.analyzeMetrics(context);
        break;
        
      case 'quality-assessment':
        await this.assessQuality(context);
        break;
    }
  }
  
  async performQualityGateCheck(stageName, stageResult) {
    const checks = [];
    
    // 根据阶段执行不同的质量检查
    switch (stageName) {
      case 'test':
        checks.push(
          this.checkTestCoverage(stageResult),
          this.checkTestResults(stageResult)
        );
        break;
        
      case 'deploy':
        checks.push(
          this.checkDeploymentHealth(stageResult),
          this.checkErrorRate(stageResult)
        );
        break;
        
      case 'monitor':
        checks.push(
          this.checkPerformanceMetrics(stageResult),
          this.checkSystemHealth(stageResult)
        );
        break;
    }
    
    const results = await Promise.all(checks);
    const failedChecks = results.filter(result => !result.passed);
    
    if (failedChecks.length > 0) {
      this.emit('quality:gate:failed', {
        stageName,
        failedChecks,
        stageResult
      });
      
      return {
        passed: false,
        reason: failedChecks.map(check => check.reason).join(', '),
        details: failedChecks
      };
    }
    
    return {
      passed: true,
      checks: results
    };
  }
  
  async checkTestCoverage(stageResult) {
    const coverage = stageResult.coverage || 0;
    const threshold = this.options.qualityGates.coverageThreshold;
    
    return {
      name: 'test-coverage',
      passed: coverage >= threshold,
      value: coverage,
      threshold,
      reason: coverage < threshold ? `Test coverage ${(coverage * 100).toFixed(1)}% below threshold ${(threshold * 100).toFixed(1)}%` : null
    };
  }
  
  async checkErrorRate(stageResult) {
    const errorRate = await this.metricsCollector.getErrorRate();
    const threshold = this.options.qualityGates.errorRate;
    
    return {
      name: 'error-rate',
      passed: errorRate <= threshold,
      value: errorRate,
      threshold,
      reason: errorRate > threshold ? `Error rate ${(errorRate * 100).toFixed(2)}% exceeds threshold ${(threshold * 100).toFixed(2)}%` : null
    };
  }
  
  async checkPerformanceMetrics(stageResult) {
    const performanceScore = await this.metricsCollector.getPerformanceScore();
    const threshold = this.options.qualityGates.performanceScore;
    
    return {
      name: 'performance-score',
      passed: performanceScore >= threshold,
      value: performanceScore,
      threshold,
      reason: performanceScore < threshold ? `Performance score ${performanceScore} below threshold ${threshold}` : null
    };
  }
  
  generatePipelineId() {
    return `pipeline-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
  
  // 事件处理器
  handleStageStart(event) {
    console.log(`Stage ${event.stageName} started for pipeline ${event.pipelineId}`);
    this.recordMetric('stage.start', {
      stage: event.stageName,
      pipeline: event.pipelineId,
      timestamp: event.startTime
    });
  }
  
  handleStageComplete(event) {
    console.log(`Stage ${event.stageName} completed in ${event.duration}ms`);
    this.recordMetric('stage.complete', {
      stage: event.stageName,
      pipeline: event.pipelineId,
      duration: event.duration,
      success: true
    });
  }
  
  handleStageFailed(event) {
    console.error(`Stage ${event.stageName} failed:`, event.error);
    this.recordMetric('stage.failed', {
      stage: event.stageName,
      pipeline: event.pipelineId,
      duration: event.duration,
      error: event.error.message,
      success: false
    });
    
    // 发送失败通知
    this.sendNotification('stage_failed', {
      stage: event.stageName,
      pipeline: event.pipelineId,
      error: event.error,
      duration: event.duration
    });
  }
  
  handlePipelineComplete(event) {
    console.log(`Pipeline ${event.pipelineId} completed successfully in ${event.duration}ms`);
    this.recordMetric('pipeline.complete', {
      pipeline: event.pipelineId,
      duration: event.duration,
      success: true
    });
    
    // 发送成功通知
    this.sendNotification('pipeline_success', {
      pipeline: event.pipelineId,
      duration: event.duration
    });
  }
  
  handleQualityGateFailed(event) {
    console.error(`Quality gate failed for stage ${event.stageName}:`, event.failedChecks);
    
    // 发送质量门失败通知
    this.sendNotification('quality_gate_failed', {
      stage: event.stageName,
      failedChecks: event.failedChecks
    });
  }
  
  recordMetric(name, data) {
    if (!this.metrics.has(name)) {
      this.metrics.set(name, []);
    }
    
    this.metrics.get(name).push({
      timestamp: Date.now(),
      ...data
    });
  }
  
  async sendNotification(type, data) {
    const promises = [];
    
    for (const [channelName, notifier] of this.notifications) {
      promises.push(
        notifier.send(type, data).catch(error => {
          console.error(`Failed to send notification via ${channelName}:`, error);
        })
      );
    }
    
    await Promise.allSettled(promises);
  }
  
  // 事件发射器方法
  on(event, handler) {
    if (!this.eventHandlers) {
      this.eventHandlers = new Map();
    }
    
    if (!this.eventHandlers.has(event)) {
      this.eventHandlers.set(event, []);
    }
    
    this.eventHandlers.get(event).push(handler);
  }
  
  emit(event, data) {
    if (!this.eventHandlers || !this.eventHandlers.has(event)) {
      return;
    }
    
    const handlers = this.eventHandlers.get(event);
    handlers.forEach(handler => {
      try {
        handler(data);
      } catch (error) {
        console.error(`Error in event handler for ${event}:`, error);
      }
    });
  }
}
```

### 1.2 流水线阶段管理

```javascript
// 流水线阶段管理器
class PipelineStage {
  constructor(config) {
    this.name = config.name;
    this.description = config.description;
    this.actions = config.actions || [];
    this.sentryActions = config.sentryActions || [];
    this.timeout = config.timeout || 300000; // 5分钟默认超时
    this.retryAttempts = config.retryAttempts || 3;
    this.parallel = config.parallel || false;
  }
  
  async execute(context) {
    const startTime = Date.now();
    
    try {
      let result;
      
      if (this.parallel) {
        result = await this.executeParallel(context);
      } else {
        result = await this.executeSequential(context);
      }
      
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      return {
        stage: this.name,
        success: true,
        duration,
        result,
        timestamp: endTime
      };
      
    } catch (error) {
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      throw new StageExecutionError(this.name, error, duration);
    }
  }
  
  async executeSequential(context) {
    const results = [];
    
    for (const action of this.actions) {
      const actionResult = await this.executeAction(action, context);
      results.push(actionResult);
      
      // 将前一个动作的结果传递给下一个动作
      context = { ...context, previousResult: actionResult };
    }
    
    return results;
  }
  
  async executeParallel(context) {
    const promises = this.actions.map(action => 
      this.executeAction(action, context)
    );
    
    return await Promise.all(promises);
  }
  
  async executeAction(action, context) {
    let attempt = 0;
    let lastError;
    
    while (attempt < this.retryAttempts) {
      try {
        return await this.performAction(action, context);
      } catch (error) {
        lastError = error;
        attempt++;
        
        if (attempt < this.retryAttempts) {
          const delay = Math.pow(2, attempt) * 1000; // 指数退避
          await this.sleep(delay);
        }
      }
    }
    
    throw new ActionExecutionError(action, lastError, attempt);
  }
  
  async performAction(action, context) {
    switch (action) {
      case 'compile':
        return await this.compile(context);
      case 'test':
        return await this.runTests(context);
      case 'analyze':
        return await this.analyzeCode(context);
      case 'unit-test':
        return await this.runUnitTests(context);
      case 'integration-test':
        return await this.runIntegrationTests(context);
      case 'e2e-test':
        return await this.runE2ETests(context);
      case 'deploy-staging':
        return await this.deployToStaging(context);
      case 'deploy-production':
        return await this.deployToProduction(context);
      case 'health-check':
        return await this.performHealthCheck(context);
      case 'performance-check':
        return await this.performPerformanceCheck(context);
      case 'error-check':
        return await this.performErrorCheck(context);
      case 'collect-metrics':
        return await this.collectMetrics(context);
      case 'generate-reports':
        return await this.generateReports(context);
      case 'notify-team':
        return await this.notifyTeam(context);
      default:
        throw new Error(`Unknown action: ${action}`);
    }
  }
  
  async compile(context) {
    // 模拟编译过程
    await this.sleep(2000);
    return {
      action: 'compile',
      success: true,
      artifacts: ['bundle.js', 'bundle.css'],
      size: {
        js: 245678,
        css: 45123
      }
    };
  }
  
  async runTests(context) {
    // 模拟测试执行
    await this.sleep(5000);
    return {
      action: 'test',
      success: true,
      total: 150,
      passed: 148,
      failed: 2,
      coverage: 0.85,
      duration: 4850
    };
  }
  
  async analyzeCode(context) {
    // 模拟代码分析
    await this.sleep(3000);
    return {
      action: 'analyze',
      success: true,
      issues: {
        critical: 0,
        major: 2,
        minor: 8,
        info: 15
      },
      qualityScore: 8.5
    };
  }
  
  async runUnitTests(context) {
    await this.sleep(3000);
    return {
      action: 'unit-test',
      success: true,
      total: 120,
      passed: 119,
      failed: 1,
      coverage: 0.88
    };
  }
  
  async runIntegrationTests(context) {
    await this.sleep(4000);
    return {
      action: 'integration-test',
      success: true,
      total: 25,
      passed: 24,
      failed: 1,
      coverage: 0.75
    };
  }
  
  async runE2ETests(context) {
    await this.sleep(8000);
    return {
      action: 'e2e-test',
      success: true,
      total: 15,
      passed: 15,
      failed: 0,
      coverage: 0.65
    };
  }
  
  async deployToStaging(context) {
    await this.sleep(6000);
    return {
      action: 'deploy-staging',
      success: true,
      environment: 'staging',
      url: 'https://staging.example.com',
      version: context.version || '1.0.0'
    };
  }
  
  async deployToProduction(context) {
    await this.sleep(8000);
    return {
      action: 'deploy-production',
      success: true,
      environment: 'production',
      url: 'https://example.com',
      version: context.version || '1.0.0'
    };
  }
  
  async performHealthCheck(context) {
    await this.sleep(2000);
    return {
      action: 'health-check',
      success: true,
      status: 'healthy',
      checks: {
        database: 'ok',
        cache: 'ok',
        external_apis: 'ok'
      }
    };
  }
  
  async performPerformanceCheck(context) {
    await this.sleep(3000);
    return {
      action: 'performance-check',
      success: true,
      metrics: {
        responseTime: 245,
        throughput: 1250,
        errorRate: 0.002,
        cpuUsage: 0.45,
        memoryUsage: 0.67
      }
    };
  }
  
  async performErrorCheck(context) {
    await this.sleep(1500);
    return {
      action: 'error-check',
      success: true,
      errorCount: 3,
      errorRate: 0.001,
      criticalErrors: 0
    };
  }
  
  async collectMetrics(context) {
    await this.sleep(2000);
    return {
      action: 'collect-metrics',
      success: true,
      metrics: {
        deploymentFrequency: 12,
        leadTime: 2.5,
        mttr: 0.8,
        changeFailureRate: 0.05
      }
    };
  }
  
  async generateReports(context) {
    await this.sleep(3000);
    return {
      action: 'generate-reports',
      success: true,
      reports: [
        'deployment-report.html',
        'performance-report.pdf',
        'quality-report.json'
      ]
    };
  }
  
  async notifyTeam(context) {
    await this.sleep(1000);
    return {
      action: 'notify-team',
      success: true,
      notifications: {
        slack: 'sent',
        email: 'sent',
        teams: 'sent'
      }
    };
  }
  
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

// 自定义错误类
class StageExecutionError extends Error {
  constructor(stageName, originalError, duration) {
    super(`Stage ${stageName} failed: ${originalError.message}`);
    this.name = 'StageExecutionError';
    this.stageName = stageName;
    this.originalError = originalError;
    this.duration = duration;
  }
}

class ActionExecutionError extends Error {
  constructor(actionName, originalError, attempts) {
    super(`Action ${actionName} failed after ${attempts} attempts: ${originalError.message}`);
    this.name = 'ActionExecutionError';
    this.actionName = actionName;
    this.originalError = originalError;
    this.attempts = attempts;
  }
}
```

## 2. CI/CD集成实现

### 2.1 GitHub Actions集成

```yaml
# .github/workflows/sentry-integration.yml
name: Sentry DevOps Integration

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  release:
    types: [ published ]

env:
  SENTRY_ORG: ${{ secrets.SENTRY_ORG }}
  SENTRY_PROJECT: ${{ secrets.SENTRY_PROJECT }}
  SENTRY_AUTH_TOKEN: ${{ secrets.SENTRY_AUTH_TOKEN }}
  NODE_VERSION: '18'

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    outputs:
      release-version: ${{ steps.version.outputs.version }}
      build-artifacts: ${{ steps.build.outputs.artifacts }}
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: ${{ env.NODE_VERSION }}
        cache: 'npm'
    
    - name: Install dependencies
      run: npm ci
    
    - name: Generate version
      id: version
      run: |
        VERSION=$(node -p "require('./package.json').version")-$(git rev-parse --short HEAD)
        echo "version=$VERSION" >> $GITHUB_OUTPUT
        echo "Generated version: $VERSION"
    
    - name: Create Sentry release
      uses: getsentry/action-release@v1
      env:
        SENTRY_AUTH_TOKEN: ${{ env.SENTRY_AUTH_TOKEN }}
        SENTRY_ORG: ${{ env.SENTRY_ORG }}
        SENTRY_PROJECT: ${{ env.SENTRY_PROJECT }}
      with:
        environment: ${{ github.ref == 'refs/heads/main' && 'production' || 'staging' }}
        version: ${{ steps.version.outputs.version }}
    
    - name: Run linting
      run: npm run lint
    
    - name: Run unit tests
      run: |
        npm run test:unit -- --coverage --reporter=json --outputFile=test-results.json
        npm run test:unit -- --coverage --reporter=lcov
    
    - name: Run integration tests
      run: npm run test:integration
    
    - name: Build application
      id: build
      run: |
        npm run build
        echo "artifacts=$(ls -la dist/)" >> $GITHUB_OUTPUT
    
    - name: Upload source maps to Sentry
      uses: getsentry/action-release@v1
      env:
        SENTRY_AUTH_TOKEN: ${{ env.SENTRY_AUTH_TOKEN }}
        SENTRY_ORG: ${{ env.SENTRY_ORG }}
        SENTRY_PROJECT: ${{ env.SENTRY_PROJECT }}
      with:
        environment: ${{ github.ref == 'refs/heads/main' && 'production' || 'staging' }}
        version: ${{ steps.version.outputs.version }}
        sourcemaps: './dist'
        url_prefix: '~/'
    
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: |
          test-results.json
          coverage/
    
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: build-artifacts
        path: dist/
  
  deploy-staging:
    needs: build-and-test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: staging
    
    steps:
    - name: Download build artifacts
      uses: actions/download-artifact@v3
      with:
        name: build-artifacts
        path: dist/
    
    - name: Deploy to staging
      run: |
        echo "Deploying to staging environment..."
        # 实际的部署命令
        # aws s3 sync dist/ s3://staging-bucket/
        # aws cloudfront create-invalidation --distribution-id $STAGING_DISTRIBUTION_ID --paths "/*"
    
    - name: Create Sentry deployment
      uses: getsentry/action-release@v1
      env:
        SENTRY_AUTH_TOKEN: ${{ env.SENTRY_AUTH_TOKEN }}
        SENTRY_ORG: ${{ env.SENTRY_ORG }}
        SENTRY_PROJECT: ${{ env.SENTRY_PROJECT }}
      with:
        environment: staging
        version: ${{ needs.build-and-test.outputs.release-version }}
        deployed: true
    
    - name: Run E2E tests
      run: |
        npm install
        npm run test:e2e:staging
    
    - name: Monitor deployment health
      run: |
        node scripts/monitor-deployment.js --environment=staging --version=${{ needs.build-and-test.outputs.release-version }}
  
  deploy-production:
    needs: [build-and-test, deploy-staging]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
    - name: Download build artifacts
      uses: actions/download-artifact@v3
      with:
        name: build-artifacts
        path: dist/
    
    - name: Deploy to production
      run: |
        echo "Deploying to production environment..."
        # 实际的部署命令
        # aws s3 sync dist/ s3://production-bucket/
        # aws cloudfront create-invalidation --distribution-id $PRODUCTION_DISTRIBUTION_ID --paths "/*"
    
    - name: Create Sentry deployment
      uses: getsentry/action-release@v1
      env:
        SENTRY_AUTH_TOKEN: ${{ env.SENTRY_AUTH_TOKEN }}
        SENTRY_ORG: ${{ env.SENTRY_ORG }}
        SENTRY_PROJECT: ${{ env.SENTRY_PROJECT }}
      with:
        environment: production
        version: ${{ needs.build-and-test.outputs.release-version }}
        deployed: true
    
    - name: Monitor deployment health
      run: |
        node scripts/monitor-deployment.js --environment=production --version=${{ needs.build-and-test.outputs.release-version }}
    
    - name: Finalize Sentry release
      uses: getsentry/action-release@v1
      env:
        SENTRY_AUTH_TOKEN: ${{ env.SENTRY_AUTH_TOKEN }}
        SENTRY_ORG: ${{ env.SENTRY_ORG }}
        SENTRY_PROJECT: ${{ env.SENTRY_PROJECT }}
      with:
        environment: production
        version: ${{ needs.build-and-test.outputs.release-version }}
        finalize: true
  
  quality-gates:
    needs: [build-and-test]
    runs-on: ubuntu-latest
    if: always()
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Download test results
      uses: actions/download-artifact@v3
      with:
        name: test-results
        path: test-results/
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: ${{ env.NODE_VERSION }}
    
    - name: Install dependencies
      run: npm ci
    
    - name: Run quality gate checks
      run: |
        node scripts/quality-gates.js \
          --test-results=test-results/test-results.json \
          --coverage-dir=test-results/coverage \
          --sentry-project=${{ env.SENTRY_PROJECT }} \
          --environment=${{ github.ref == 'refs/heads/main' && 'production' || 'staging' }}
    
    - name: Comment PR with quality results
      if: github.event_name == 'pull_request'
      uses: actions/github-script@v6
      with:
        script: |
          const fs = require('fs');
          const qualityResults = JSON.parse(fs.readFileSync('quality-results.json', 'utf8'));
          
          const comment = `
          ## 🔍 Quality Gate Results
          
          | Metric | Value | Threshold | Status |
          |--------|-------|-----------|--------|
          | Test Coverage | ${(qualityResults.coverage * 100).toFixed(1)}% | 80% | ${qualityResults.coverage >= 0.8 ? '✅' : '❌'} |
          | Error Rate | ${(qualityResults.errorRate * 100).toFixed(2)}% | 1% | ${qualityResults.errorRate <= 0.01 ? '✅' : '❌'} |
          | Performance Score | ${qualityResults.performanceScore} | 80 | ${qualityResults.performanceScore >= 80 ? '✅' : '❌'} |
          
          **Overall Status:** ${qualityResults.passed ? '✅ PASSED' : '❌ FAILED'}
          `;
          
          github.rest.issues.createComment({
            issue_number: context.issue.number,
            owner: context.repo.owner,
            repo: context.repo.repo,
            body: comment
          });
```

### 2.2 部署监控脚本

```javascript
// scripts/monitor-deployment.js
const { SentryApi } = require('@sentry/node');
const axios = require('axios');

class DeploymentMonitor {
  constructor(options) {
    this.options = {
      environment: 'production',
      version: null,
      monitorDuration: 300000, // 5分钟
      checkInterval: 30000, // 30秒
      errorThreshold: 0.01, // 1%
      performanceThreshold: 3000, // 3秒
      ...options
    };
    
    this.sentryApi = new SentryApi({
      token: process.env.SENTRY_AUTH_TOKEN,
      org: process.env.SENTRY_ORG,
      project: process.env.SENTRY_PROJECT
    });
    
    this.metrics = {
      errors: [],
      performance: [],
      health: []
    };
  }
  
  async monitor() {
    console.log(`Starting deployment monitoring for version ${this.options.version} in ${this.options.environment}`);
    
    const startTime = Date.now();
    const endTime = startTime + this.options.monitorDuration;
    
    while (Date.now() < endTime) {
      try {
        await this.collectMetrics();
        await this.checkHealthStatus();
        
        const analysis = this.analyzeMetrics();
        
        if (!analysis.healthy) {
          console.error('Deployment health check failed:', analysis.issues);
          await this.triggerRollback(analysis);
          process.exit(1);
        }
        
        console.log(`Health check passed. Next check in ${this.options.checkInterval / 1000}s`);
        await this.sleep(this.options.checkInterval);
        
      } catch (error) {
        console.error('Error during monitoring:', error);
        await this.sleep(this.options.checkInterval);
      }
    }
    
    console.log('Deployment monitoring completed successfully');
    await this.generateReport();
  }
  
  async collectMetrics() {
    const now = Date.now();
    const oneMinuteAgo = now - 60000;
    
    // 收集错误指标
    const errorStats = await this.sentryApi.getErrorStats({
      environment: this.options.environment,
      release: this.options.version,
      start: new Date(oneMinuteAgo),
      end: new Date(now)
    });
    
    this.metrics.errors.push({
      timestamp: now,
      count: errorStats.count,
      rate: errorStats.rate,
      uniqueUsers: errorStats.uniqueUsers
    });
    
    // 收集性能指标
    const performanceStats = await this.sentryApi.getPerformanceStats({
      environment: this.options.environment,
      release: this.options.version,
      start: new Date(oneMinuteAgo),
      end: new Date(now)
    });
    
    this.metrics.performance.push({
      timestamp: now,
      avgResponseTime: performanceStats.avgResponseTime,
      p95ResponseTime: performanceStats.p95ResponseTime,
      throughput: performanceStats.throughput
    });
  }
  
  async checkHealthStatus() {
    const healthEndpoints = [
      `https://${this.getHostname()}/health`,
      `https://${this.getHostname()}/api/health`,
      `https://${this.getHostname()}/status`
    ];
    
    const healthChecks = await Promise.allSettled(
      healthEndpoints.map(url => this.checkEndpoint(url))
    );
    
    const healthStatus = {
      timestamp: Date.now(),
      endpoints: healthChecks.map((result, index) => ({
        url: healthEndpoints[index],
        status: result.status === 'fulfilled' ? 'healthy' : 'unhealthy',
        responseTime: result.value?.responseTime || null,
        error: result.reason?.message || null
      }))
    };
    
    this.metrics.health.push(healthStatus);
  }
  
  async checkEndpoint(url) {
    const startTime = Date.now();
    
    try {
      const response = await axios.get(url, {
        timeout: 10000,
        validateStatus: status => status < 500
      });
      
      const responseTime = Date.now() - startTime;
      
      return {
        status: response.status,
        responseTime,
        healthy: response.status < 400
      };
    } catch (error) {
      const responseTime = Date.now() - startTime;
      throw new Error(`Health check failed: ${error.message} (${responseTime}ms)`);
    }
  }
  
  analyzeMetrics() {
    const issues = [];
    
    // 分析错误率
    if (this.metrics.errors.length > 0) {
      const latestErrors = this.metrics.errors.slice(-3); // 最近3次检查
      const avgErrorRate = latestErrors.reduce((sum, metric) => sum + metric.rate, 0) / latestErrors.length;
      
      if (avgErrorRate > this.options.errorThreshold) {
        issues.push({
          type: 'high_error_rate',
          value: avgErrorRate,
          threshold: this.options.errorThreshold,
          message: `Error rate ${(avgErrorRate * 100).toFixed(2)}% exceeds threshold ${(this.options.errorThreshold * 100).toFixed(2)}%`
        });
      }
    }
    
    // 分析性能指标
    if (this.metrics.performance.length > 0) {
      const latestPerformance = this.metrics.performance.slice(-3);
      const avgResponseTime = latestPerformance.reduce((sum, metric) => sum + metric.avgResponseTime, 0) / latestPerformance.length;
      
      if (avgResponseTime > this.options.performanceThreshold) {
        issues.push({
          type: 'slow_response_time',
          value: avgResponseTime,
          threshold: this.options.performanceThreshold,
          message: `Average response time ${avgResponseTime.toFixed(0)}ms exceeds threshold ${this.options.performanceThreshold}ms`
        });
      }
    }
    
    // 分析健康检查
    if (this.metrics.health.length > 0) {
      const latestHealth = this.metrics.health[this.metrics.health.length - 1];
      const unhealthyEndpoints = latestHealth.endpoints.filter(endpoint => endpoint.status === 'unhealthy');
      
      if (unhealthyEndpoints.length > 0) {
        issues.push({
          type: 'unhealthy_endpoints',
          value: unhealthyEndpoints.length,
          threshold: 0,
          message: `${unhealthyEndpoints.length} endpoint(s) are unhealthy: ${unhealthyEndpoints.map(e => e.url).join(', ')}`
        });
      }
    }
    
    return {
      healthy: issues.length === 0,
      issues,
      metrics: this.metrics
    };
  }
  
  async triggerRollback(analysis) {
    console.log('Triggering automatic rollback due to health issues...');
    
    // 记录回滚事件到Sentry
    await this.sentryApi.createEvent({
      message: 'Automatic rollback triggered',
      level: 'error',
      tags: {
        deployment_version: this.options.version,
        environment: this.options.environment,
        rollback_reason: 'health_check_failed'
      },
      extra: {
        issues: analysis.issues,
        metrics: analysis.metrics
      }
    });
    
    // 发送告警通知
    await this.sendRollbackNotification(analysis);
    
    // 执行回滚操作（这里需要根据实际部署方式实现）
    // await this.executeRollback();
  }
  
  async sendRollbackNotification(analysis) {
    const webhookUrl = process.env.SLACK_WEBHOOK_URL;
    if (!webhookUrl) return;
    
    const message = {
      text: '🚨 Automatic Rollback Triggered',
      attachments: [
        {
          color: 'danger',
          fields: [
            {
              title: 'Environment',
              value: this.options.environment,
              short: true
            },
            {
              title: 'Version',
              value: this.options.version,
              short: true
            },
            {
              title: 'Issues',
              value: analysis.issues.map(issue => issue.message).join('\n'),
              short: false
            }
          ],
          ts: Math.floor(Date.now() / 1000)
        }
      ]
    };
    
    try {
      await axios.post(webhookUrl, message);
    } catch (error) {
      console.error('Failed to send rollback notification:', error);
    }
  }
  
  async generateReport() {
    const report = {
      deployment: {
        version: this.options.version,
        environment: this.options.environment,
        monitoringDuration: this.options.monitorDuration,
        timestamp: new Date().toISOString()
      },
      summary: {
        totalChecks: this.metrics.errors.length,
        avgErrorRate: this.calculateAverage(this.metrics.errors, 'rate'),
        avgResponseTime: this.calculateAverage(this.metrics.performance, 'avgResponseTime'),
        healthChecksPassed: this.metrics.health.filter(h => 
          h.endpoints.every(e => e.status === 'healthy')
        ).length
      },
      metrics: this.metrics
    };
    
    console.log('\n=== Deployment Monitoring Report ===');
    console.log(JSON.stringify(report, null, 2));
    
    // 保存报告到文件
    const fs = require('fs');
    fs.writeFileSync(
      `deployment-report-${this.options.version}-${Date.now()}.json`,
      JSON.stringify(report, null, 2)
    );
  }
  
  calculateAverage(data, property) {
    if (data.length === 0) return 0;
    return data.reduce((sum, item) => sum + item[property], 0) / data.length;
  }
  
  getHostname() {
    const hostnames = {
      production: 'example.com',
      staging: 'staging.example.com',
      development: 'dev.example.com'
    };
    
    return hostnames[this.options.environment] || 'localhost';
  }
  
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

// 命令行接口
if (require.main === module) {
  const args = process.argv.slice(2);
  const options = {};
  
  args.forEach(arg => {
    const [key, value] = arg.split('=');
    if (key.startsWith('--')) {
      const optionKey = key.substring(2).replace(/-([a-z])/g, (g) => g[1].toUpperCase());
      options[optionKey] = value || true;
    }
  });
  
  const monitor = new DeploymentMonitor(options);
  monitor.monitor().catch(error => {
    console.error('Monitoring failed:', error);
    process.exit(1);
  });
}

module.exports = DeploymentMonitor;
```

## 3. 自动化质量门控

### 3.1 质量门控检查器

```javascript
// scripts/quality-gates.js
const fs = require('fs');
const path = require('path');
const { SentryApi } = require('@sentry/node');

class QualityGateChecker {
  constructor(options = {}) {
    this.options = {
      testResultsFile: 'test-results.json',
      coverageDir: 'coverage',
      environment: 'production',
      sentryProject: process.env.SENTRY_PROJECT,
      thresholds: {
        testCoverage: 0.8, // 80%
        errorRate: 0.01, // 1%
        performanceScore: 80,
        codeQuality: 7.0,
        securityScore: 8.0
      },
      ...options
    };
    
    this.sentryApi = new SentryApi({
      token: process.env.SENTRY_AUTH_TOKEN,
      org: process.env.SENTRY_ORG,
      project: this.options.sentryProject
    });
    
    this.results = {
      passed: false,
      checks: [],
      summary: {},
      timestamp: new Date().toISOString()
    };
  }
  
  async runAllChecks() {
    console.log('🔍 Running quality gate checks...');
    
    try {
      // 执行各项检查
      await this.checkTestCoverage();
      await this.checkTestResults();
      await this.checkCodeQuality();
      await this.checkSecurity();
      await this.checkPerformance();
      await this.checkErrorRate();
      
      // 生成总结
      this.generateSummary();
      
      // 保存结果
      this.saveResults();
      
      // 输出结果
      this.printResults();
      
      return this.results;
      
    } catch (error) {
      console.error('Quality gate check failed:', error);
      throw error;
    }
  }
  
  async checkTestCoverage() {
    console.log('📊 Checking test coverage...');
    
    try {
      const coverageSummaryPath = path.join(this.options.coverageDir, 'coverage-summary.json');
      
      if (!fs.existsSync(coverageSummaryPath)) {
        throw new Error('Coverage summary file not found');
      }
      
      const coverageData = JSON.parse(fs.readFileSync(coverageSummaryPath, 'utf8'));
      const totalCoverage = coverageData.total;
      
      const coverageMetrics = {
        lines: totalCoverage.lines.pct / 100,
        functions: totalCoverage.functions.pct / 100,
        branches: totalCoverage.branches.pct / 100,
        statements: totalCoverage.statements.pct / 100
      };
      
      const overallCoverage = Object.values(coverageMetrics).reduce((sum, val) => sum + val, 0) / 4;
      
      const check = {
        name: 'test-coverage',
        passed: overallCoverage >= this.options.thresholds.testCoverage,
        value: overallCoverage,
        threshold: this.options.thresholds.testCoverage,
        details: coverageMetrics,
        message: overallCoverage >= this.options.thresholds.testCoverage 
          ? `Test coverage ${(overallCoverage * 100).toFixed(1)}% meets threshold`
          : `Test coverage ${(overallCoverage * 100).toFixed(1)}% below threshold ${(this.options.thresholds.testCoverage * 100).toFixed(1)}%`
      };
      
      this.results.checks.push(check);
      
    } catch (error) {
      this.results.checks.push({
        name: 'test-coverage',
        passed: false,
        error: error.message,
        message: `Test coverage check failed: ${error.message}`
      });
    }
  }
  
  async checkTestResults() {
    console.log('🧪 Checking test results...');
    
    try {
      if (!fs.existsSync(this.options.testResultsFile)) {
        throw new Error('Test results file not found');
      }
      
      const testData = JSON.parse(fs.readFileSync(this.options.testResultsFile, 'utf8'));
      
      const testMetrics = {
        total: testData.numTotalTests || 0,
        passed: testData.numPassedTests || 0,
        failed: testData.numFailedTests || 0,
        skipped: testData.numPendingTests || 0,
        successRate: testData.numTotalTests > 0 ? testData.numPassedTests / testData.numTotalTests : 0
      };
      
      const check = {
        name: 'test-results',
        passed: testMetrics.failed === 0 && testMetrics.successRate >= 0.95,
        value: testMetrics.successRate,
        threshold: 0.95,
        details: testMetrics,
        message: testMetrics.failed === 0 
          ? `All ${testMetrics.total} tests passed`
          : `${testMetrics.failed} test(s) failed out of ${testMetrics.total}`
      };
      
      this.results.checks.push(check);
      
    } catch (error) {
      this.results.checks.push({
        name: 'test-results',
        passed: false,
        error: error.message,
        message: `Test results check failed: ${error.message}`
      });
    }
  }
  
  async checkCodeQuality() {
    console.log('📝 Checking code quality...');
    
    try {
      // 这里可以集成SonarQube、ESLint等代码质量工具
      // 为了演示，我们模拟一个代码质量检查
      
      const qualityMetrics = await this.analyzeCodeQuality();
      
      const check = {
        name: 'code-quality',
        passed: qualityMetrics.score >= this.options.thresholds.codeQuality,
        value: qualityMetrics.score,
        threshold: this.options.thresholds.codeQuality,
        details: qualityMetrics,
        message: qualityMetrics.score >= this.options.thresholds.codeQuality
          ? `Code quality score ${qualityMetrics.score} meets threshold`
          : `Code quality score ${qualityMetrics.score} below threshold ${this.options.thresholds.codeQuality}`
      };
      
      this.results.checks.push(check);
      
    } catch (error) {
      this.results.checks.push({
        name: 'code-quality',
        passed: false,
        error: error.message,
        message: `Code quality check failed: ${error.message}`
      });
    }
  }
  
  async analyzeCodeQuality() {
    // 模拟代码质量分析
    // 实际实现中可以调用SonarQube API或其他代码质量工具
    
    return {
      score: 8.2,
      issues: {
        critical: 0,
        major: 2,
        minor: 8,
        info: 15
      },
      metrics: {
        complexity: 3.2,
        duplication: 0.05,
        maintainability: 8.5,
        reliability: 9.1,
        security: 8.8
      }
    };
  }
  
  async checkSecurity() {
    console.log('🔒 Checking security...');
    
    try {
      const securityMetrics = await this.analyzeSecurityVulnerabilities();
      
      const check = {
        name: 'security',
        passed: securityMetrics.score >= this.options.thresholds.securityScore && securityMetrics.criticalVulnerabilities === 0,
        value: securityMetrics.score,
        threshold: this.options.thresholds.securityScore,
        details: securityMetrics,
        message: securityMetrics.criticalVulnerabilities > 0
          ? `${securityMetrics.criticalVulnerabilities} critical security vulnerabilities found`
          : securityMetrics.score >= this.options.thresholds.securityScore
            ? `Security score ${securityMetrics.score} meets threshold`
            : `Security score ${securityMetrics.score} below threshold ${this.options.thresholds.securityScore}`
      };
      
      this.results.checks.push(check);
      
    } catch (error) {
      this.results.checks.push({
        name: 'security',
        passed: false,
        error: error.message,
        message: `Security check failed: ${error.message}`
      });
    }
  }
  
  async analyzeSecurityVulnerabilities() {
    // 模拟安全漏洞分析
    // 实际实现中可以调用Snyk、OWASP ZAP等安全扫描工具
    
    return {
      score: 8.5,
      criticalVulnerabilities: 0,
      highVulnerabilities: 1,
      mediumVulnerabilities: 3,
      lowVulnerabilities: 8,
      vulnerabilities: [
        {
          severity: 'high',
          type: 'dependency',
          package: 'lodash',
          version: '4.17.15',
          description: 'Prototype pollution vulnerability',
          cve: 'CVE-2020-8203'
        },
        {
          severity: 'medium',
          type: 'code',
          file: 'src/utils/validation.js',
          line: 45,
          description: 'Potential XSS vulnerability in user input validation'
        }
      ]
    };
  }
  
  async checkPerformance() {
    console.log('⚡ Checking performance metrics...');
    
    try {
      const performanceMetrics = await this.getPerformanceMetrics();
      
      const check = {
        name: 'performance',
        passed: performanceMetrics.score >= this.options.thresholds.performanceScore,
        value: performanceMetrics.score,
        threshold: this.options.thresholds.performanceScore,
        details: performanceMetrics,
        message: performanceMetrics.score >= this.options.thresholds.performanceScore
          ? `Performance score ${performanceMetrics.score} meets threshold`
          : `Performance score ${performanceMetrics.score} below threshold ${this.options.thresholds.performanceScore}`
      };
      
      this.results.checks.push(check);
      
    } catch (error) {
      this.results.checks.push({
        name: 'performance',
        passed: false,
        error: error.message,
        message: `Performance check failed: ${error.message}`
      });
    }
  }
  
  async getPerformanceMetrics() {
    // 从Sentry获取性能指标
    try {
      const now = Date.now();
      const oneHourAgo = now - 3600000;
      
      const performanceData = await this.sentryApi.getPerformanceStats({
        environment: this.options.environment,
        start: new Date(oneHourAgo),
        end: new Date(now)
      });
      
      // 计算综合性能分数
      const score = this.calculatePerformanceScore(performanceData);
      
      return {
        score,
        metrics: performanceData,
        webVitals: {
          lcp: performanceData.lcp || 2500,
          fid: performanceData.fid || 100,
          cls: performanceData.cls || 0.1,
          fcp: performanceData.fcp || 1800,
          ttfb: performanceData.ttfb || 600
        }
      };
    } catch (error) {
      // 如果无法获取Sentry数据，使用模拟数据
      return {
        score: 85,
        metrics: {
          avgResponseTime: 245,
          p95ResponseTime: 890,
          throughput: 1250
        },
        webVitals: {
          lcp: 2100,
          fid: 85,
          cls: 0.08,
          fcp: 1600,
          ttfb: 520
        }
      };
    }
  }
  
  calculatePerformanceScore(data) {
    // 基于Web Vitals计算性能分数
    const weights = {
      lcp: 0.25,
      fid: 0.25,
      cls: 0.25,
      fcp: 0.15,
      ttfb: 0.10
    };
    
    const thresholds = {
      lcp: { good: 2500, poor: 4000 },
      fid: { good: 100, poor: 300 },
      cls: { good: 0.1, poor: 0.25 },
      fcp: { good: 1800, poor: 3000 },
      ttfb: { good: 800, poor: 1800 }
    };
    
    let totalScore = 0;
    
    Object.keys(weights).forEach(metric => {
      const value = data[metric] || 0;
      const threshold = thresholds[metric];
      
      let score;
      if (value <= threshold.good) {
        score = 100;
      } else if (value <= threshold.poor) {
        score = 50;
      } else {
        score = 0;
      }
      
      totalScore += score * weights[metric];
    });
    
    return Math.round(totalScore);
  }
  
  async checkErrorRate() {
    console.log('🚨 Checking error rate...');
    
    try {
      const errorMetrics = await this.getErrorMetrics();
      
      const check = {
        name: 'error-rate',
        passed: errorMetrics.rate <= this.options.thresholds.errorRate,
        value: errorMetrics.rate,
        threshold: this.options.thresholds.errorRate,
        details: errorMetrics,
        message: errorMetrics.rate <= this.options.thresholds.errorRate
          ? `Error rate ${(errorMetrics.rate * 100).toFixed(2)}% within threshold`
          : `Error rate ${(errorMetrics.rate * 100).toFixed(2)}% exceeds threshold ${(this.options.thresholds.errorRate * 100).toFixed(2)}%`
      };
      
      this.results.checks.push(check);
      
    } catch (error) {
      this.results.checks.push({
        name: 'error-rate',
        passed: false,
        error: error.message,
        message: `Error rate check failed: ${error.message}`
      });
    }
  }
  
  async getErrorMetrics() {
    try {
      const now = Date.now();
      const oneHourAgo = now - 3600000;
      
      const errorData = await this.sentryApi.getErrorStats({
        environment: this.options.environment,
        start: new Date(oneHourAgo),
        end: new Date(now)
      });
      
      return {
        rate: errorData.rate || 0,
        count: errorData.count || 0,
        uniqueUsers: errorData.uniqueUsers || 0,
        topErrors: errorData.topErrors || []
      };
    } catch (error) {
      // 如果无法获取Sentry数据，使用模拟数据
      return {
        rate: 0.005,
        count: 12,
        uniqueUsers: 8,
        topErrors: [
          {
            message: 'TypeError: Cannot read property of undefined',
            count: 5,
            users: 3
          },
          {
            message: 'Network request failed',
            count: 4,
            users: 3
          }
        ]
      };
    }
  }
  
  generateSummary() {
    const passedChecks = this.results.checks.filter(check => check.passed);
    const failedChecks = this.results.checks.filter(check => !check.passed);
    
    this.results.passed = failedChecks.length === 0;
    this.results.summary = {
      total: this.results.checks.length,
      passed: passedChecks.length,
      failed: failedChecks.length,
      successRate: this.results.checks.length > 0 ? passedChecks.length / this.results.checks.length : 0,
      criticalIssues: failedChecks.filter(check => 
        check.name === 'security' || check.name === 'error-rate'
      ).length
    };
  }
  
  saveResults() {
    const resultsFile = 'quality-results.json';
    fs.writeFileSync(resultsFile, JSON.stringify(this.results, null, 2));
    console.log(`Quality gate results saved to ${resultsFile}`);
  }
  
  printResults() {
    console.log('\n=== Quality Gate Results ===');
    console.log(`Overall Status: ${this.results.passed ? '✅ PASSED' : '❌ FAILED'}`);
    console.log(`Success Rate: ${(this.results.summary.successRate * 100).toFixed(1)}%`);
    console.log(`Checks: ${this.results.summary.passed}/${this.results.summary.total} passed`);
    
    if (this.results.summary.criticalIssues > 0) {
      console.log(`⚠️  Critical Issues: ${this.results.summary.criticalIssues}`);
    }
    
    console.log('\n--- Detailed Results ---');
    this.results.checks.forEach(check => {
      const status = check.passed ? '✅' : '❌';
      console.log(`${status} ${check.name}: ${check.message}`);
      
      if (check.details && !check.passed) {
        console.log(`   Details: ${JSON.stringify(check.details, null, 2)}`);
      }
    });
  }
}

// 命令行接口
if (require.main === module) {
  const args = process.argv.slice(2);
  const options = {};
  
  args.forEach(arg => {
    const [key, value] = arg.split('=');
    if (key.startsWith('--')) {
      const optionKey = key.substring(2).replace(/-([a-z])/g, (g) => g[1].toUpperCase());
      options[optionKey] = value || true;
    }
  });
  
  const checker = new QualityGateChecker(options);
  checker.runAllChecks().then(results => {
    if (!results.passed) {
      process.exit(1);
    }
  }).catch(error => {
    console.error('Quality gate check failed:', error);
    process.exit(1);
  });
}

module.exports = QualityGateChecker;
```

## 4. 通知系统集成

### 4.1 多渠道通知管理器

```javascript
// 通知系统基类
class NotificationChannel {
  constructor(config) {
    this.config = config;
    this.enabled = config.enabled !== false;
  }
  
  async send(type, data) {
    if (!this.enabled) {
      return { success: false, reason: 'Channel disabled' };
    }
    
    try {
      return await this.sendNotification(type, data);
    } catch (error) {
      console.error(`Failed to send ${type} notification:`, error);
      return { success: false, error: error.message };
    }
  }
  
  async sendNotification(type, data) {
    throw new Error('sendNotification must be implemented by subclass');
  }
  
  formatMessage(type, data) {
    const templates = {
      stage_failed: {
        title: '🚨 Pipeline Stage Failed',
        message: `Stage ${data.stage} failed in pipeline ${data.pipeline}`,
        details: {
          'Error': data.error?.message || 'Unknown error',
          'Duration': `${(data.duration / 1000).toFixed(1)}s`,
          'Pipeline ID': data.pipeline
        }
      },
      pipeline_success: {
        title: '✅ Pipeline Completed Successfully',
        message: `Pipeline ${data.pipeline} completed successfully`,
        details: {
          'Duration': `${(data.duration / 1000).toFixed(1)}s`,
          'Pipeline ID': data.pipeline
        }
      },
      quality_gate_failed: {
        title: '❌ Quality Gate Failed',
        message: `Quality gate failed for stage ${data.stage}`,
        details: {
          'Failed Checks': data.failedChecks.map(check => check.reason).join(', '),
          'Stage': data.stage
        }
      },
      deployment_health_issue: {
        title: '⚠️ Deployment Health Issue',
        message: 'Deployment health check detected issues',
        details: data.issues.reduce((acc, issue) => {
          acc[issue.type] = issue.message;
          return acc;
        }, {})
      }
    };
    
    return templates[type] || {
      title: 'DevOps Notification',
      message: JSON.stringify(data),
      details: {}
    };
  }
}

// Slack通知器
class SlackNotifier extends NotificationChannel {
  constructor(config) {
    super(config);
    this.webhookUrl = config.webhookUrl;
    this.channel = config.channel || '#devops';
    this.username = config.username || 'Sentry DevOps Bot';
  }
  
  async sendNotification(type, data) {
    const formatted = this.formatMessage(type, data);
    
    const payload = {
      channel: this.channel,
      username: this.username,
      icon_emoji: this.getEmojiForType(type),
      text: formatted.title,
      attachments: [
        {
          color: this.getColorForType(type),
          title: formatted.message,
          fields: Object.entries(formatted.details).map(([key, value]) => ({
            title: key,
            value: value,
            short: true
          })),
          footer: 'Sentry DevOps Integration',
          ts: Math.floor(Date.now() / 1000)
        }
      ]
    };
    
    const response = await fetch(this.webhookUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });
    
    if (!response.ok) {
      throw new Error(`Slack API error: ${response.status} ${response.statusText}`);
    }
    
    return { success: true, channel: this.channel };
  }
  
  getEmojiForType(type) {
    const emojis = {
      stage_failed: ':x:',
      pipeline_success: ':white_check_mark:',
      quality_gate_failed: ':warning:',
      deployment_health_issue: ':exclamation:'
    };
    return emojis[type] || ':information_source:';
  }
  
  getColorForType(type) {
    const colors = {
      stage_failed: 'danger',
      pipeline_success: 'good',
      quality_gate_failed: 'warning',
      deployment_health_issue: 'warning'
    };
    return colors[type] || '#36a64f';
  }
}

// 邮件通知器
class EmailNotifier extends NotificationChannel {
  constructor(config) {
    super(config);
    this.smtpConfig = config.smtpConfig;
    this.recipients = config.recipients || [];
    this.from = config.from || 'devops@company.com';
  }
  
  async sendNotification(type, data) {
    const formatted = this.formatMessage(type, data);
    
    const emailContent = {
      from: this.from,
      to: this.recipients.join(', '),
      subject: formatted.title,
      html: this.generateEmailHTML(formatted, data)
    };
    
    // 这里需要集成实际的邮件发送服务（如SendGrid、AWS SES等）
    // 为了演示，我们模拟发送过程
    console.log('Sending email:', emailContent.subject);
    
    return {
      success: true,
      recipients: this.recipients.length,
      subject: emailContent.subject
    };
  }
  
  generateEmailHTML(formatted, data) {
    return `
      <!DOCTYPE html>
      <html>
      <head>
        <style>
          body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }
          .header { background-color: #f8f9fa; padding: 20px; border-radius: 5px; }
          .content { margin: 20px 0; }
          .details { background-color: #f1f3f4; padding: 15px; border-radius: 5px; }
          .detail-item { margin: 5px 0; }
          .footer { margin-top: 30px; font-size: 12px; color: #666; }
        </style>
      </head>
      <body>
        <div class="header">
          <h2>${formatted.title}</h2>
        </div>
        <div class="content">
          <p>${formatted.message}</p>
          <div class="details">
            <h3>Details:</h3>
            ${Object.entries(formatted.details).map(([key, value]) => 
              `<div class="detail-item"><strong>${key}:</strong> ${value}</div>`
            ).join('')}
          </div>
        </div>
        <div class="footer">
          <p>This notification was sent by Sentry DevOps Integration at ${new Date().toISOString()}</p>
        </div>
      </body>
      </html>
    `;
  }
}

// Microsoft Teams通知器
class TeamsNotifier extends NotificationChannel {
  constructor(config) {
    super(config);
    this.webhookUrl = config.webhookUrl;
  }
  
  async sendNotification(type, data) {
    const formatted = this.formatMessage(type, data);
    
    const payload = {
      "@type": "MessageCard",
      "@context": "https://schema.org/extensions",
      "summary": formatted.title,
      "themeColor": this.getColorForType(type),
      "sections": [
        {
          "activityTitle": formatted.title,
          "activitySubtitle": formatted.message,
          "facts": Object.entries(formatted.details).map(([key, value]) => ({
            "name": key,
            "value": value
          })),
          "markdown": true
        }
      ]
    };
    
    const response = await fetch(this.webhookUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });
    
    if (!response.ok) {
      throw new Error(`Teams API error: ${response.status} ${response.statusText}`);
    }
    
    return { success: true };
  }
  
  getColorForType(type) {
    const colors = {
      stage_failed: 'FF0000',
      pipeline_success: '00FF00',
      quality_gate_failed: 'FFA500',
      deployment_health_issue: 'FFA500'
    };
    return colors[type] || '0078D4';
  }
}
```

### 4.2 DevOps指标收集器

```javascript
// DevOps指标收集器
class DevOpsMetricsCollector {
  constructor(options = {}) {
    this.options = {
      sentryDsn: options.sentryDsn,
      environment: options.environment || 'production',
      release: options.release,
      metricsRetention: options.metricsRetention || 30, // 30天
      ...options
    };
    
    this.metrics = new Map();
    this.sentryApi = new SentryApi({
      token: process.env.SENTRY_AUTH_TOKEN,
      org: process.env.SENTRY_ORG,
      project: process.env.SENTRY_PROJECT
    });
    
    this.startMetricsCollection();
  }
  
  startMetricsCollection() {
    // 每分钟收集一次指标
    setInterval(() => {
      this.collectAllMetrics().catch(error => {
        console.error('Failed to collect metrics:', error);
      });
    }, 60000);
  }
  
  async collectAllMetrics() {
    const timestamp = Date.now();
    
    try {
      // 收集DORA指标
      const doraMetrics = await this.collectDORAMetrics();
      this.recordMetric('dora', doraMetrics, timestamp);
      
      // 收集错误指标
      const errorMetrics = await this.collectErrorMetrics();
      this.recordMetric('errors', errorMetrics, timestamp);
      
      // 收集性能指标
      const performanceMetrics = await this.collectPerformanceMetrics();
      this.recordMetric('performance', performanceMetrics, timestamp);
      
      // 收集部署指标
      const deploymentMetrics = await this.collectDeploymentMetrics();
      this.recordMetric('deployment', deploymentMetrics, timestamp);
      
      // 清理过期指标
      this.cleanupExpiredMetrics();
      
    } catch (error) {
      console.error('Error collecting metrics:', error);
    }
  }
  
  async collectDORAMetrics() {
    // DORA四大指标：部署频率、变更前置时间、平均恢复时间、变更失败率
    const now = Date.now();
    const oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000);
    
    try {
      // 部署频率 (Deployment Frequency)
      const deployments = await this.getDeployments(oneWeekAgo, now);
      const deploymentFrequency = deployments.length / 7; // 每天部署次数
      
      // 变更前置时间 (Lead Time for Changes)
      const avgLeadTime = this.calculateAverageLeadTime(deployments);
      
      // 平均恢复时间 (Mean Time to Recovery)
      const incidents = await this.getIncidents(oneWeekAgo, now);
      const mttr = this.calculateMTTR(incidents);
      
      // 变更失败率 (Change Failure Rate)
      const failedDeployments = deployments.filter(d => d.status === 'failed');
      const changeFailureRate = deployments.length > 0 ? failedDeployments.length / deployments.length : 0;
      
      return {
        deploymentFrequency,
        leadTime: avgLeadTime,
        mttr,
        changeFailureRate,
        period: '7days'
      };
    } catch (error) {
      // 返回模拟数据
      return {
        deploymentFrequency: 2.1,
        leadTime: 4.5, // 小时
        mttr: 1.2, // 小时
        changeFailureRate: 0.05,
        period: '7days'
      };
    }
  }
  
  async collectErrorMetrics() {
    const now = Date.now();
    const oneHourAgo = now - 3600000;
    
    try {
      const errorData = await this.sentryApi.getErrorStats({
        environment: this.options.environment,
        release: this.options.release,
        start: new Date(oneHourAgo),
        end: new Date(now)
      });
      
      return {
        rate: errorData.rate || 0,
        count: errorData.count || 0,
        uniqueUsers: errorData.uniqueUsers || 0,
        newIssues: errorData.newIssues || 0,
        resolvedIssues: errorData.resolvedIssues || 0
      };
    } catch (error) {
      return {
        rate: 0.002,
        count: 5,
        uniqueUsers: 3,
        newIssues: 1,
        resolvedIssues: 2
      };
    }
  }
  
  async collectPerformanceMetrics() {
    const now = Date.now();
    const oneHourAgo = now - 3600000;
    
    try {
      const performanceData = await this.sentryApi.getPerformanceStats({
        environment: this.options.environment,
        release: this.options.release,
        start: new Date(oneHourAgo),
        end: new Date(now)
      });
      
      return {
        avgResponseTime: performanceData.avgResponseTime || 0,
        p95ResponseTime: performanceData.p95ResponseTime || 0,
        p99ResponseTime: performanceData.p99ResponseTime || 0,
        throughput: performanceData.throughput || 0,
        apdex: performanceData.apdex || 0
      };
    } catch (error) {
      return {
        avgResponseTime: 245,
        p95ResponseTime: 890,
        p99ResponseTime: 1250,
        throughput: 1200,
        apdex: 0.95
      };
    }
  }
  
  async collectDeploymentMetrics() {
    // 收集部署相关指标
    return {
      totalDeployments: 15,
      successfulDeployments: 14,
      failedDeployments: 1,
      avgDeploymentTime: 8.5, // 分钟
      rollbackCount: 0,
      hotfixCount: 1
    };
  }
  
  recordMetric(category, data, timestamp) {
    if (!this.metrics.has(category)) {
      this.metrics.set(category, []);
    }
    
    const categoryMetrics = this.metrics.get(category);
    categoryMetrics.push({
      timestamp,
      data
    });
    
    // 保持最近1000条记录
    if (categoryMetrics.length > 1000) {
      categoryMetrics.splice(0, categoryMetrics.length - 1000);
    }
  }
  
  getMetrics(category, timeRange = 3600000) { // 默认1小时
    if (!this.metrics.has(category)) {
      return [];
    }
    
    const now = Date.now();
    const startTime = now - timeRange;
    
    return this.metrics.get(category).filter(metric => 
      metric.timestamp >= startTime
    );
  }
  
  async getErrorRate() {
    const errorMetrics = this.getMetrics('errors', 3600000);
    if (errorMetrics.length === 0) return 0;
    
    const latestMetric = errorMetrics[errorMetrics.length - 1];
    return latestMetric.data.rate;
  }
  
  async getPerformanceScore() {
    const performanceMetrics = this.getMetrics('performance', 3600000);
    if (performanceMetrics.length === 0) return 80;
    
    const latestMetric = performanceMetrics[performanceMetrics.length - 1];
    const data = latestMetric.data;
    
    // 基于响应时间和Apdex计算性能分数
    let score = 100;
    
    // 响应时间影响（权重50%）
    if (data.avgResponseTime > 1000) {
      score -= 25;
    } else if (data.avgResponseTime > 500) {
      score -= 10;
    }
    
    // Apdex影响（权重50%）
    score = score * 0.5 + (data.apdex * 100) * 0.5;
    
    return Math.round(score);
  }
  
  cleanupExpiredMetrics() {
    const retentionTime = this.options.metricsRetention * 24 * 60 * 60 * 1000;
    const cutoffTime = Date.now() - retentionTime;
    
    for (const [category, metrics] of this.metrics) {
      const filteredMetrics = metrics.filter(metric => 
        metric.timestamp >= cutoffTime
      );
      this.metrics.set(category, filteredMetrics);
    }
  }
  
  generateReport() {
    const report = {
      timestamp: new Date().toISOString(),
      environment: this.options.environment,
      release: this.options.release,
      metrics: {}
    };
    
    for (const [category, metrics] of this.metrics) {
      if (metrics.length > 0) {
        const latestMetric = metrics[metrics.length - 1];
        report.metrics[category] = latestMetric.data;
      }
    }
    
    return report;
  }
  
  // 辅助方法
  async getDeployments(startTime, endTime) {
    // 模拟获取部署数据
    return [
      { id: '1', timestamp: startTime + 86400000, status: 'success', leadTime: 3.5 },
      { id: '2', timestamp: startTime + 172800000, status: 'success', leadTime: 4.2 },
      { id: '3', timestamp: startTime + 259200000, status: 'failed', leadTime: 2.1 },
      { id: '4', timestamp: startTime + 345600000, status: 'success', leadTime: 5.8 }
    ];
  }
  
  async getIncidents(startTime, endTime) {
    // 模拟获取事故数据
    return [
      { id: '1', startTime: startTime + 100000, endTime: startTime + 104000, duration: 4000 },
      { id: '2', startTime: startTime + 200000, endTime: startTime + 206000, duration: 6000 }
    ];
  }
  
  calculateAverageLeadTime(deployments) {
    if (deployments.length === 0) return 0;
    const totalLeadTime = deployments.reduce((sum, deployment) => sum + deployment.leadTime, 0);
    return totalLeadTime / deployments.length;
  }
  
  calculateMTTR(incidents) {
    if (incidents.length === 0) return 0;
    const totalRecoveryTime = incidents.reduce((sum, incident) => sum + incident.duration, 0);
    return (totalRecoveryTime / incidents.length) / (1000 * 60 * 60); // 转换为小时
  }
}
```

## 5. 最佳实践与总结

### 5.1 实施建议

1. **渐进式集成**
   - 从单个环境开始，逐步扩展到所有环境
   - 先实现基础监控，再添加高级功能
   - 建立明确的质量门控标准

2. **团队协作**
   - 建立跨团队的DevOps文化
   - 定期回顾和优化流水线
   - 培训团队成员使用监控工具

3. **持续改进**
   - 定期分析指标趋势
   - 根据反馈调整质量门控
   - 优化通知策略，避免告警疲劳

### 5.2 核心价值

- **自动化监控**：实现从代码提交到生产部署的全流程监控
- **质量保障**：通过质量门控确保代码质量和系统稳定性
- **快速反馈**：及时发现和解决问题，缩短修复时间
- **数据驱动**：基于指标数据进行决策和优化
- **团队协作**：促进开发、测试、运维团队的协作

### 5.3 未来发展趋势

- **AI驱动的异常检测**：利用机器学习识别异常模式
- **预测性维护**：基于历史数据预测潜在问题
- **自动化修复**：实现问题的自动诊断和修复
- **云原生集成**：与Kubernetes、服务网格等深度集成

通过构建完整的Sentry DevOps集成方案，我们可以实现高效、可靠的软件交付流水线，提升开发效率和产品质量，为企业的数字化转型提供强有力的技术支撑。